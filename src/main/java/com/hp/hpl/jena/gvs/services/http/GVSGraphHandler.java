/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: GVSGraphHandler.java,v 1.6 2007/06/25 11:18:13 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http;

import java.io.StringReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.util.EnhancedRequest;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterCollection;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterUtil;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.util.java.MillisDateFormat;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler;
import com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class GVSGraphHandler implements GraphHandler {

	private Store store;

	private GraphHandler unavailableSourceGetHandler;

	

	/**
	 * Instantiates a GraphHandler to access a store
	 * 
	 * @param store
	 * @param unavailableSourceGetHandler
	 *            the handler to be called on a request to the url of a
	 *            non-existing source
	 */
	public GVSGraphHandler(Store store, GraphHandler unavailableSourceGetHandler) {
		this.store = store;
		this.unavailableSourceGetHandler = unavailableSourceGetHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler#getGraph(org.wymiwyg.wrhapi.Request)
	 */
	public void handle(Request request, TypedResponse<Graph> response)
			throws HandlerException {
		Date moment = GVSHttpUtil.getMoment(request);
		String[] resourceURIStrings = request.getRequestURI()
				.getParameterValues("resource");
		if (request.getRequestURI().getPath().equals("/gvs")) {
			gvsService(request, moment, resourceURIStrings, response);
		} else {
			EnhancedRequest ehRequest = new EnhancedRequest(request);
			URL sourceURL = ehRequest.getRequestURLWithoutParams();
			Source source = new SourceImpl(sourceURL.toString());
			if (request.getMethod().equals(Method.GET) && (!store.getSources().contains(source))) {
				unavailableSourceGetHandler.handle(request, response);
			} else {
				gvsSourceHandle(request, source, moment, resourceURIStrings,
						response);
			}
		}

	}

	/**
	 * @param sourceURL
	 * @param moment
	 * @param resourceURIStrings
	 * @param response
	 * @return
	 * @throws HandlerException
	 */
	private void gvsSourceHandle(Request request, Source source, Date moment,
			String[] resourceURIStrings, TypedResponse<Graph> response)
			throws HandlerException {
		Set<Source> sources = Collections.singleton(source);
		handleSources(request, sources, moment, resourceURIStrings, response);

	}

	/**
	 * @param request
	 * @param sources
	 * @param moment
	 * @param resourceURIStrings
	 * @param response
	 * @throws HandlerException
	 */
	private void handleSources(Request request, Set<Source> sources,
			Date moment, String[] resourceURIStrings,
			TypedResponse<Graph> response) throws HandlerException {
		if (request.getMethod().equals(Method.GET)) {
			handleSourcesGet(sources, moment, resourceURIStrings, response);
		} else {
			if (request.getMethod().equals(Method.PUT)) {
				handleSourcesPut(request, sources, resourceURIStrings, response);
			} else {
				if (request.getMethod().equals(Method.POST)) {
					handleSourcesPost(request, sources,
							resourceURIStrings, response);
				}
			}
		}

	}

	/**
	 * @param request
	 * @param sources
	 * @param moment
	 * @param response
	 * @throws HandlerException
	 */
	private void handleSourcesPut(Request request, Set<Source> sources,
			String[] resourceURIStrings, TypedResponse<Graph> response)
			throws HandlerException {
		if (sources.size() != 1) {
			throw new RuntimeException("Must PUT as exactly one source");
		}
		Source source = sources.iterator().next();
		Model bodyModel = ModelFactory.createDefaultModel();
		try {
			bodyModel.read(Channels.newInputStream(request.getMessageBody()
					.read()), source.getURIRef());
		} catch (Exception e) {
			throw new HandlerException(e);
		}
		final FCAGraph graph = new FCAGraphImpl(bodyModel);
		if (resourceURIStrings != null) {

			final Set<GroundedNode> onlyForGroundedNodes = new HashSet<GroundedNode>(
					resourceURIStrings.length);
			for (String uriString : resourceURIStrings) {
				onlyForGroundedNodes.add(new NamedNodeImpl(uriString));
			}
			// response.setBody(reconstructGraph(filterableDecomposition,
			// onlyForGroundedNodes));
			store.perform(source, new StoreTransaction() {

				public void execute(SourceStoreView storeView) {
					FCAGraph fcaGraph = storeView.getGraph();
					storeView
							.revokeGraph(fcaGraph.filter(onlyForGroundedNodes));
					storeView.assertGraph(graph);
				}

			});
		} else {
			store.updateGraph(source, graph);
		}

	}

	private void handleSourcesPost(Request request, Set<Source> sources, String[] resourceURIStrings,
			TypedResponse<Graph> response) throws HandlerException {
		if (sources.size() != 1) {
			throw new RuntimeException("Must POST as exactly one source");
		}
		ParameterCollection parameters = ParameterUtil.getBodyPameters(request);
		
		Source source = sources.iterator().next();
		String assertedRDFXML = parameters.getParameteValues("assert")[0].toString();
		String revokedRDFXML = parameters.getParameteValues("revoke")[0].toString();
		Model assertedModel = ModelFactory.createDefaultModel();
		assertedModel
				.read(new StringReader(assertedRDFXML), source.getURIRef());
		FCAGraph assertedGraph = new FCAGraphImpl(assertedModel);
		Model revokedModel = ModelFactory.createDefaultModel();
		revokedModel.read(new StringReader(revokedRDFXML), source.getURIRef());
		FCAGraph revokedGraph = new FCAGraphImpl(revokedModel);
		store.changeGraph(source, revokedGraph, assertedGraph);
	}

	/**
	 * @param sources
	 * @param moment
	 * @param resourceURIStrings
	 * @param response
	 * @throws HandlerException
	 */
	private void handleSourcesGet(Set<Source> sources, Date moment,
			String[] resourceURIStrings, TypedResponse<Graph> response)
			throws HandlerException {

		GraphOverTime got = store.getGraphOverTime(sources);
		{
			Iterator<Date> previousIter = got.previousChanges(moment);
			List<String> previousDateStringList = new ArrayList<String>();
			int count = 0;
			while (previousIter.hasNext()) {
				if (count++ == 10) {
					break;
				}
				previousDateStringList.add(MillisDateFormat.instance
						.format(previousIter.next()));
			}
			Object[] previousArray = previousDateStringList.toArray();
			if (previousArray.length > 0) {
				response.setHeader(HeaderName.get("X-GVS-Previous"),
						previousArray);
			}
		}
		{
			Iterator<Date> followingIter = got.followingChanges(moment);
			List<String> previousDateStringList = new ArrayList<String>();
			int count = 0;
			while (followingIter.hasNext()) {
				if (count++ == 10) {
					break;
				}
				previousDateStringList.add(MillisDateFormat.instance
						.format(followingIter.next()));
			}
			Object[] previousArray = previousDateStringList.toArray();
			if (previousArray.length > 0) {
				response.setHeader(HeaderName.get("X-GVS-Following"),
						previousDateStringList.toArray());
			}
		}
		if (resourceURIStrings != null) {
			FCAGraph fcaGraph = got.getGraph(moment);
			Set<GroundedNode> onlyForGroundedNodes = new HashSet<GroundedNode>(
					resourceURIStrings.length);
			for (String uriString : resourceURIStrings) {
				onlyForGroundedNodes.add(new NamedNodeImpl(uriString));
			}
			response.setBody(fcaGraph.filter(onlyForGroundedNodes));
		} else {
			response.setBody(got.getGraph(moment));
		}
	}

	/**
	 * @param request
	 * @param resourceURIStrings
	 * @return
	 * @throws HandlerException
	 */
	private void gvsService(Request request, Date moment,
			String[] resourceURIStrings, TypedResponse<Graph> response)
			throws HandlerException {
		String[] sourceString = request.getRequestURI().getParameterValues(
				"source");
		Set<Source> sources = new HashSet<Source>();
		if (sourceString != null) {
			for (int i = 0; i < sourceString.length; i++) {
				sources.add(new SourceImpl(sourceString[i]));
			}
		}
		handleSources(request, sources, moment, resourceURIStrings, response);
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

