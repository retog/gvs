/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GVSLoginModule.java,v 1.2 2007/02/09 11:43:55 rebach Exp $
*/
package com.hp.hpl.jena.gvs.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.util.Util;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author reto
 *
 */
public class GVSLoginModule implements LoginModule {

	private Subject subject;
	private CallbackHandler callbackHandler;
	private static Log logger = LogFactory.getLog(GVSLoginModule.class);
	private boolean success = false;
	private ArrayList<GVSPrincipal> tempPrincipals = new ArrayList<GVSPrincipal>();
	private ArrayList<Object> tempCredentials = new ArrayList<Object>();
	GraphOverTime configGOT;
	private Resource anonUserResource;
	{
		anonUserResource = ModelFactory.createDefaultModel().createResource("http://gvs.hpl.hp.com/anonymous-user#");
	}
	private GVSPrincipal anonymousUserPrincipal = new GVSPrincipal("", anonUserResource);

	/**
	 * @see javax.security.auth.spi.LoginModule#initialize(Subject, CallbackHandler, Map, Map)
	 */
	public void initialize(
		Subject subject,
		CallbackHandler callbackHandler,
		Map sharedState,
		Map options) {
		logger.debug("initializing");
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		configGOT =
				(GraphOverTime) options.get("configGOT");
		if (configGOT == null) {
			logger.error("attempt to intialize without model", new RuntimeException("attempt to intialize without model"));
			throw new RuntimeException("attempt to intialize without model");
		}
		
	}

	/**
	 * @see javax.security.auth.spi.LoginModule#login()
	 */
	public boolean login() throws LoginException {
		logger.debug("logging in");
		if (callbackHandler == null)
			throw new LoginException(
				"Error: no CallbackHandler available "
					+ "to garner authentication information from the user");
		// Setup default callback handlers.
		Callback[] callbacks =
			new Callback[] {
				new NameCallback("Username: "),
				new PasswordCallback("Password: ", false)};

		try {
			callbackHandler.handle(callbacks);
		} catch (IOException e) {
			throw new LoginException("exception calling back: " + e.toString());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException("exception calling back: " + e.toString());
		}

		String username = ((NameCallback) callbacks[0]).getName();
		String password =
			new String(((PasswordCallback) callbacks[1]).getPassword());
		if (username.equals("")) {
			//succesfull login of anoynmous user
			success = true;
			tempPrincipals.add(anonymousUserPrincipal);
			return true;
		}
		//get userInfo and check pwd
		try {
			Resource userResource = getUserResource(username);
			if (userResource == null) {
				if (logger.isInfoEnabled()) {
					logger.info("No user-resource with shortname "+username+" found");
				}
				throw new LoginException("Authentication failed: Password does not match");
			}
			success =
				userResource
					.getProperty(ACCOUNTMANAGER.passwordSha1)
					.getLiteral()
					.getString()
					.equals(
					Util.sha1(password));

			if (!success)
				throw new LoginException("Authentication failed: Password does not match");
			GVSPrincipal newPrincipal = new GVSPrincipal(username, userResource);
			tempPrincipals.add(newPrincipal);
		} catch (RuntimeException ex) {
			logger.error("error handling user info", ex);
			throw ex;
		}
		return true;
	}


	/**
	 * @param username
	 * @return
	 */
	private Resource getUserResource(String username) {
		Graph graph  = configGOT.getGraph(new Date());
		Model model = JenaUtil.getModelFromGraph(graph);
		ResIterator resIter = model.listSubjectsWithProperty(ACCOUNTMANAGER.userName, username);
		if (resIter.hasNext()) {
			return resIter.nextResource();
		} else {
			return null;
		}
		
	}

	/**
	 * @see javax.security.auth.spi.LoginModule#abort()
	 */
	public boolean abort() throws LoginException {
		logger.debug("aborting");
		logout();
		return true;
	}

	/**
	 * @see javax.security.auth.spi.LoginModule#commit()
	 */
	public boolean commit() throws LoginException {
		logger.debug("committing");
		if (success) {
			if (subject.isReadOnly()) {
				throw new LoginException("Subject is Readonly");
			}
			subject.getPrincipals().addAll(tempPrincipals);
			subject.getPublicCredentials().addAll(tempCredentials);
			tempPrincipals.clear();
			tempCredentials.clear();
		} else {
			tempPrincipals.clear();
			tempCredentials.clear();
		}
		return true;
	}

	/**
	 * @see javax.security.auth.spi.LoginModule#logout()
	 */
	public boolean logout() throws LoginException {
		subject.getPrincipals().clear();
		subject.getPublicCredentials().clear();
		return true;
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

