/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: PutInfoBitHandler.java,v 1.2 2007/06/25 11:18:12 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;

import org.wymiwyg.commons.vocabulary.DISCOBITS;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Read;
import org.wymiwyg.wrhapi.util.parameterparser.FormFile;
import org.wymiwyg.wrhapi.util.parameterparser.MultiPartBody;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterUtil;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
public class PutInfoBitHandler implements Handler {

	private Store store;

	private Source identity;

	/**
	 * @param store
	 * @param identity
	 */
	public PutInfoBitHandler(Store store, Source identity) {
		this.store = store;
		this.identity = identity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.wrhapi.Handler#handle(org.wymiwyg.wrhapi.Request,
	 *      org.wymiwyg.wrhapi.Response)
	 */
	public void handle(Request request, Response response)
			throws HandlerException {
		MultiPartBody body = ParameterUtil.parseMultipart(request);
		final FormFile formFile = body.getFormFileParameterValues("file")[0];
		final String locationString = body.getTextParameterValues("location")[0];
		final Model editableModel = ModelFactory.createDefaultModel();
		final Resource infoBitResource = editableModel.createResource();
		
		infoBitResource.addProperty(DISCOBITS.mediaType, formFile
				.getMimeType().toString());
		infoBitResource.addProperty(DISCOBITS.bytes, editableModel
				.createTypedLiteral(formFile.getContent()));
		infoBitResource.addProperty(RDF.type, DISCOBITS.InfoBit);
		store.perform(identity, new StoreTransaction() {

			public void execute(SourceStoreView storeView) {
				NamedNode locationNode = new NamedNodeImpl(locationString);
				FCAGraph origGraph = storeView.getGraph().filter(
						Collections.singleton((GroundedNode) locationNode));
				Model origModel = ModelFactory.createModelForGraph(origGraph);
				editableModel.add(origModel);
				Resource locationResource = editableModel
						.createResource(locationString);
				locationResource.addProperty(RDF.type, DISCOBITS.InfoDiscoBit);
				locationResource.removeAll(DISCOBITS.infoBit);
				locationResource.addProperty(DISCOBITS.infoBit, infoBitResource);
				storeView.revokeGraph(origGraph);
				storeView.assertGraph(new FCAGraphImpl(editableModel));
			}

		});
		response.setBody(new MessageBody2Read() {
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(PutInfoBitHandler.class.getResourceAsStream("put-info-bit-response.xhtml"));
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

