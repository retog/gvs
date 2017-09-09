/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: SpiderService.java,v 1.1 2007/05/12 07:42:41 rebach Exp $
*/
package com.hp.hpl.jena.gvs.services.spider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.wrhapi.HeaderName;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.services.Service;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author reto
 *
 */
public class SpiderService implements Service {

	private Store store;
	private Set<Source> trustedSources;
	private static final Log log = LogFactory.getLog(SpiderService.class);

	/**
	 * @param store
	 * @param trustedSources
	 */
	public SpiderService(Store store, Set<Source> trustedSources) {
		this.store = store;
		this.trustedSources = trustedSources;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.services.Service#start()
	 */
	public void start() {
		int previousTrustedSourceSize = 0;
		while (trustedSources.size() > previousTrustedSourceSize) {
			previousTrustedSourceSize = trustedSources.size();
			Model currentModel = ModelFactory.createModelForGraph(store.getGraphOverTime(trustedSources).getGraph(new Date()));
			StmtIterator seeAlsoStmt = currentModel.listStatements(null, RDFS.seeAlso, (Resource)null);
			while(seeAlsoStmt.hasNext()) {
				Statement stmt = seeAlsoStmt.nextStatement();
				RDFNode object = stmt.getObject();
				try {
					Source seeAlsoSource = new SourceImpl(((Resource)object).getURI());
					if (trustedSources.contains(seeAlsoSource)) {
						continue;
					}
					if (!store.getSources().contains(seeAlsoSource)) {
						downloadSource(seeAlsoSource);
					}
					trustedSources.add(seeAlsoSource);
				} catch (Exception e) {
					log.debug(e);
				}
			}
		}
	}

	/**
	 * @param source
	 */
	private void downloadSource(Source source) {
		log.info("Spidering: "+source);
		Model downloadModel = ModelFactory.createDefaultModel();
		try {
			URL url = new URL(source.getURIRef());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);
			connection.addRequestProperty(HeaderName.ACCEPT.toString(), "application/rdf+xml; q=1, application/xml; q=.9, text/xml; q=.8, */*; q=.1");
			connection.setReadTimeout(3000);
			connection.connect();
			int responseCode = connection.getResponseCode(); 
			if (responseCode != 200) {
				log.info("Response code: "+connection.getResponseCode());
				if ((responseCode >= 300) && (responseCode < 400)) {
					String location = connection.getHeaderField(HeaderName.LOCATION.toString());
					if (location != null) {
						connection.disconnect();
						downloadSource(new SourceImpl(location));
					}
				}
				
				return;
			}
			downloadModel.read(connection.getInputStream(), source.getURIRef());
			store.assertGraph(source, new FCAGraphImpl(downloadModel));
			connection.disconnect();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
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

