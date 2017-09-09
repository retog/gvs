/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: DiscoBitHandler.java,v 1.2 2007/05/28 16:00:43 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.wymiwyg.commons.vocabulary.DISCOBITS;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.Node;
import org.wymiwyg.rdf.graphs.Triple;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.impl.PropertyNodeImpl;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.util.EnhancedRequest;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler;
import com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse;

/**
 * @author reto
 * 
 */
public class DiscoBitHandler implements GraphHandler {

	private GraphOverTime trustedGOT;

	/**
	 * @param trustedGOT
	 */
	public DiscoBitHandler(GraphOverTime trustedGOT) {
		this.trustedGOT = trustedGOT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler#handle(org.wymiwyg.wrhapi.Request,
	 *      com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse)
	 */
	public void handle(Request request, TypedResponse<Graph> response)
			throws HandlerException {
		Date moment = GVSHttpUtil.getMoment(request);
		EnhancedRequest ehRequest = new EnhancedRequest(request);
		String resourceURIString = ehRequest.getRequestURLWithoutParams()
				.toString();
		response.setDefaultStylesheet("/application/stylesheets/combined");
		Set<GroundedNode> resources = new HashSet<GroundedNode>();
		resources.add(new NamedNodeImpl(resourceURIString));
		FCAGraph completeGraph = trustedGOT.getGraph(moment);
		int lastResourcesCount = 0;
		FCAGraph graph = null;
		while (resources.size() > lastResourcesCount) {
			lastResourcesCount = resources.size();
			graph = completeGraph.filter(resources);
			for (Triple triple : graph) {
				if (triple.getPredicate().equals(
						new PropertyNodeImpl(DISCOBITS.getURI() + "holds"))) {
					Node object = triple.getObject();
					if (object instanceof GroundedNode) {
						resources.add((GroundedNode) object);
					}
				}
			}
		}
		response.setBody(graph);

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

