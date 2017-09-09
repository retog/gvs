/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: InfoDiscoBitFilter.java,v 1.1 2007/06/04 18:34:10 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wymiwyg.commons.vocabulary.DISCOBITS;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.util.EnhancedRequest;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
public class InfoDiscoBitFilter implements Filter {

	private interface InfoBit {
		String getMimeTypeString();

		MessageBody getContent();
	}

	private final Map<String, InfoBit> infoDiscoBitURIs = new HashMap<String, InfoBit>();

	/**
	 * @param trustedGOT
	 */
	public InfoDiscoBitFilter(final GraphOverTime trustedGOT) {
		new Thread() {

			@Override
			public void run() {
				while (true) {
					Model model = ModelFactory.createModelForGraph(trustedGOT
							.getGraph(new Date()));
					addURIsForType(model, DISCOBITS.InfoDiscoBit);
					addURIsForType(model, DISCOBITS.XHTMLInfoDiscoBit);
					try {
						Thread.sleep(60 * 1000); // Sleep a minute
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

			private void addURIsForType(Model model, OntClass type) {
				ResIterator subjectIter = model.listSubjectsWithProperty(
						RDF.type, type);
				// model.write(System.out);
				while (subjectIter.hasNext()) {
					Resource infoDiscoResource = subjectIter.nextResource();
					RDFNode infoBitNode = infoDiscoResource.getProperty(
							DISCOBITS.infoBit).getObject();
					if (infoBitNode instanceof Literal) {
						final Literal literaInfoBit = (Literal) infoBitNode;
						infoDiscoBitURIs.put(infoDiscoResource.getURI(),
								new InfoBit() {

									public MessageBody getContent() {
										return new MessageBody2Read() {

											public ReadableByteChannel read()
													throws IOException {
												// TODO Auto-generated method
												// stub
												return Channels
														.newChannel(new ByteArrayInputStream(
																literaInfoBit
																		.getString()
																		.getBytes(
																				"utf-8")));
											}

										};
									}

									public String getMimeTypeString() {
										return "application/xhtml+xml";
									}

								});
					} else {
						final Resource literaInfoBit = (Resource) infoBitNode;
						infoDiscoBitURIs.put(infoDiscoResource.getURI(),
								new InfoBit() {

									public MessageBody getContent() {
										return new MessageBody2Read() {

											public ReadableByteChannel read()
													throws IOException {
												// TODO Auto-generated method
												// stub
												return Channels
														.newChannel(new ByteArrayInputStream(
																(byte[]) literaInfoBit.getRequiredProperty(DISCOBITS.bytes).getLiteral().getValue()
																		));
											}

										};
									}

									public String getMimeTypeString() {
										return literaInfoBit.getRequiredProperty(DISCOBITS.mediaType).getString();
									}

								});
					}
				}
			}

		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.wrhapi.filter.Filter#handle(org.wymiwyg.wrhapi.Request,
	 *      org.wymiwyg.wrhapi.Response, org.wymiwyg.wrhapi.Handler)
	 */
	public void handle(Request request, Response response, Handler rest)
			throws HandlerException {
		String requestURL = new EnhancedRequest(request)
				.getRequestURLWithoutParams().toString();
		if (infoDiscoBitURIs.containsKey(requestURL)) {
			InfoBit infoBit = infoDiscoBitURIs.get(requestURL);
			response.setHeader(HeaderName.CONTENT_TYPE, infoBit
					.getMimeTypeString());
			response.setBody(infoBit.getContent());
		} else {
			rest.handle(request, response);
		}
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

