/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: CookieCallbackHandler.java,v 1.1 2007/05/12 07:42:25 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.http.security;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.util.Cookie;
import org.wymiwyg.wrhapi.util.EnhancedRequest;

/**
 * @author reto
 *
 */
public class CookieCallbackHandler implements CallbackHandler {

	String userName, password;
	/**
	 * @param request
	 * @throws HandlerException 
	 */
	public CookieCallbackHandler(Request request) throws HandlerException {
		EnhancedRequest ehRequest = new EnhancedRequest(request);
		Cookie[] cookies = ehRequest.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("login")) {
				String value = cookie.getValue();
				StringTokenizer tokens = new StringTokenizer(value,":");
				userName = tokens.nextToken();
				password = tokens.nextToken();

				
			}
		}
		if (userName == null) {
			userName = "";
		}
		if (password == null) {
			password = "";
		}
		
	}

	/**
	 * @see javax.security.auth.callback.CallbackHandler#handle(Callback[])
	 */
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				((NameCallback) callbacks[i]).setName(userName);
			} else if (callbacks[i] instanceof PasswordCallback) {
				((PasswordCallback) callbacks[i]).setPassword(password
						.toCharArray());
			} else {
				throw new UnsupportedCallbackException(callbacks[i],
						"Callback class not supported");
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

