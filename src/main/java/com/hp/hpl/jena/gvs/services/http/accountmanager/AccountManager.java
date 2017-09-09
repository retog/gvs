/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: AccountManager.java,v 1.3 2007/06/25 11:18:35 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http.accountmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.util.Util;
import org.wymiwyg.commons.vocabulary.FOAF;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.Node;
import org.wymiwyg.rdf.graphs.fgnodes.impl.InverseFunctionalPropertyNodeImpl;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.impl.NodeImpl;
import org.wymiwyg.rdf.graphs.impl.PlainLiteralNodeImpl;
import org.wymiwyg.rdf.graphs.impl.PropertyNodeImpl;
import org.wymiwyg.rdf.graphs.impl.SimpleGraph;
import org.wymiwyg.rdf.graphs.impl.TripleImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.Cookie;
import org.wymiwyg.wrhapi.util.MessageBody2Read;
import org.wymiwyg.wrhapi.util.parameterparser.KeyValuePair;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterUtil;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterValue;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.security.ACCOUNTMANAGER;
import com.hp.hpl.jena.gvs.security.AUTHORIZATION;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler;
import com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author reto
 * 
 */
public class AccountManager implements GraphHandler {

	/**
	 * @author reto
	 * 
	 */
	public static class LoginData {

		private String userName;

		private String passwordSha1;

		private String mboxSha1;

		/**
		 * @param username
		 * @param passwordSha1
		 * @param mboxSha1
		 */
		public LoginData(String userName, String passwordSha1, String mboxSha1) {
			this.userName = userName;
			this.passwordSha1 = passwordSha1;
			this.mboxSha1 = mboxSha1;
		}

		public String getMboxSha1() {
			return mboxSha1;
		}

		public String getPasswordSha1() {
			return passwordSha1;
		}

		public String getUserName() {
			return userName;
		}

	}

	/**
	 * @author reto
	 * 
	 */
	public static class Configuration {

		private String smtpHost = "localhost";

		private String fromAddress = "noreply@gvs";

		/**
		 * @param store
		 * @param configGOT
		 * @param configuration
		 * 
		 */
		public Configuration(Store store, GraphOverTime configGOT,
				NamedNode configuration) {

			Graph configGraph = configGOT.getGraph(new Date());
			Model configModel = JenaUtil.getModelFromGraph(configGraph);
			Resource httpConfigRes = configModel.createResource(configuration
					.getURIRef());
			Statement amConfigStmt = httpConfigRes
					.getProperty(WEBACCOUNTMANAGER.webAccountManagerConfiguration);
			if (amConfigStmt != null) {
				Resource amConfigRes = amConfigStmt.getResource();
				Statement smtpHostStmt = amConfigRes
						.getProperty(WEBACCOUNTMANAGER.smtpServer);
				if (smtpHostStmt != null) {
					smtpHost = smtpHostStmt.getString();
				}
				Statement fromAddressStmt = amConfigRes
						.getProperty(WEBACCOUNTMANAGER.fromAddress);
				if (fromAddressStmt != null) {
					fromAddress = fromAddressStmt.getString();
				}
			}
		}

		public String getSmtpHost() {
			return smtpHost;
		}

		public String getFromAddress() {
			return fromAddress;
		}

	}

	private static final Log log = LogFactory.getLog(AccountManager.class);

	private Store store;

	private LoginLinkSender loginLinkSender;

	private Configuration configuration;

	private Map<String, LoginData> verificationMap = new HashMap<String, LoginData>();

	private Source identity;

	private GraphOverTime configGOT;

