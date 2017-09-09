/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: ApplicationServerHandlerTest.java,v 1.4 2007/06/25 11:18:42 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http.test;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.Policy;

import junit.framework.TestCase;

import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;

import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;
import com.hp.hpl.jena.gvs.services.http.ApplicationServerHandler;
import com.hp.hpl.jena.gvs.services.http.GVSServerLauncher;

/**
 * @author reto
 * 
 */
public class ApplicationServerHandlerTest extends TestCase {

	public void testFileRetrieval() throws Exception {
		WebServerFactory webServerFactory = WebServerFactory.newInstance();
		ServerBinding serverBinding = new ServerBinding() {

			final int port = PortFinder.getFreePort(getInetAddress());
			
			public InetAddress getInetAddress() {
				try {
					return InetAddress.getByName("localhost");
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			}

			public int getPort() {
				return port;
			}

		};
		URL basePathURL = GVSServerLauncher.class
				.getResource("/META-INF/web/application/");
		PathNode rootNode = PathNodeFactory.getPathNode(basePathURL);
		WebServer webServer = webServerFactory.startNewWebServer(
				new ApplicationServerHandler(rootNode, "/application"),
				serverBinding);
		try {
			URL serverURL = new URL("http://"
					+ serverBinding.getInetAddress().getHostAddress() + ":"
					+ serverBinding.getPort()
					+ "/application/test-dir/test-data");
			InputStream remoteStream = serverURL.openStream();
			InputStream resourceStream = ApplicationServerHandler.class
					.getResourceAsStream("/META-INF/web/application/test-dir/test-data.txt");
			for (int i = resourceStream.read(); i < -1; i = resourceStream
					.read()) {
				assertEquals(i, remoteStream.read());
			}
			// same with URL-extension
			serverURL = new URL("http://"
					+ serverBinding.getInetAddress().getHostAddress() + ":"
					+ serverBinding.getPort()
					+ "/application/test-dir/test-data.txt");
			remoteStream = serverURL.openStream();
			resourceStream = ApplicationServerHandler.class
					.getResourceAsStream("/META-INF/web/application/test-dir/test-data.txt");
			for (int i = resourceStream.read(); i > -1; i = resourceStream
					.read()) {
				assertEquals(i, remoteStream.read());
			}
		} finally {
			webServer.stop();
		}

	}

	public void testFileRetrievalWithQuery() throws Exception {
		WebServerFactory webServerFactory = WebServerFactory.newInstance();
		ServerBinding serverBinding = new ServerBinding() {

			final int port = PortFinder.getFreePort(getInetAddress());
			
			public InetAddress getInetAddress() {
				try {
					return InetAddress.getByName("localhost");
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			}

			public int getPort() {
				return port;
			}

		};
		URL basePathURL = GVSServerLauncher.class
				.getResource("/META-INF/web/application/");
		PathNode rootNode = PathNodeFactory.getPathNode(basePathURL);
		WebServer webServer = webServerFactory.startNewWebServer(
				new ApplicationServerHandler(rootNode, "/application"),
				serverBinding);
		try {
			URL serverURL = new URL("http://"
					+ serverBinding.getInetAddress().getHostAddress() + ":"
					+ serverBinding.getPort()
					+ "/application/test-dir/test-data?bar.html");
			InputStream remoteStream = serverURL.openStream();
			InputStream resourceStream = ApplicationServerHandler.class
					.getResourceAsStream("/META-INF/web/application/test-dir/test-data.txt");
			for (int i = resourceStream.read(); i < -1; i = resourceStream
					.read()) {
				assertEquals(i, remoteStream.read());
			}
			// same with URL-extension
			serverURL = new URL("http://"
					+ serverBinding.getInetAddress().getHostAddress() + ":"
					+ serverBinding.getPort()
					+ "/application/test-dir/test-data.txt?bar.html");
			remoteStream = serverURL.openStream();
			resourceStream = ApplicationServerHandler.class
					.getResourceAsStream("/META-INF/web/application/test-dir/test-data.txt");
			for (int i = resourceStream.read(); i > -1; i = resourceStream
					.read()) {
				assertEquals(i, remoteStream.read());
			}
		} finally {
			webServer.stop();
		}

	}

	public void testContentTypeResponseHeader() throws Exception {
		WebServerFactory webServerFactory = WebServerFactory.newInstance();
		ServerBinding serverBinding = new ServerBinding() {

			final int port = PortFinder.getFreePort(getInetAddress());
			
			public InetAddress getInetAddress() {
				try {
					return InetAddress.getByName("localhost");
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			}

			public int getPort() {
				return port;
			}

		};
		URL basePathURL = GVSServerLauncher.class
				.getResource("/META-INF/web/application/");
		PathNode rootNode = PathNodeFactory.getPathNode(basePathURL);
		WebServer webServer = webServerFactory.startNewWebServer(
				new ApplicationServerHandler(rootNode, "/application"),
				serverBinding);
		URL serverURL = new URL("http://"
				+ serverBinding.getInetAddress().getHostAddress() + ":"
				+ serverBinding.getPort() + "/application/test-dir/test-data");
		URLConnection connection = serverURL.openConnection();
		String contentType = connection.getHeaderField("Content-Type");
		assertEquals("text/plain", contentType);
		webServer.stop();

	}

	@Override
	protected void tearDown() throws Exception {
		// setting back default policy
		Policy.setPolicy(null);
		Policy.setPolicy(new NoRestrictionsPolicy());
		super.tearDown();
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

