/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: SecurityTest.java,v 1.2 2007/06/07 12:00:47 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http.test;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Policy;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.login.Configuration;

import junit.framework.TestCase;

import org.wymiwyg.commons.util.Util;
import org.wymiwyg.commons.vocabulary.FOAF;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.utils.jena.LeanDiffPatch;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.gvs.security.ACCOUNTMANAGER;
import com.hp.hpl.jena.gvs.security.AUTHORIZATION;
import com.hp.hpl.jena.gvs.security.DefaultPolicy;
import com.hp.hpl.jena.gvs.security.GVSConfiguration;
import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;
import com.hp.hpl.jena.gvs.services.http.GVSServerLauncher;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author reto
 * 
 */
public class SecurityTest extends TestCase {

	public void testUnauthorizedPut() throws Exception {
		Store store = new MemoryStoreImpl();
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

		Source identity =new SourceImpl("http://example.org/tests/identity");
		Set<Source> trustedSources  = new HashSet<Source>();
		NamedNode configuration = new NamedNodeImpl("http://example.org/tests/identity");;
		WebServer webServer = GVSServerLauncher.launchGVSServer(store, serverBinding, identity ,
				trustedSources, configuration);

		Model model = ModelFactory.createDefaultModel();
		model.read(getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
				.openStream(), getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
				.toString());
		URL serverURL = new URL("http://"
				+ serverBinding.getInetAddress().getHostName() + ":"
				+ serverBinding.getPort() + "/danbri");
		HttpURLConnection connection = (HttpURLConnection) serverURL
				.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		OutputStream out = connection.getOutputStream();
		model.write(out);
		int responseCode = connection.getResponseCode();
		assertEquals(403, responseCode);
		webServer.stop();

	}
	
	public void testAuthorizedPut() throws Exception {
		//testing
		GraphOverTime trustedGOT = new MemoryStoreImpl().getGraphOverTime(new HashSet<Source>());
		Policy orig = Policy.getPolicy();
		Policy.setPolicy(new DefaultPolicy(trustedGOT));
		//Policy.setPolicy(orig);
		Policy.setPolicy(null);
		Policy.setPolicy(new NoRestrictionsPolicy());

		//
		Store store = new MemoryStoreImpl();
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
		URL sourceURL = new URL("http://"
				+ serverBinding.getInetAddress().getHostName() + ":"
				+ serverBinding.getPort() + "/danbri");
		Source identity = new SourceImpl("http://example.org/tests/identity");
		Set<Source> trustedSources  = new HashSet<Source>();
		trustedSources.add(identity);
		NamedNode configuration = new NamedNodeImpl("http://example.org/tests/configuration#");
		Model authorizationModel = ModelFactory.createDefaultModel();
		Resource user = authorizationModel.createResource(FOAF.Agent);
		user.addProperty(ACCOUNTMANAGER.userName, "beta");
		user.addProperty(ACCOUNTMANAGER.passwordSha1, Util.sha1("tester"));
		user.addProperty(AUTHORIZATION.mayImpersonate, authorizationModel.createResource(sourceURL.toString()));
		store.assertGraph(identity, new FCAGraphImpl(authorizationModel));
		WebServer webServer = GVSServerLauncher.launchGVSServer(store, serverBinding, identity ,
				trustedSources, configuration);

		Model model = ModelFactory.createDefaultModel();
		model.read(getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
				.openStream(), getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
				.toString());
		model = LeanDiffPatch.leanify(model);
		HttpURLConnection connection = (HttpURLConnection) sourceURL
				.openConnection();
		connection.addRequestProperty("Cookie", "login=beta:tester");
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		OutputStream out = connection.getOutputStream();
		model.write(out);
		int responseCode = connection.getResponseCode();
		assertEquals(200, responseCode);
		connection.disconnect();
		Model retrievedModel = ModelFactory.createDefaultModel();
		retrievedModel.read(sourceURL.toString());
		assertTrue("Retrieved model isomorphic to equals", model.isIsomorphicWith(retrievedModel));
		webServer.stop();

	}
	
	public void testWrongPasswordPut() throws Exception {
		Store store = new MemoryStoreImpl();
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
		URL sourceURL = new URL("http://"
				+ serverBinding.getInetAddress().getHostName() + ":"
				+ serverBinding.getPort() + "/danbri");
		Source identity = new SourceImpl("http://example.org/tests/identity");
		Set<Source> trustedSources  = new HashSet<Source>();
		trustedSources.add(identity);
		NamedNode configuration = new NamedNodeImpl("http://example.org/tests/configuration#");
		Model authorizationModel = ModelFactory.createDefaultModel();
		Resource user = authorizationModel.createResource(FOAF.Agent);
		user.addProperty(ACCOUNTMANAGER.userName, "beta");
		user.addProperty(ACCOUNTMANAGER.passwordSha1, Util.sha1("tester"));
		user.addProperty(AUTHORIZATION.mayImpersonate, authorizationModel.createResource(sourceURL.toString()));
		store.assertGraph(identity, new FCAGraphImpl(authorizationModel));
		WebServer webServer = GVSServerLauncher.launchGVSServer(store, serverBinding, identity ,
				trustedSources, configuration);

		Model model = ModelFactory.createDefaultModel();
		model.read(getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
				.openStream(), getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
				.toString());
		model = LeanDiffPatch.leanify(model);
		HttpURLConnection connection = (HttpURLConnection) sourceURL
				.openConnection();
		connection.addRequestProperty("Cookie", "login=beta:wrong");
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		OutputStream out = connection.getOutputStream();
		model.write(out);
		int responseCode = connection.getResponseCode();
		assertEquals(403, responseCode);
		connection.disconnect();
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

