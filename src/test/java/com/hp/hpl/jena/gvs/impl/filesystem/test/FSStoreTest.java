/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FSStoreTest.java,v 1.9 2007/05/23 12:57:28 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.filesystem.test;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreException;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.filesystem.FSSToreImpl;
import com.hp.hpl.jena.gvs.impl.util.ToyClock;
import com.hp.hpl.jena.gvs.test.StoreTest;
import com.hp.hpl.jena.gvs.test.TimedGraphSequence;

/**
 * @author reto
 * 
 */
public class FSStoreTest extends StoreTest {

	private Store lastStore = null;
	
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
	 * @param name
	 */
	public FSStoreTest(String name) {
		super(name);
	}

	/**
	 * it must be impossible to open a store for a directory which is used by
	 * another store instance
	 * @throws IOException 
	 */
	public void testDoubleInstance() throws IOException {
		Store store1 = new FSSToreImpl(testStoreDir);
		boolean gotException = false;
		try {
			new FSSToreImpl(testStoreDir);
		} catch (StoreException e) {
			gotException = true;
		}
		assertTrue(gotException);
	}

	@Override
	protected Store getStoreImpl(Clock clock) {
		try {
			if (lastStore != null) {
				lastStore.close();
			}
			if (testStoreDir.exists()) {
				delDirContent(testStoreDir);
			}
			lastStore =  new FSSToreImpl(testStoreDir, clock);
			return lastStore;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		/*
		 * File teststore = new File("teststore"); teststore.mkdirs(); return
		 * new BaseStoreImpl(new MemoryMetaStore(), new
		 * FSMoleculeStore(teststore));
		 */
	}

	protected Store reGetStoreImpl() {
		try {
			if (lastStore != null) {
				lastStore.close();
			}
			lastStore = new FSSToreImpl(testStoreDir);
			return lastStore;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void performTestsWithGraphs(FCAGraph[] graphs) {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		TimedGraphSequence tgs = TimedGraphSequence
				.getSequenceWithRandomDates(graphs);
		for (int i = 0; i < tgs.getSize(); i++) {
			clock.setTime(tgs.getDate(i));
			store.updateGraph(source, tgs.getGraph(i));
		}
		assertTrue(compareAllVersions(store, source, tgs));
		assertTrue(compareVersionAtRandomDate(store, source, tgs));
		// do the same again with new store instance
		try {
			store.close();
			store = new FSSToreImpl(testStoreDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// assertTrue(compareAllVersions(store, source, tgs));
		// assertTrue(compareVersionAtRandomDate(store, source, tgs));
	}

	static void delDirContent(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				delDirContent(file);
			}
			file.delete();
		}
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

