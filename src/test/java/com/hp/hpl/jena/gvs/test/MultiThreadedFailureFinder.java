/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: MultiThreadedFailureFinder.java,v 1.1 2007/07/02 11:44:42 rebach Exp $
 */
package com.hp.hpl.jena.gvs.test;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;

import org.wymiwyg.commons.jena.ModelCreationUtil;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.leanifier.MoleculeBasedLeanifier;
import org.wymiwyg.rdf.molecules.diff.MoleculeDiffImpl;
import org.wymiwyg.rdf.molecules.functref.impl.ReferenceGroundedDecompositionImpl;
import org.wymiwyg.rdf.molecules.model.modelref.implgraph.ModelReferencingDecompositionImpl;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class MultiThreadedFailureFinder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Store store = FailureFinder.getStoreImpl();
		for (int i = 0; i < threads; i++) {
			startTestingThread(store, i);
		}
	}

	final static int modelSize = 5;
	final static int iterations = 10000;
	final static int threads = 9;

	/**
	 * @param store
	 */
	private static void startTestingThread(final Store store,
			final int threadNumber) {

		new Thread() {

			@Override
			public void run() {
				Source source = new SourceImpl("http://test-source/"
						+ threadNumber);
				for (int i = 0; i < iterations; i++) {
					try {
						Thread.sleep(2);
					} catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
					Graph currentGraph = JenaUtil
							.getGraphFromModel(
									ModelCreationUtil
											.createRandomModel((int) (modelSize * 2 * Math
													.random())), true);

					currentGraph = MoleculeBasedLeanifier.getLeanVersionOf(currentGraph);
					store.updateGraph(source, new FCAGraphImpl(currentGraph));
					if (Math.random() > 0.3) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						
						// ModelReferencingDecomposition dec = new
						// ModelReferencingDecompositionImpl(currentGraph);
						// TODO directly leanify dec
						FCAGraph retrievedGraph = store.getGraphOverTime(
								Collections.singleton(source)).getGraph(
								new Date());
						if (!retrievedGraph.equals(currentGraph)) {
							System.out.println("FAILURE!");
							JenaUtil.getModelFromGraph(currentGraph).write(
									System.out);
							ModelFactory.createModelForGraph(retrievedGraph)
									.write(System.out);
							PrintWriter pout = new PrintWriter(System.out);
							new MoleculeDiffImpl(
									new ReferenceGroundedDecompositionImpl(
											new ModelReferencingDecompositionImpl(
													currentGraph)),
													new ReferenceGroundedDecompositionImpl(
															new ModelReferencingDecompositionImpl(
																	retrievedGraph)))
									.print(pout);
							pout.flush();
							retrievedGraph = store.getGraphOverTime(
									Collections.singleton(source)).getGraph(
									new Date());
							System.out.println("on second attempt: "+retrievedGraph.equals(currentGraph));
							System.exit(1);
						}
					}
					if ((i % 100) == 0) {
						System.out.println("thread " + threadNumber
								+ " completed round " + i);
					}
				}
			}
		}.start();

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

