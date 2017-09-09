/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: SourceGraphHandler.java,v 1.1 2007/05/12 07:42:05 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http;

import java.security.AccessController;
import java.util.Set;

import javax.security.auth.Subject;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.METAMODEL;
import com.hp.hpl.jena.gvs.security.GVSPrincipal;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler;
import com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 *
 */
public class SourceGraphHandler implements GraphHandler {

	private Store store;

	/**
	 * @param store
	 */
	public SourceGraphHandler(Store store) {
		this.store = store;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler#getGraph(org.wymiwyg.wrhapi.Request)
	 */
	public void handle(Request request, TypedResponse<Graph> response) throws HandlerException {
		Set<Source> sources = store.getSources();
		Model resultModel = ModelFactory.createDefaultModel();
		for (Source source : sources) {
			Resource sourceRes = resultModel.createResource(source.getURIRef());
			sourceRes.addProperty(RDF.type, METAMODEL.Source);
		}
		Subject subject = Subject.getSubject(AccessController.getContext());
		Set<GVSPrincipal> gvsPrincipals = subject.getPrincipals(GVSPrincipal.class);
		for (GVSPrincipal principal : gvsPrincipals) {
			resultModel.add(org.wymiwyg.commons.jena.JenaUtil.getExpandedResource(principal.getUserResource(), 2));
		}
		response.setBody(JenaUtil.getGraphFromModel(resultModel, true));
	}

}


/*
    (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

