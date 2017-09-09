/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: HTTPListenerService.java,v 1.1 2007/05/12 07:41:44 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.wrhapi.ServerBinding;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.services.http.GVSServerLauncher;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author reto
 *
 */
public class HTTPListenerService implements Service {

	private Store store;
	private NamedNode configuration;
	private Set<Source> trustedSources;
	private Source identity;

	/**
	 * @param store
	 * @param identity 
	 * @param trustedSources 
	 * @param configuration 
	 */
	public HTTPListenerService(Store store, Source identity, Set<Source> trustedSources, NamedNode configuration) {
		this.store = store;
		this.identity = identity;
		this.configuration = configuration;
		this.trustedSources = trustedSources;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.services.Service#start()
	 */
	public void start() {
		Graph trustedGraph = store.getGraphOverTime(trustedSources).getGraph(new Date());
		Model trustedModel = JenaUtil.getModelFromGraph(trustedGraph);
		final Resource configRes = trustedModel.getResource(configuration.getURIRef());
		try {
			GVSServerLauncher.launchGVSServer(store, new ServerBinding() {
				public InetAddress getInetAddress() {
					Statement nicStatement = configRes.getProperty(HTTPLISTENER.networkInterface); 
					if (nicStatement != null) {
						try {
							return InetAddress.getByName(nicStatement.getString());
						} catch (UnknownHostException e) {
							throw new RuntimeException(e);
						}
					}
					return null;
				}

				public int getPort() {
					return configRes.getProperty(HTTPLISTENER.port).getInt();
				}
				
			}, identity, trustedSources, configuration);
		} catch (IOException e) {
			throw new RuntimeException(e);
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

