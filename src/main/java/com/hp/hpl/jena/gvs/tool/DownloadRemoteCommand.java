/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: DownloadRemoteCommand.java,v 1.6 2007/10/01 19:29:55 rebach Exp $
 */
package com.hp.hpl.jena.gvs.tool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.METAMODEL;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.util.ToyClock;
import com.hp.hpl.jena.gvs.impl.util.java.MillisDateFormat;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
@CommandDescription(description = "Downloads all contents of a remote store to the local store")
public class DownloadRemoteCommand extends
		GVSToolCommand<DownloadRemoteCommandArgumets> {

	/**
	 * @author reto
	 * 
	 */
	private class SliceResponse {

		private Date nextFollowing;

		private Model model;

		/**
		 * @param model
		 * @param nextFollowing
		 */
		public SliceResponse(Model model, Date nextFollowing) {
			this.model = model;
			this.nextFollowing = nextFollowing;
		}

		Model getModel() {
			return model;
		}

		Date getNextFollowing() {
			return nextFollowing;
		}

		/**
		 * @return
		 */
		public FCAGraph getGraph() {
			return new FCAGraphImpl(model);
		}

	}

	private static final Log log = LogFactory
			.getLog(DownloadRemoteCommand.class);

	private static final TimeZone utcTZ = new SimpleTimeZone(0, "UTC");

	private SimpleDateFormat dateFormat;

	private DateFormat millisDateFormat = new MillisDateFormat();
	{
		String datePattern = "yyyyMMddHHmmssSSS";
		dateFormat = new SimpleDateFormat(datePattern);
		dateFormat.setTimeZone(utcTZ);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#execute(com.hp.hpl.jena.gvs.Store,
	 *      com.hp.hpl.jena.gvs.tool.GVSToolArguments)
	 */
	@Override
	protected void execute(Store store, DownloadRemoteCommandArgumets arguments) {
		URL remoteService = arguments.getRemoteServiceURL();
		log.info("Remote Service: " + remoteService);
		Clock origClock = store.getClock();
		ToyClock toyClock = new ToyClock();
		store.setClock(toyClock);
		Set<String> sources = getSources(remoteService);
		for (String omitSource : arguments.getOmitSourceURLs()) {
			if (!sources.contains(omitSource)) {
				throw new RuntimeException("Cannot omit " + omitSource
						+ " it doesn't exist.");
			}
			sources.remove(omitSource);
		}
		for (String source : sources) {
			log.info("Processing source: " + source);
			try {
				downloadSource(store, toyClock, remoteService, source);
			} catch (Exception e) {
				log.error("Failed downloading source "+source, e);
			}
		}
		store.setClock(origClock);
	}

	private void downloadSource(Store store, ToyClock toyClock,
			URL remoteService, String source) {
		Date date = new Date(0);
		SliceResponse response = getSlice(remoteService, source, date);
		while (true) {
			date = response.getNextFollowing();
			response = getSlice(remoteService, source, date);
			toyClock.setTime(date);
			store.updateGraph(new SourceImpl(source), response.getGraph());
			if (response.getNextFollowing() == null) {
				return;
			}
		}

	}

	/**
	 * @param remoteService
	 * @param source
	 * @param date
	 * @return
	 */
	private SliceResponse getSlice(URL remoteService, String source, Date date) {
		try {
			log.info("getting slice of " + source + " at " + date);
			URL sliceURL = getSliceURL(remoteService, source, date);
			HttpURLConnection connection = (HttpURLConnection) sliceURL
					.openConnection();
			String gvsFollowing = connection.getHeaderField("X-GVS-Following");
			Date nextFollowing = null;
			if (gvsFollowing != null) {
				StringTokenizer tokenizer = new StringTokenizer(gvsFollowing,
						",");
				try {
					String nextFollowingString = tokenizer.nextToken();
					nextFollowing = millisDateFormat.parse(nextFollowingString);
				} catch (NoSuchElementException e) {
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			Model model = ModelFactory.createDefaultModel();
			model.read(connection.getInputStream(), sliceURL.toExternalForm());
			// model.write(System.out);
			return new SliceResponse(model, nextFollowing);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param remoteService
	 * @param source
	 * @param date
	 * @return
	 */
	private URL getSliceURL(URL remoteService, String source, Date date) {
		try {
			return new URL(remoteService, "/gvs?source="
					+ URLEncoder.encode(source, "utf-8") + "&moment="
					+ dateFormat.format(date));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> getSources(URL remoteService) {
		Set<String> result = new HashSet<String>();
		try {
			URL sourcesURL = new URL(remoteService, "/meta/sources");
			Model sourcesModel = ModelFactory.createDefaultModel();
			sourcesModel.read(sourcesURL.toString());
			ResIterator sourcesIter = sourcesModel.listSubjectsWithProperty(
					RDF.type, METAMODEL.Source);
			while (sourcesIter.hasNext()) {
				result.add(sourcesIter.nextResource().getURI());
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#getArgumentsClass()
	 */
	@Override
	protected Class<DownloadRemoteCommandArgumets> getArgumentsClass() {
		return DownloadRemoteCommandArgumets.class;
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

