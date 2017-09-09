/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: GVSGraphHandlerTest.java,v 1.3 2007/06/07 12:00:47 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http.test;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.Policy;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.rdf.utils.jena.LeanDiffPatch;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;

import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.gvs.impl.util.CannedDataReader;
import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;
import com.hp.hpl.jena.gvs.services.http.GVSGraphHandler;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandlerAdaptor;
import com.hp.hpl.jena.gvs.test.StoreTest;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class GVSGraphHandlerTest extends TestCase {
	private static final Log log = LogFactory.getLog(GVSGraphHandlerTest.class);

	/**
	 * @author reto
	 * 
	 */
	public interface RetrievalTest {

		/**
		 * @param serverBinding
		 * @throws Exception
		 */
		void performTest(ServerBinding serverBinding) throws Exception;

	}

	public void testSingleLatest() throws Exception {
		testWithCannedData(new RetrievalTest() {

			public void performTest(ServerBinding serverBinding)
					throws Exception {
				Model model = ModelFactory.createDefaultModel();
				model.read(StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
						.openStream(), StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
						.toString());
				URL serverURL = new URL("http://"
						+ serverBinding.getInetAddress().getHostName() + ":"
						+ serverBinding.getPort() + "/danbri");
				Model retrievedModel = ModelFactory.createDefaultModel();
				retrievedModel.read(serverURL.toString());
				// retrievedModel = LeanDiffPatch.leanify(retrievedModel);
				model = LeanDiffPatch.leanify(model);
				log.info(model.size());
				log.info(retrievedModel.size());
				log.info(LeanDiffPatch.getDiff(model, retrievedModel));
				assertTrue(model.isIsomorphicWith(retrievedModel));

			}

		});
	}
	


	public void testSinglePrevious() throws Exception {
		testWithCannedData(new RetrievalTest() {

			public void performTest(ServerBinding serverBinding)
					throws Exception {
				Model model = ModelFactory.createDefaultModel();
				model.read(StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-19980920123600000.rdf")
						.openStream(), StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-19980920123600000.rdf")
						.toString());
				URL serverURL = new URL("http://"
						+ serverBinding.getInetAddress().getHostName() + ":"
						+ serverBinding.getPort() + "/danbri?moment=20040302");
				Model retrievedModel = ModelFactory.createDefaultModel();
				retrievedModel.read(serverURL.toString());
				// retrievedModel = LeanDiffPatch.leanify(retrievedModel);
				model = LeanDiffPatch.leanify(model);
				assertTrue(model.isIsomorphicWith(retrievedModel));

			}

		});
	}

	public void testMultipleLatest() throws Exception {
		testWithCannedData(new RetrievalTest() {

			public void performTest(ServerBinding serverBinding)
					throws Exception {
				Model model = ModelFactory.createDefaultModel();
				model.read(StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
						.openStream(), StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/danbri-20060920123618661.rdf")
						.toString());
				model.read(StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/nadia-20060919113256000.rdf")
						.openStream(), StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata/nadia-20060919113256000.rdf")
						.toString());
				String danbriSourceURLString = "http://"
						+ serverBinding.getInetAddress().getHostName() + ":"
						+ serverBinding.getPort() + "/danbri";
				String naidaSourceURLString = "http://"
						+ serverBinding.getInetAddress().getHostName() + ":"
						+ serverBinding.getPort() + "/nadia";
				String serviceBaseURLString = "http://"
						+ serverBinding.getInetAddress().getHostName() + ":"
						+ serverBinding.getPort() + "/gvs?";
				URL serviceURL = new URL(serviceBaseURLString + "source="
						+ URLEncoder.encode(danbriSourceURLString, "utf-8")
						+ "&source="
						+ URLEncoder.encode(naidaSourceURLString, "utf-8"));
				Model retrievedModel = ModelFactory.createDefaultModel();
				retrievedModel.read(serviceURL.toString());
				retrievedModel = LeanDiffPatch.leanify(retrievedModel);
				model = LeanDiffPatch.leanify(model);
				assertTrue(model.isIsomorphicWith(retrievedModel));

			}

		});
	}

	private void testWithCannedData(RetrievalTest retrievalTest)
			throws Exception {
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
		WebServerFactory webServerFactory = WebServerFactory.newInstance();
		
		WebServer webServer = webServerFactory.startNewWebServer(
				new GraphHandlerAdaptor(new GVSGraphHandler(store, null)),
				serverBinding);
		try {
			retrievalTest.performTest(serverBinding);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} finally {
			webServer.stop();
		}
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

