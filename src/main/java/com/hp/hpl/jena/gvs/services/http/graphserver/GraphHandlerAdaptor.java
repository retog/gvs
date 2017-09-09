/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GraphHandlerAdaptor.java,v 1.1 2007/05/12 07:41:54 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http.graphserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.wymiwyg.r3xserializer.Serializer;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Write;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author reto
 *
 */
public class GraphHandlerAdaptor implements Handler {

	private GraphHandler graphHandler;
	
	public GraphHandlerAdaptor(GraphHandler graphHandler) {
		this.graphHandler = graphHandler;
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.wrhapi.Handler#handle(org.wymiwyg.rwcf.Request, org.wymiwyg.wrhapi.Response)
	 */
	public void handle(Request request, Response response)
			throws HandlerException {
		final String[] stylesheetParams = request.getRequestURI().getParameterValues("stylesheet");
		graphHandler.handle(request, new TypedResponseWrapper<Graph>(response) {
			
			String stylesheet = null;
			
			public void setBody(final Graph graph) throws HandlerException {
				if (stylesheetParams != null) {
					stylesheet = stylesheetParams[0];
				}
				if (stylesheet == null) {
					super.setHeader(HeaderName.CONTENT_TYPE, "application/rdf+xml");
				} else {
					super.setHeader(HeaderName.CONTENT_TYPE, "application/xml");
				}
				super.setBody(new MessageBody2Write() {
					//<?xml-stylesheet type="text/xsl" href="/stylesheets/topic"?>
					public void writeTo(WritableByteChannel out) throws IOException {
						Model model = JenaUtil.getModelFromGraph(graph);
						//TODO deliver other formats
						OutputStream outS = Channels.newOutputStream(out); 
						
						if (stylesheet != null) {
							outS.write(("<?xml-stylesheet type=\"text/xsl\" href=\""+stylesheet+"\"?>\n").getBytes("utf-8"));
						}
						Writer outW = new OutputStreamWriter(outS, "utf-8");
						new Serializer().serialize(model, "", outW);
						outW.flush();
						//model.write(outS);
						outS.close();
					}
					
				});
			}

			public void setDefaultStylesheet(String stylesheet) {
				this.stylesheet = stylesheet;
				
			}
		});
		

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

