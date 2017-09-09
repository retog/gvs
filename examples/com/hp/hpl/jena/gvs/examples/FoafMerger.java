/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FoafMerger.java,v 1.4 2007/06/06 09:50:50 rebach Exp $
 */
package com.hp.hpl.jena.gvs.examples;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.filesystem.FSSToreImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This example merges the contents from different foaf personal profile
 * documents retrieved over the web.
 * 
 * @author reto
 * 
 */
public class FoafMerger {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Store store = new FSSToreImpl(new File("~/mystore"));
		Source danny = new SourceImpl("http://dannyayers.com/me.rdf");
		Source morten = new SourceImpl(
				"http://xml.mfd-consult.dk/foaf/morten.rdf");
		Source libby = new SourceImpl(
				"http://swordfish.rdfweb.org/people/libby/rdfweb/webwho.xrdf");
		Source timbl = new SourceImpl(
				"http://www.w3.org/People/Berners-Lee/card");
		Source henry = new SourceImpl("http://bblfish.net/people/henry/card");
		updateGraph(store, danny);
		updateGraph(store, morten);
		updateGraph(store, libby);
		updateGraph(store, timbl);
		updateGraph(store, henry);
		Set<Source> relevantSources = new HashSet<Source>();
		relevantSources.add(libby);
		relevantSources.add(henry);
		FCAGraph retrievedGraph = store.getGraphOverTime(relevantSources)
				.getGraph(new Date());
		Model retrievedModel = ModelFactory.createModelForGraph(retrievedGraph);
		retrievedModel.write(System.out);

	}

	/**
	 * @param store
	 * @param danny
	 */
	private static void updateGraph(Store store, Source source) {
		Model model = ModelFactory.createDefaultModel();
		model.read(source.getURIRef());
		FCAGraph graph = new FCAGraphImpl(model);
		store.assertGraph(source, graph);

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

