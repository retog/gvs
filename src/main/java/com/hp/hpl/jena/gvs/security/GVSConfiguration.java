/*
 * ====================================================================
 *
 * The WYMIWYG Software License, Version 1.0
 *
 * Copyright (c) 2002-2003 WYMIWYG  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by WYMIWYG."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name "WYMIWYG" or "WYMIWYG.org" must not be used to endorse 
 *    or promote products derived from this software without prior written 
 *    permission. For written permission, please contact wymiwyg@wymiwyg.org.
 *
 * 5. Products derived from this software may not be called  
 *    "WYMIWYG" nor may "WYMIWYG" appear in their names 
 *    without prior written permission of WYMIWYG.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL WYMIWYG OR ITS CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of WYMIWYG.  For more
 * information on WYMIWYG, please see http://www.WYMIWYG.org/.
 *
 * This licensed is based on The Apache Software License, Version 1.1,
 * see http://www.apache.org/.
 */

package com.hp.hpl.jena.gvs.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.gvs.GraphOverTime;

/** The GVSLoginModule cannot currently be configured through policy-files as it 
 * requires a reference to a Store instance.
 * 
 * @author reto
 */
public class GVSConfiguration extends Configuration {

	private Configuration previous;
	private GraphOverTime configGOT;
	private String configurationApplication;
	private static Log logger = LogFactory.getLog(GVSConfiguration.class);

	/**
	 * Constructor for RConfiguration.
	 */
	public GVSConfiguration(GraphOverTime configGOT, String configurationApplication) {
		this.configurationApplication = configurationApplication;
		if (configGOT == null) {
			throw new NullPointerException("configGOT may not be null");
		}
		this.configGOT = configGOT;
		try {
			previous = Configuration.getConfiguration();		
		} catch (Exception ex) {
		}
		logger.debug("Constructed RConfiguration");
	}

	/**
	 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(String)
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry(String application) {
		if (application.equals(configurationApplication)) {
			AppConfigurationEntry[] result = new AppConfigurationEntry[1];
			Map<String, GraphOverTime> options = new HashMap<String, GraphOverTime>(1);
			options.put("configGOT", configGOT);
			if (logger.isDebugEnabled()) {
				logger.debug("Set model");// to " + model);
			}
			result[0] =
				new AppConfigurationEntry(
					GVSLoginModule.class.getName(),
					AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
					options);
			if (logger.isDebugEnabled()) {
				logger.debug("returning AppConfigurationEntry: " + result);
			}
			return result;
		} else {
			if (previous == null) {
				throw new RuntimeException("no aqpplication configurstaion for "+application);
			}
			return previous.getAppConfigurationEntry(application);
		}
	}

	/**
	 * @see javax.security.auth.login.Configuration#refresh()
	 */
	public void refresh() {
		if (previous != null) {
			previous.refresh();
		}
	}

}

