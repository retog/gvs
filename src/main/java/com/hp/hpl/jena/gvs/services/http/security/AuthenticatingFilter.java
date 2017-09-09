/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: AuthenticatingFilter.java,v 1.2 2007/06/07 12:01:09 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.util.Cookie;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

import com.hp.hpl.jena.gvs.security.GVSPrincipal;
import com.hp.hpl.jena.gvs.security.SuperUserGVSPrincipal;

/**
 * @author reto
 *
 */
public class AuthenticatingFilter implements Filter {
	

	private static final Log log = LogFactory.getLog(AuthenticatingFilter.class);
	
	
	public void handle(final Request request, final Response response, final Handler wrapped)
			throws HandlerException {
		if (request.getRequestURI().getPath().equals("/meta/account-manager")) {
			Subject subject = new Subject();
			subject.getPrincipals().add(SuperUserGVSPrincipal.instance);
			try {
				Subject.doAsPrivileged(subject, new PrivilegedExceptionAction() {
					public Object run() throws Exception {
						wrapped.handle(request, response);
						return null;
					}
				}, null);
			} catch (PrivilegedActionException e) {
				throw new RuntimeException(e);
			}
		} else {
			LoginContext loginContext;
			try {
				loginContext = new LoginContext("GVS-Auth", new CookieCallbackHandler(request));
				loginContext.login();
			} catch (LoginException e) {
				/*response.setResponseStatus(ResponseStatus.FORBIDDEN);
				log.info(e);
				response.setBody(new MessageBody2Read() {
	
					public ReadableByteChannel read() throws IOException {
						return Channels.newChannel(new ByteArrayInputStream("Login Failed".getBytes()));
					}
					
				});
				return;*/
				try {
					Cookie loginCookie = new Cookie("login", "-");
					loginCookie.setMaxAge(-1);
					response.setHeader(HeaderName.SET_COOKIE, loginCookie.toString());

					loginContext = new LoginContext("GVS-Auth", new CallbackHandler() {

						public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
							for (int i = 0; i < callbacks.length; i++) {
								if (callbacks[i] instanceof NameCallback) {
									((NameCallback) callbacks[i]).setName("");
								} else if (callbacks[i] instanceof PasswordCallback) {
									((PasswordCallback) callbacks[i]).setPassword(""
											.toCharArray());
								} else {
									throw new UnsupportedCallbackException(callbacks[i],
											"Callback class not supported");
								}

							}
							
						}
					
					});
					loginContext.login();
				} catch (LoginException e1) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e1);
				}
				
			}
			try {
				Subject subject = loginContext.getSubject();
				Subject.doAsPrivileged(subject, new PrivilegedExceptionAction() {
					public Object run() throws Exception {
						wrapped.handle(request, response);
						return null;
					}
				}, null);
			} catch (UnsupportedClassVersionError e) {
				
			} catch (PrivilegedActionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof HandlerException) {
					throw (HandlerException)cause;
				}
				if (cause instanceof RuntimeException) {
					throw (RuntimeException)cause;
				}
				throw new RuntimeException(e);
			} catch (AccessControlException e) {
				response.setResponseStatus(ResponseStatus.FORBIDDEN);
				log.info(e);
				response.setBody(new MessageBody2Read() {
	
					public ReadableByteChannel read() throws IOException {
						return Channels.newChannel(new ByteArrayInputStream("ACCESS DENIED".getBytes()));
					}
					
				});
			}
		}

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

