/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: SecurityTest.java,v 1.4 2007/06/07 12:01:17 rebach Exp $
*/
package com.hp.hpl.jena.gvs.test;

import java.io.IOException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.wymiwyg.commons.util.Util;
import org.wymiwyg.commons.vocabulary.FOAF;
import org.wymiwyg.rdf.graphs.impl.SimpleGraph;

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
import com.hp.hpl.jena.gvs.security.GVSImpersonatePermission;
import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author reto
 *
 */
public class SecurityTest extends TestCase {
	
	private final String userSourceURL = "http://example.org/users/beta#";
	private final String username = "beta";
	private final String password = "test";


	
	public void testLoginAndPermission() {
		initControl();
		
		LoginContext loginContext;
		try {
			loginContext = new LoginContext("gvs", new CallbackHandler() {

				public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
					for (Callback callback : callbacks) {
						if (callback instanceof NameCallback) {
							((NameCallback) callback).setName(username);
						} else if (callback instanceof PasswordCallback) {
							((PasswordCallback) callback).setPassword(password
									.toCharArray());
						} else {
							throw new UnsupportedCallbackException(callback,
									"Callback class not supported");
						}
					}
				}
				
			});
			loginContext.login();
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
		assertTrue(true); //made it till here
		Subject subject = loginContext.getSubject();
		Subject.doAsPrivileged(subject, new PrivilegedAction<Object>() {

			public Object run() {
				AccessController.checkPermission(new GVSImpersonatePermission(new SourceImpl(userSourceURL)));
				return null;
			}
			
		}, null);
		assertTrue(true); //made it till here
		final boolean[] exceptionCatched = new boolean[1];
		Subject.doAsPrivileged(subject, new PrivilegedAction<Object>() {
			public Object run() {
				try {
					AccessController.checkPermission(new GVSImpersonatePermission(new SourceImpl("http://topsecret/")));
				} catch (AccessControlException e) {
					exceptionCatched[0] = true;
				}
				return null;
			}
			
		}, null);
		
		assertTrue(exceptionCatched[0]);
	}
	
	public void testWrongLogin() {
		initControl();
		final boolean[] exceptionCatched = new boolean[1];
		LoginContext loginContext;
		try {
			loginContext = new LoginContext("gvs", new CallbackHandler() {

				public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
					for (Callback callback : callbacks) {
						if (callback instanceof NameCallback) {
							((NameCallback) callback).setName(username);
						} else if (callback instanceof PasswordCallback) {
							((PasswordCallback) callback).setPassword((password+"wrong")
									.toCharArray());
						} else {
							throw new UnsupportedCallbackException(callback,
									"Callback class not supported");
						}
					}
				}
				
			});
			loginContext.login();
		} catch (LoginException e) {
			exceptionCatched[0] = true;
		} 
		assertTrue(exceptionCatched[0]);
		
	}

	
	/**
	 * 
	 */
	private void initControl() {
		Model authenticationModel = createAuthenticationModel();
		
		Store store = new MemoryStoreImpl();
		Source authenticationSource = new SourceImpl("http://localhost/authentication-master#");
		store.assertGraph(authenticationSource, new FCAGraphImpl(authenticationModel));
		
		Model authorizationModel = createAuthorizationModel();
		Source authorizationSource = new SourceImpl("http://localhost/authorization-master#");
		store.assertGraph(authorizationSource, new FCAGraphImpl(authorizationModel));
		
		Set<Source> sources = new HashSet<Source>();
		sources.add(authenticationSource);
		sources.add(authorizationSource);
		GraphOverTime configGOT = store.getGraphOverTime(sources );
		//JenaUtil.getModelFromGraph(configGOT.getGraph(new Date())).write(System.out);
		Configuration.setConfiguration(new GVSConfiguration(configGOT,
				"gvs"));
		Policy.setPolicy(new DefaultPolicy(configGOT));
	}

	/**
	 * @return
	 */
	private Model createAuthenticationModel() {
		Model userModel = ModelFactory.createDefaultModel();
		Resource userBeta = userModel.createResource(FOAF.Person);
		userBeta.addProperty(ACCOUNTMANAGER.userName, username);
		userBeta.addProperty(ACCOUNTMANAGER.passwordSha1, Util.sha1(password));
		return userModel;
	}
	
	private Model createAuthorizationModel() {
		Model userModel = ModelFactory.createDefaultModel();
		Resource userBeta = userModel.createResource(FOAF.Person);
		Resource userSource = userModel.createResource(userSourceURL);
		userBeta.addProperty(AUTHORIZATION.mayImpersonate, userSource);
		userBeta.addProperty(ACCOUNTMANAGER.userName, username);
		return userModel;
	}

	/**
	 * Things to be solved: StackOverflow when SecurityManager is active and 
	 * the SecurityConstants.DO_AS_PRIVILEGED_PERMISSION the user still has. 
	 *
	 */
	public void notYetTestChangeUser() {
		final boolean[] exceptionCatched = new boolean[1];
		final Store store = new MemoryStoreImpl();
		Subject subject = new Subject();
		System.setSecurityManager(new SecurityManager());
		try {
			Subject.doAsPrivileged(subject, new PrivilegedExceptionAction() {
				public Object run() throws Exception {
					Source source = new SourceImpl("http://example.org/graph-source");
					Subject subject2 = new Subject();
					try {
						Subject.doAsPrivileged(null, new PrivilegedExceptionAction() {
							public Object run() throws Exception {
								Source source = new SourceImpl("http://example.org/graph-source");
								try {
									store.assertGraph(source, new FCAGraphImpl(new SimpleGraph()));
								} catch (AccessControlException ex) {
									exceptionCatched[0] = true;
								}
								return null;
							}
						}, null);
					} catch (PrivilegedActionException e) {
						throw new RuntimeException(e);
					}
					return null;
				}
			}, null);
		} catch (PrivilegedActionException e) {
			throw new RuntimeException(e);
		}
		assertTrue(exceptionCatched[0]);	
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