	/**
	 * @param store
	 * @param configuration
	 * @param trustedSources
	 * @param identity
	 *            the identity used to save the login-information
	 */
	public AccountManager(Store store, Source identity,
			Set<Source> trustedSources, NamedNode configurationNode) {
		this.store = store;
		configGOT = store.getGraphOverTime(trustedSources);
		this.configuration = new Configuration(store, configGOT,
				configurationNode);
		this.loginLinkSender = new LoginLinkSender(configuration);
		this.identity = identity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandler#handle(org.wymiwyg.wrhapi.Request,
	 *      com.hp.hpl.jena.gvs.services.http.graphserver.TypedResponse)
	 */
	public void handle(Request request, TypedResponse<Graph> response)
			throws HandlerException {
		Map<String, String> parameterMap = new HashMap<String, String>();
		Iterator<KeyValuePair<ParameterValue>> parameters = ParameterUtil.getBodyPameters(request).iterator();
		while (parameters.hasNext()) {
			KeyValuePair<ParameterValue> currentPair = parameters.next();
			parameterMap.put(currentPair.getKey(), currentPair.getValue().toString());
		}
		String username = parameterMap.get("username");
		String password = parameterMap.get("password");
		if (parameterMap.containsKey("email")) {
			String email = parameterMap.get("email");
			String passwordSha1 = Util.sha1(password);
			String mboxSha1 = Util.sha1("mbox" + email);
			Resource user = getUserByUsername(username);
			if (user != null) {
				if (!user.hasProperty(FOAF.mbox_sha1sum, mboxSha1)) {
					throw new HandlerException(
							"user already exists with a differen email address");
				} else {
					log
							.info("Username/Email combination exist, will delete exitisting on confirmation");
				}
			}

			LoginData loginData = new LoginData(username, passwordSha1,
					mboxSha1);
			String verificationKey = Util.createRandomString(28);
			verificationMap.put(verificationKey, loginData);
			log.info("sending login link to " + email);
			loginLinkSender.sendLoginLink(email, getVerificationLink(request,
					verificationKey));
			response.setResponseStatus(ResponseStatus.MOVED_TEMPORARILY);
			response.setHeader(HeaderName.LOCATION,
					"/application/verification-sent");
		} else {
			String[] verificationParameters = request.getRequestURI()
					.getParameterValues("verification");
			if (verificationParameters != null) {
				if (verificationParameters.length == 1) {
					LoginData loginData = verificationMap
							.get(verificationParameters[0]);
					if (loginData == null) {
						throw new HandlerException(
								"Verification string not found, probably expired");
					}
					Graph graph = new SimpleGraph();
					addUser(loginData, graph, request);
					response
							.setDefaultStylesheet("/application/stylesheets/verification-result");
					response.setBody(graph);
					Resource user = getUserByUsernameInIdentityGOT(loginData.userName);
					FCAGraph revokeGraphTmp = null;
					if (user != null) {
						log
								.info("Username already exist, removing previous password");
						Model revokeModel = ModelFactory.createDefaultModel();
						revokeModel.add(user
								.listProperties(ACCOUNTMANAGER.passwordSha1));
						revokeModel.add(user.listProperties(FOAF.mbox_sha1sum));
						revokeModel.add(user
								.listProperties(ACCOUNTMANAGER.userName));
						revokeGraphTmp = new FCAGraphImpl(revokeModel);
						// store.revokeGraph(identity, new
						// FCAGraphImpl(revokeModel), now);
					}
					final FCAGraph revokeGraph = revokeGraphTmp;
					final FCAGraph assertGraph = new FCAGraphImpl(graph);
					// store.assertGraph(identity, new FCAGraphImpl(graph),
					// now);
					store.perform(identity, new StoreTransaction() {

						public void execute(SourceStoreView storeView) {
							if (revokeGraph != null)
								storeView.revokeGraph(revokeGraph);
							storeView.assertGraph(assertGraph);
						}

					});
				} else {
					throw new HandlerException(
							"Invalid request: needs exactly one verification-parameter");
				}
			} else {
				// login
				if (!loginValid(username, password)) {
					response.setResponseStatus(ResponseStatus.FORBIDDEN);
					response.setBody(new MessageBody2Read() {

						public ReadableByteChannel read() throws IOException {
							return Channels
									.newChannel(new ByteArrayInputStream(
											"LOGIN INVALID".getBytes()));
						}

					});
				}
				Cookie loginCookie = new Cookie("login", username + ":"
						+ password);
				response.setHeader(HeaderName.SET_COOKIE, loginCookie
						.toString());
				response.setResponseStatus(ResponseStatus.MOVED_TEMPORARILY);
				response.setHeader(HeaderName.LOCATION,
						"/application/gvs-browser");
				// response.setBody((MessageBody)null);
			}
		}

	}

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean loginValid(String username, String password) {
		Resource user = getUserByUsername(username);
		if (user == null) {
			return false;
		}
		return user.hasProperty(ACCOUNTMANAGER.passwordSha1, Util
				.sha1(password));
	}

