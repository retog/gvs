/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FailureFinder.java,v 1.14 2007/07/02 11:44:42 rebach Exp $
 */
package com.hp.hpl.jena.gvs.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.util.Date;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.BaseStoreImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.filesystem.EhcacheGraphCache;
import com.hp.hpl.jena.gvs.impl.filesystem.FSMoleculeStore;
import com.hp.hpl.jena.gvs.impl.memory.MemoryMetaStore;
import com.hp.hpl.jena.gvs.impl.util.ToyClock;
import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;

/**
 * @author reto
 * 
 */
public class FailureFinder {

	/**
	 * 
	 */
	public FailureFinder() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Policy.setPolicy(new NoRestrictionsPolicy());
		for (int round = 0; round < 5000; round++) {
			singleRound(round);
			if (round > 100) {
				if ((round % 10) == 0) {
					System.gc();
					System.out.println(new Date() + ": "
							+ Runtime.getRuntime().freeMemory());
					// new BufferedReader(new
					// InputStreamReader(System.in)).readLine();
					System.out.println("continuing");
				}
			}

		}

	}

	static File testStoreDir;
	static {
		try {
			testStoreDir = File.createTempFile("teststore", "dir");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void recursiveDelete(File dir) {
		if (!dir.exists()) {
			return;
		}
		if (dir.delete()) {
			return;
		}
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				recursiveDelete(file);
			}
		} else {
			throw new RuntimeException("couldn't delete non-dir file");
		}
		if (!dir.delete()) {
			throw new RuntimeException(
					"couldn't delete dir after removing content");
		}
	}

	static Store getStoreImpl() {
		// return new MemoryStoreImpl();
		Policy.setPolicy(new NoRestrictionsPolicy());
		
		recursiveDelete(testStoreDir);
		if (!testStoreDir.mkdirs()) {
			throw new RuntimeException("failed to create "+testStoreDir);
		}
		Store result =  new BaseStoreImpl(new MemoryMetaStore(), new FSMoleculeStore(testStoreDir, new EhcacheGraphCache()));
		return result;
	}

	/**
	 * @param round
	 */
	private static void singleRound(int round) {
		System.out.println("round nr: " + round);
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl();
		store.setClock(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		TimedGraphSequence testData = TimedGraphSequence.getRandomSequence(4,
				315);// + 300);//(round / 5));
		try {
			System.out.println("writing current-case.txt");
			FileOutputStream currentCaseOut = new FileOutputStream(
					"current-case.txt");
			testData.write(currentCaseOut);
			currentCaseOut.close();
			currentCaseOut = null;
			// graphs representing the state of a changing graph in
			// chronological
			// order
			for (int i = 0; i < testData.getSize(); i++) {
				clock.setTime(testData.getDate(i));
				store.updateGraph(source, testData.getGraph(i));
			}
			for (int i = 0; i < 1/* 0 */; i++) {

				if (!StoreTest.compareVersionAtRandomDate(store, source,
						testData)) {
					testData.write(System.out);
					FileOutputStream fileOut = new FileOutputStream(
							"failing-case.txt");
					testData.write(fileOut);
					fileOut.close();
					return;
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				testData.write(System.out);
				FileOutputStream fileOut = new FileOutputStream(
						"failing-case.txt");
				testData.write(fileOut);
				fileOut.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			throw new RuntimeException("Failure found and saved");
		}

		store = null;
		testData = null;

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

