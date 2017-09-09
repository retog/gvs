/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: SimpleExample.java,v 1.1 2007/06/06 11:31:40 rebach Exp $
 */
package com.hp.hpl.jena.gvs.examples;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.wymiwyg.commons.vocabulary.FOAF;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.gvs.impl.util.java.MillisDateFormat;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A simple example on how an application can use a local GVS Store.
 * 
 * Two source assert two different Graphs after each other, after this all
 * time-versions of the graph for the union of both sources are outputted.
 * 
 * @author reto
 * 
 */
public class SimpleExample {

	/**
	 * The main method
	 * 
	 * @param args
	 *            no arguments supported
	 */
	public static void main(final String[] args) {
		final Store store = new MemoryStoreImpl(); // alternative: new
		// FSSToreImpl(new
		// File("/path/to/directory"));
		final Source jo = new SourceImpl("http://example.org/jo#");
		final Source mary = new SourceImpl("http://example.org/mary#");
		store.assertGraph(jo, getFirstGraph());
		store.assertGraph(mary, getSecondGraph());
		final Set<Source> joAndMary = new HashSet<Source>();
		joAndMary.add(jo);
		joAndMary.add(mary);
		final GraphOverTime graphOverTime = store.getGraphOverTime(joAndMary);
		final Iterator<Date> changeDates = graphOverTime
				.followingChanges(new Date(0));
		while (changeDates.hasNext()) {
			final Date currentChangeDate = changeDates.next();
			System.out.println("At "
					+ MillisDateFormat.instance.format(currentChangeDate)
					+ " Jo and Mary asserted:");
			ModelFactory.createModelForGraph(
					graphOverTime.getGraph(currentChangeDate))
					.write(System.out);
		}
	}

	/**
	 * @return a Graph with some infos about annie
	 */
	private static FCAGraph getFirstGraph() {
		Model model = ModelFactory.createDefaultModel();
		Resource annie = model.createResource();
		annie.addProperty(FOAF.mbox, model
				.createResource("mailto:annie@example.org"));
		annie.addProperty(FOAF.interest, model
				.createResource("http://www.w3.org/2001/sw/"));
		return new FCAGraphImpl(model);
	}

	/**
	 * @return a Graph with some infos about annie
	 */
	private static FCAGraph getSecondGraph() {
		Model model = ModelFactory.createDefaultModel();
		Resource annie = model
				.createResource("http://annie.example.org/profile#me");
		annie.addProperty(FOAF.mbox, model
				.createResource("mailto:annie@example.org"));
		annie.addProperty(FOAF.depiction, model
				.createResource("http://annie.example.org/images/me"));
		return new FCAGraphImpl(model);
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

