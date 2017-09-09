/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: AggregatorService.java,v 1.3 2007/06/04 20:31:25 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.aggregator;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.util.text.W3CDateFormat;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.Node;
import org.wymiwyg.rdf.graphs.Triple;
import org.wymiwyg.rdf.graphs.impl.SimpleGraph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.services.Service;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
public class AggregatorService extends Thread implements Service {

	/**
	 * @author reto
	 * 
	 */
	private static class AllreadyLoggedRuntimeException extends
			RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param e
		 */
		public AllreadyLoggedRuntimeException(Exception e) {
			super(e);
		}

	}

	private final static Log log = LogFactory.getLog(AggregatorService.class);

	private int interval;

	private GraphOverTime configuration;

	private Source identity;

	private GraphOverTime logReader;

	private Store store;

	private boolean finish = false;

	/**
	 * @param store
	 * @param identity
	 * @param trustedSources
	 * @param configuration
	 */
	public AggregatorService(Store store, Source identity,
			Set<Source> trustedSources, NamedNode configuration) {
		this(60, store, store.getGraphOverTime(trustedSources), identity);
	}

	/**
	 * @param interval
	 *            the amount of seconds the aggregator should pause between
	 *            checking for due aggrgations
	 * @param store
	 *            the store to which the aggregated sources are added
	 * @param configuration
	 *            the GraphOverTime from which the configuration is read
	 * @param identity
	 *            the identity used when adding logging-data to the source
	 */
	public AggregatorService(int interval, Store store,
			GraphOverTime configuration, Source identity) {
		this(interval, store, configuration, identity, store
				.getGraphOverTime(Collections.singleton(identity)));
	}

	/**
	 * @param interval
	 *            the amount of seconds the aggregator should pause between
	 *            checking for due aggrgations
	 * @param store
	 *            the store to which the aggregated sources are added
	 * @param configuration
	 *            the GraphOverTime from which the configuration is read
	 * @param identity
	 *            the identity used when adding logging-data to the source
	 * @param logReader
	 *            the GraphOverTime from which the logged data is read to decide
	 *            if the aggrgation of a source is due, this must contain the
	 *            data logged by this aggregator
	 */
	public AggregatorService(int interval, Store store,
			GraphOverTime configuration, Source identity,
			GraphOverTime logReader) {
		this.interval = interval;
		this.store = store;
		this.configuration = configuration;
		this.identity = identity;
		this.logReader = logReader;
	}

	/**
	 * finish aggregating
	 */
	public void finish() {
		finish = true;
		notifyAll();
	}

	/**
	 * @param currentConfiguration
	 * @param currentLog
	 * @param now
	 * @return
	 */
	private static Set<Source> getDueSources(Graph currentConfiguration,
			Graph currentLog, Date now) {
		Model configurationModel = JenaUtil
				.getModelFromGraph(currentConfiguration);
		Model logModel = JenaUtil.getModelFromGraph(currentLog);
		Set<Source> result = new HashSet<Source>();
		ResIterator sourceIter = configurationModel.listSubjectsWithProperty(
				RDF.type, AGGREGATOR.AggregatedSource);
		while (sourceIter.hasNext()) {
			Resource aggregatedSource = sourceIter.nextResource();
			Date lastAggregation = getLastAggregationDate((Resource) aggregatedSource
					.inModel(logModel));
			if (lastAggregation == null) {
				result.add(new SourceImpl(aggregatedSource.getURI()));
			} else {
				int aggregationInterval = aggregatedSource.getProperty(
						AGGREGATOR.interval).getInt();
				Date aggregationDueDate = new Date(lastAggregation.getTime()
						+ aggregationInterval * 1000);
				if (!aggregationDueDate.after(now)) {
					result.add(new SourceImpl(aggregatedSource.getURI()));
				}
			}
		}
		return result;
	}

	/**
	 * @param node
	 * @return the Date of the last AggragationAttempt or null if none exist
	 */
	private static Date getLastAggregationDate(Resource aggregatedSource) {
		Model model = aggregatedSource.getModel();
		ResIterator downloadAttempts = model.listSubjectsWithProperty(
				AGGREGATOR.aggregatedSource, aggregatedSource);
		Date result = null;
		while (downloadAttempts.hasNext()) {
			Resource downloadAtttempt = downloadAttempts.nextResource();
			Date time;
			try {
				time = new W3CDateFormat().parse(downloadAtttempt.getProperty(
						AGGREGATOR.time).getString());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			if ((result == null) || (time.after(result))) {
				result = time;
			}
		}
		return result;
	}

	@Override
	public void run() {
		while (true) {
			Date now = new Date();
			Graph currentConfiguration = configuration.getGraph(now);
			Graph currentLog = logReader.getGraph(now);
			Set<Source> dueSources = getDueSources(currentConfiguration,
					currentLog, now);
			for (Source node : dueSources) {
				aggregate(node, store, identity);
			}
			try {
				synchronized (this) {
					wait(interval * 1000);
				}

			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (finish) {
				return;
			}
		}
	}

	/**
	 * @param aggregatedSource
	 * @param store
	 * @param identity
	 */
	private static void aggregate(final Source aggregatedSource, Store store,
			Source identity) {
		URL url;
		try {
			url = new URL(aggregatedSource.getURIRef());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		Date now = new Date();

		Model logEntryModel = ModelFactory.createDefaultModel();
		Resource downloadAttempt = logEntryModel
				.createResource(AGGREGATOR.DownloadAttempt);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		Literal momentLit = logEntryModel.createTypedLiteral(new XSDDateTime(
				calendar));
		downloadAttempt.addProperty(AGGREGATOR.time, momentLit);
		downloadAttempt.addProperty(AGGREGATOR.aggregatedSource, logEntryModel
				.createResource(aggregatedSource.getURIRef()));
		downloadAttempt.addProperty(AGGREGATOR.aggregator, logEntryModel
				.createResource(identity.getURIRef()));
		Graph logEntryGraph = JenaUtil.getGraphFromModel(logEntryModel, true);
		final StringWriter aggregationWarningWriter = new StringWriter();
		final StringWriter aggregationErrorWriter = new StringWriter();
		try {
			Model model = ModelFactory.createDefaultModel();
			// model.read(url.toString());
			JenaReader jenaReader = new JenaReader();
			jenaReader.setErrorHandler(new RDFErrorHandler() {

				public void error(Exception e) {
					aggregationErrorWriter.write(e.getMessage());
				}

				public void fatalError(Exception e) {
					aggregationErrorWriter.write("FATAL: ");
					aggregationErrorWriter.write(e.getMessage());
					log.info("Fatal Error aggregating " + aggregatedSource, e);
					throw new AllreadyLoggedRuntimeException(e);
				}

				public void warning(Exception e) {
					aggregationWarningWriter.write(e.getMessage());
				}

			});
			jenaReader.read(model, url.toString());
			String errorMessage = aggregationErrorWriter.toString();
			if (errorMessage.equals("")) {
				Graph graph = JenaUtil.getGraphFromModel(model, true);
				store.updateGraph(aggregatedSource, new FCAGraphImpl(graph));
			} else {
				if (!errorMessage.equals("")) {
					downloadAttempt.addProperty(AGGREGATOR.errorMessage,
							errorMessage);
				}
			}
		} catch (Throwable th) {
			if (!(th instanceof AllreadyLoggedRuntimeException)) {
				String errorMessage = th.getMessage();
				if (errorMessage == null) {
					errorMessage = th.toString();
				}
				downloadAttempt.addProperty(AGGREGATOR.errorMessage,
						errorMessage);
			}
		}
		String warningMessage = aggregationWarningWriter.toString();
		if (!warningMessage.equals("")) {
			downloadAttempt.addProperty(AGGREGATOR.warningMessage,
					warningMessage);
		}

		updateWorkingGraph(store, identity, now, aggregatedSource,
				logEntryGraph);
		// store.assertGraph(identity, new FCAGraphImpl(logEntryGraph), now);
	}

	/**
	 * @param store
	 * @param identity
	 * @param now
	 * @param aggregatedSource
	 * @param logEntryGraph
	 */
	private static void updateWorkingGraph(Store store, Source identity,
			Date now, final Source aggregatedSource, final Graph logEntryGraph) {
		store.perform(identity, new StoreTransaction() {

			public void execute(SourceStoreView storeView) {
				SimpleGraph newWorkingGraph = new SimpleGraph();
				FCAGraph oldWorkingGraph = storeView.getGraph();
				Node downloadAttemptNode = null;
				for (Triple triple : oldWorkingGraph) {
					if (triple.getObject().equals(aggregatedSource)
							&& triple.getPredicate().getURIRef().equals(
									AGGREGATOR.aggregatedSource.getURI())) {
						downloadAttemptNode = triple.getSubject();
						break;
					}
				}
				for (Iterator<Triple> iter = oldWorkingGraph.iterator(); iter
						.hasNext();) {
					Triple triple = iter.next();
					// x.equals(null) should return false, making the first
					// condition obsolete
					if ((downloadAttemptNode == null)
							|| !triple.getSubject().equals(downloadAttemptNode)) {
						newWorkingGraph.add(triple);
					}
				}
				newWorkingGraph.addAll(logEntryGraph);
				newWorkingGraph.markFinalized();
				storeView.revokeAll();
				storeView.assertGraph(new FCAGraphImpl(newWorkingGraph));
			}

		});
	}
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

