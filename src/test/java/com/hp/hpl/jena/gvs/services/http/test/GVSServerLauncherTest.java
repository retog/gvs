/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GVSServerLauncherTest.java,v 1.2 2007/06/07 12:00:47 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http.test;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Policy;
import java.util.Collections;

import junit.framework.TestCase;

import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.gvs.impl.util.CannedDataReader;
import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;
import com.hp.hpl.jena.gvs.services.http.ApplicationServerHandler;
import com.hp.hpl.jena.gvs.services.http.GVSServerLauncher;
import com.hp.hpl.jena.gvs.test.StoreTest;

/**
 * @author reto
 *
 */
public class GVSServerLauncherTest extends TestCase {

	public void testFileRetrieval() throws Exception {
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
		Store store = new MemoryStoreImpl();
		URL directory = StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata");
		String baseURLString = "http://"
				+ serverBinding.getInetAddress().getHostName() + ":"
				+ serverBinding.getPort() + "/";
		CannedDataReader.readCannedData(store, directory, baseURLString);
		Source source = new SourceImpl("http://example.org/graph-source");
		NamedNode config = new NamedNodeImpl("http://example.org/config#");
		WebServer webServer = GVSServerLauncher.launchGVSServer(store, serverBinding, null, Collections.singleton(source), config);
		URL serverURL = new URL("http://"+serverBinding.getInetAddress().getHostAddress()+":"+serverBinding.getPort()+"/application/test-dir/test-data");
		InputStream remoteStream = serverURL.openStream();
		InputStream resourceStream = ApplicationServerHandler.class.getResourceAsStream("/META-INF/web/application/test-dir/test-data.txt");
		for (int i = resourceStream.read(); i > -1; i = resourceStream.read()) {
			assertEquals(i, remoteStream.read());
		}
		//same with URL-extension
		serverURL = new URL("http://"+serverBinding.getInetAddress().getHostAddress()+":"+serverBinding.getPort()+"/application/test-dir/test-data.txt");
		remoteStream = serverURL.openStream();
		resourceStream = ApplicationServerHandler.class.getResourceAsStream("/META-INF/web/application/test-dir/test-data.txt");
		for (int i = resourceStream.read(); i > -1; i = resourceStream.read()) {
			assertEquals(i, remoteStream.read());
		}
		webServer.stop();

	}
	@Override
	protected void tearDown() throws Exception {
		//setting back default policy
		Policy.setPolicy(null);
		Policy.setPolicy(new NoRestrictionsPolicy());
		super.tearDown();
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

