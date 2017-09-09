/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: FullRetrievalTester.java,v 1.8 2007/05/15 09:11:26 rebach Exp $
*/
package com.hp.hpl.jena.gvs.test;

import java.io.File;
import java.io.IOException;

import org.wymiwyg.rdf.leanifier.MoleculeBasedLeanifier;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.filesystem.FSSToreImpl;
import com.hp.hpl.jena.gvs.impl.util.ToyClock;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * @author reto
 *
 */
public class FullRetrievalTester implements TestPerformer {

	private File testStoreDir;// = new File("teststore");
	{
		try {
			testStoreDir = File.createTempFile("teststore", "dir");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		testStoreDir.delete();
		testStoreDir.mkdirs();
	}
	
	/**
	 * 
	 */
	public FullRetrievalTester() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.test.TestPerformer#perfromTest(org.wymiwyg.rdf.graphs.Graph[])
	 */
	public boolean performTest(FCAGraph[] graphs) {
		for (int k = 0; k < 2; k++) {
		JenaParameters.disableBNodeUIDGeneration = true;
		try {
			ToyClock clock = new ToyClock();
			Store store = getStoreImpl(clock);
			Source source = new SourceImpl("http://example.org/graph-source");
			for (int i = 0; i < graphs.length; i++) {
				//TODO leanify based on dec
				graphs[i] = new FCAGraphImpl(MoleculeBasedLeanifier.getLeanVersionOf(graphs[i]));
			}
			TimedGraphSequence tgs = TimedGraphSequence
					.getSequenceWithRandomDates(graphs);
			for (int i = 0; i < tgs.getSize(); i++) {
				clock.setTime(tgs.getDate(i));
				store.updateGraph(source, tgs.getGraph(i));
			}
			if (!StoreTest.compareAllVersions(store, source, tgs)) {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		}
		//tgs.write(System.out);
		return true;
	}

	/**
	 * @return
	 */


	static void delDirContent(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				delDirContent(file);
			}
			file.delete();
		}
	}

	protected Store getStoreImpl(Clock clock) {
		try {
	
			if (testStoreDir.exists()) {
				delDirContent(testStoreDir);
			}
			return new FSSToreImpl(testStoreDir, clock);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		/*
		 * File teststore = new File("teststore"); teststore.mkdirs(); return
		 * new BaseStoreImpl(new MemoryMetaStore(), new
		 * FSMoleculeStore(teststore));
		 */
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