	/**
	 * @param username
	 * @return
	 */
	private Resource getUserByUsername(String username) {
		Graph graph = configGOT.getGraph(new Date());
		Model model = JenaUtil.getModelFromGraph(graph);
		ResIterator subjectIter = model.listSubjectsWithProperty(
				ACCOUNTMANAGER.userName, username);
		if (!subjectIter.hasNext()) {
			return null;
		}
		Resource subject = subjectIter.nextResource();
		if (subjectIter.hasNext()) {
			throw new RuntimeException("Username ambiguos");
		}
		return subject;
	}

	private Resource getUserByUsernameInIdentityGOT(String username) {
		Graph graph = store.getGraphOverTime(Collections.singleton(identity))
				.getGraph(new Date());
		Model model = JenaUtil.getModelFromGraph(graph);
		ResIterator subjectIter = model.listSubjectsWithProperty(
				ACCOUNTMANAGER.userName, username);
		if (!subjectIter.hasNext()) {
			return null;
		}
		Resource subject = subjectIter.nextResource();
		if (subjectIter.hasNext()) {
			throw new RuntimeException("Username ambiguos");
		}
		return subject;
	}

	/**
	 * @param loginData
	 * @param graph
	 * @throws HandlerException
	 */
	private void addUser(LoginData loginData, Graph graph, Request request)
			throws HandlerException {
		// TODO use an jenaModelWrapper (TBD)
		Node user = new NodeImpl();
		graph.add(new TripleImpl(user, new PropertyNodeImpl(RDF.type.getURI()),
				new NamedNodeImpl(FOAF.Agent.getURI())));
		graph.add(new TripleImpl(user, new InverseFunctionalPropertyNodeImpl(
				ACCOUNTMANAGER.userName.getURI()), new PlainLiteralNodeImpl(
				loginData.userName)));
		graph.add(new TripleImpl(user, new PropertyNodeImpl(
				ACCOUNTMANAGER.passwordSha1.getURI()),
				new PlainLiteralNodeImpl(loginData.passwordSha1)));
		graph.add(new TripleImpl(user, new InverseFunctionalPropertyNodeImpl(
				FOAF.mbox_sha1sum.getURI()), new PlainLiteralNodeImpl(
				loginData.mboxSha1)));
		graph.add(new TripleImpl(user, new PropertyNodeImpl(
				AUTHORIZATION.mayImpersonate.getURI()), new NamedNodeImpl(
				getUserSource(loginData.userName, request))));

	}

	/**
	 * @param userName
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private String getUserSource(String userName, Request request)
			throws HandlerException {
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append(request.getScheme().getStringRepresentation());
		resultBuffer.append("://");
		resultBuffer.append(request.getHeaderValues(HeaderName.HOST)[0]);

		resultBuffer.append("/user/");
		resultBuffer.append(userName);
		// resultBuffer.append('#');
		String result = resultBuffer.toString();
		Model creationModel = ModelFactory.createDefaultModel();
		creationModel.createResource(result).addProperty(RDFS.comment,
				"login Link sent at " + new Date());
		store.assertGraph(new SourceImpl(result), new FCAGraphImpl(
				creationModel));
		return result;
	}

	/**
	 * @param request
	 * @param verificationKey
	 * @return
	 * @throws HandlerException
	 */
	private String getVerificationLink(Request request, String verificationKey)
			throws HandlerException {
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append(request.getScheme().getStringRepresentation());
		resultBuffer.append("://");
		resultBuffer.append(request.getHeaderValues(HeaderName.HOST)[0]);
		resultBuffer.append("/meta/account-manager?verification=");
		resultBuffer.append(verificationKey);
		return resultBuffer.toString();
	}

	private String getHost(Request request) throws HandlerException {
		String host;
		try {
			host = request.getHeaderValues(HeaderName.HOST)[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new HandlerException("No host header");
		}

		int colonPos = host.indexOf(':');

		if (colonPos != -1) {
			host = host.substring(0, colonPos);
		}

		return host;
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

