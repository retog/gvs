/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: ResourceContextUpdater.java,v 1.3 2007/05/02 10:52:42 rebach Exp $
*/
package com.hp.hpl.jena.gvs.examples;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.filesystem.FSSToreImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** This class provides a method to update the context of a resource from a remote store
 * 
 * @author reto
 *
 */
public class ResourceContextUpdater {

	private URL remoteStoreURL;
	private Store store;
	private Source localSource;
	private Set<Source> trustedSources;
	/**
	 * @param store the store to be update
	 * @param localSource the source as which the local update is performed
	 * @param remoteStoreURL the base URL of the remote server
	 * @param trustedSources the trusted remote sources
	 */
	public ResourceContextUpdater(Store store, Source localSource, URL remoteStoreURL, Set<Source> trustedSources) {
		this.store = store;
		this.localSource = localSource;
		this.remoteStoreURL = remoteStoreURL;
		this.trustedSources = trustedSources;
	}
	
	/** Updates the context of the specified resource.<br/>
	 * Note: in principle this would be possible for any GroundedNode but the current webservice does not yet 
	 * support getting the context of literals.
	 * 
	 * @param resource the resource for which the context ist to be updated
	 */
	public void updateResource(NamedNode resource) {


		Model retreivedModel = ModelFactory.createDefaultModel();
		StringWriter serviceURLWiter = new StringWriter();
		serviceURLWiter.write("gvs?");
		serviceURLWiter.write("resource=");
		try {
			serviceURLWiter.write(URLEncoder.encode(resource.getURIRef(), "utf-8"));
			for (Source trustedSource : trustedSources) {
				serviceURLWiter.write("&source=");
				serviceURLWiter.write(URLEncoder.encode(trustedSource.getURIRef(), "utf-8"));
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		try {
			retreivedModel.read(new URL(remoteStoreURL, serviceURLWiter.toString()).toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		final FCAGraph graph = new FCAGraphImpl(retreivedModel);
		final Set<GroundedNode> onlyForGroundedNodes = Collections.singleton((GroundedNode)resource);
		store.perform(localSource, new StoreTransaction() {

			public void execute(SourceStoreView storeView) {
				FCAGraph fcaGraph = storeView.getGraph();
				storeView.revokeGraph(fcaGraph.filter(onlyForGroundedNodes));
				storeView.assertGraph(graph);
			}
			
		});
		
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File storeDir = new File("./example-store/");
		storeDir.mkdir();
		Store store = new FSSToreImpl(storeDir);
		Source localSource = new SourceImpl("http://localhost/example-client");
		Set<Source> trustedSources = Collections.singleton((Source)new SourceImpl("http://gvs.hpl.hp.com/documentation"));
		URL remoteStoreURL = new URL("http://gvs.hpl.hp.com/");
		ResourceContextUpdater updater = new ResourceContextUpdater(store, localSource, remoteStoreURL, trustedSources);
		updater.updateResource(new NamedNodeImpl("http://gvs.hpl.hp.com/documentation/remote-access"));
		Set<Source> localSources = Collections.singleton(localSource);
		GraphOverTime got = store.getGraphOverTime(localSources);
		System.out.println("The current version of my graph in the local store is:");
		JenaUtil.getModelFromGraph(got.getGraph(new Date())).write(System.out);
		System.out.println();
		updater.updateResource(new NamedNodeImpl("http://gvs.hpl.hp.com/documentation/remote-access-title"));
		System.out.println("The current version of my graph in the local store is:");
		JenaUtil.getModelFromGraph(got.getGraph(new Date())).write(System.out);
		System.out.println();
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

