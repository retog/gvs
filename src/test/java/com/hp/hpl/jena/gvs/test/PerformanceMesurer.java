/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: PerformanceMesurer.java,v 1.7 2007/10/01 19:29:41 rebach Exp $
 */
package com.hp.hpl.jena.gvs.test;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;

import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;

import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.gvs.impl.util.CannedDataReader;
import com.hp.hpl.jena.gvs.impl.util.CannedDataReader.Assertion;

/**
 * @author reto
 * 
 */
public class PerformanceMesurer {

	final static String baseURLString = "http://localhost/";
	
	/**
	 * @param args
	 */
	//2001018: 40872 / 100 iter. Probably because of recomputation of hashes of contained fg-nodes
	// using -server jvm option: 31381ms
	//20061013 org.wymiwyg.rdf.graphs.jenaimpl.TypedLiteralNodeImpl: 30986 / 100 iter
	//20061013 hashCode comparison in graph-equals: 31081 / 100 iter 
	//20061013 no stack trace in exception: 33054 / 100 iter
	//20061013: 43659ms / 100 iter
	//20070502: 28396ms / 100 iter
	//20070502 with anonymizing: 116569ms and 99683ms / 100 iter
	//with recomputed Strong Hash for fg-node equality: 143548ms, 106629ms
	//With cached strong hashes: 107537ms / 151086ms
	//With == identity for unfinalized fg-nodes: 68947ms / 69644ms
	//with equals always based on strong hashes: 70526ms / 68366ms
	//extracting equals/hashCode to FunctionallyGroundedNodeBase: 98224ms / 68363ms / 61201ms
	//20070517 checking if molecules already present on assertion: 71298ms / 97099ms
	//20070702 after adding synchronized statements: 77117ms / 52087ms
	//20070704 with rdf-utils 47324ms
	public static void main(String[] args) throws Exception {
		SortedSet<CannedDataReader.Assertion> data = CannedDataReader.getAssertions(PathNodeFactory.getPathNode(StoreTest.class.getResource("/com/hp/hpl/jena/gvs/test/testdata")),
				baseURLString);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			action(data);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("It took " + (endTime - startTime) + "ms");

	}

	/**
	 * @param data 
	 * 
	 */
	private static void action(SortedSet<Assertion> assertions) {
		Store store = new MemoryStoreImpl();
		CannedDataReader.addToStrore(store, assertions);
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton((Source) new SourceImpl(baseURLString + "danbri")));
		got.getGraph(new Date());
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

