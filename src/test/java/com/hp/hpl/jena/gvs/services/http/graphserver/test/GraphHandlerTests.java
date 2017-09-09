/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GraphHandlerTests.java,v 1.1 2007/05/12 07:42:09 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http.graphserver.test;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;

import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandlerAdaptor;
import com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
 * @author reto
 *
 */
public class GraphHandlerTests extends ModelTestBase {
	
	/**
	 * @param name
	 */
	public GraphHandlerTests(String name) {
		super(name);
	}

	//private static final Log log = LogFactory.getLog(GraphHandlerTests.class);
	
	ServerBinding serverBinding = new ServerBinding() {

		public InetAddress getInetAddress() {
			try {
				return InetAddress.getByName("localhost");
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}

		public int getPort() {
			return 8686;
		}
		
	};
	
	/** checks identity of a graph retrieved using model.read()
	 * 
	 * @throws Exception
	 */
	public void testSimpleBody() throws Exception {
		Model model = modelWithStatements("_a dc:subject _b; _a rdfs:comment '���'");
		final Graph body = JenaUtil.getGraphFromModel(model, true);
		WebServer webServer = createServer().startNewWebServer(new GraphHandlerAdaptor(new GraphHandler() {

			public void handle(Request request, TypedResponse<Graph> response) throws HandlerException {
				response.setBody(body);
			}
		}), serverBinding);
		
		try {
			URL serverURL = new URL("http://"+serverBinding.getInetAddress().getHostAddress()+":"+serverBinding.getPort()+"/");
			Model retrievedModel = ModelFactory.createDefaultModel();
			retrievedModel.read(serverURL.toString());
			assertTrue(model.isIsomorphicWith(retrievedModel));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} finally {
			webServer.stop();
		}
	}
	
	/**
	 * @return
	 */
	private WebServerFactory createServer() {
		return WebServerFactory.newInstance();
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

