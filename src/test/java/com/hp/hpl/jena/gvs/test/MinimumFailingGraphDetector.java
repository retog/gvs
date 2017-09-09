/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: MinimumFailingGraphDetector.java,v 1.11 2007/05/15 09:11:26 rebach Exp $
 */
package com.hp.hpl.jena.gvs.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.Triple;
import org.wymiwyg.rdf.graphs.impl.SimpleGraph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class MinimumFailingGraphDetector {

	public static class TripleInfo {

		/**
		 * @param triple2
		 */
		public TripleInfo(Triple triple) {
			this.triple = triple;
		}

		Triple triple;

		States status = States.undefined;

	}

	public static enum States {
		undefined, needed, unneeded
	}

	/**
	 * This returns subgraph of a graph, in order to find a minimal sufficient
	 * graph the class attempts to remove triples one by one, if a subgraph was
	 * not sufficient the triple missing in that graph will not be removed
	 * again, if it was found to be sufficient it will allways be remove.
	 * 
	 * @author reto
	 * 
	 */
	class SubGraphCreator {
		Set<TripleInfo> tripleInfos = new HashSet<TripleInfo>();

		private int omitUndefined = 1;

		private int qualified = 0;

		private int recentSuccessCount = 0;

		private double reduceFactor = 0.5;

		SubGraphCreator(Graph g) {
			for (Triple triple : g) {
				tripleInfos.add(new TripleInfo(triple));
			}
		}

		Graph getFirstSubgraph() {
			return getNextSubGraph();
		}

		/**
		 * returns a new subgraph or null if no new one is available
		 * 
		 * @return
		 */
		Graph getNewSubgraph(boolean previousSubGraphWasSufficient) {
			int markCount = 0;
			for (TripleInfo info : tripleInfos) {
				if (info.status == States.undefined) {
					if (previousSubGraphWasSufficient) {
						info.status = States.unneeded;
						qualified++;
						markCount++;
					} else {
						if (omitUndefined == 1) {
							info.status = States.needed;
							qualified++;
							break;
						}
					}
					if (markCount == omitUndefined) {
						break;
					}
				}
			}
			if (previousSubGraphWasSufficient) {
				recentSuccessCount++;
				omitUndefined = ((int) ((tripleInfos.size() - qualified) * (reduceFactor * recentSuccessCount))) + 1;
				if (omitUndefined < 5) {
					omitUndefined = 1;
				}
				reduceFactor *= 2;
			} else {
				recentSuccessCount = 0;
				omitUndefined = 1;
				reduceFactor *= 0.2;
			}
			return getNextSubGraph();
		}

		private Graph getNextSubGraph() {
			int count = 0;
			Graph currentGraph = new SimpleGraph();
			for (TripleInfo info : tripleInfos) {
				switch (info.status) {
				case undefined:
					if (count < omitUndefined) {
						count++;
						break;
					} else {
						currentGraph.add(info.triple);
					}
					;
					break;
				case needed:
					currentGraph.add(info.triple);
				}
			}
			if (count == 0) {
				return null;
			}
			System.out.println("returning subgraph of size "
					+ currentGraph.size());
			return currentGraph;
		}
	}

	private TestPerformer testPerformer;

	/**
	 * 
	 */
	public MinimumFailingGraphDetector(FCAGraph[] graphs,
			TestPerformer testPerformer) {
		this.testPerformer = testPerformer;
		if (getTestResult(graphs)) {
			throw new RuntimeException("test with original graphs succed!");
		}
		for (int i = 0; i < graphs.length; i++) {
			createMimimumFailing(i, graphs);

		}
	}

	private void createMimimumFailing(int position, FCAGraph[] graphs) {
		SubGraphCreator subGraphCreator = new SubGraphCreator(graphs[position]);
		boolean lastOneFailed; // note that we are looking for failing graphs
		Graph lastFailingGraph = graphs[position];
		// int roundCount = 0;
		for (Graph currentGraph = subGraphCreator.getFirstSubgraph(); currentGraph != null; currentGraph = subGraphCreator
				.getNewSubgraph(lastOneFailed)) {
			graphs[position] = new FCAGraphImpl(currentGraph);
			lastOneFailed = !getTestResult(graphs);
			if (lastOneFailed) {
				lastFailingGraph = currentGraph;
				System.out.println("Graph " + position + " reduced to "
						+ lastFailingGraph.size());
				// if (roundCount++ % 100 ==0){
				// try {
				// JenaUtil.getModelFromGraph(currentGraph).write(new
				// FileWriter("g1-"+roundCount),"N-TRIPLES");
				// } catch (IOException e) {
				// throw new RuntimeException(e);
				// }
				// }
			}
		}
		graphs[position] = new FCAGraphImpl(lastFailingGraph);
	}

	/**
	 * @param g1
	 * @param g2
	 * @return
	 */
	private boolean getTestResult(FCAGraph[] graphs) {
		return testPerformer.performTest(graphs);
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * Model m0 = ModelFactory.createDefaultModel(); Model m1 =
		 * ModelFactory.createDefaultModel();
		 * m0.read(MinimumFailingGraphDetector.class.getResource("test4-m0.rdf")
		 * .toString());
		 * m1.read(MinimumFailingGraphDetector.class.getResource("test4-m1.rdf")
		 * .toString()); //
		 * m1.read(MinimumFailingGraphDetector.class.getResource("test13-1.nt").toString(), //
		 * "N-TRIPLE"); //
		 * m2.read(MinimumFailingGraphDetector.class.getResource("test14-2.nt").toString(), //
		 * "N-TRIPLE"); m0 = LeanDiffPatch.leanify(m0); m1 =
		 * LeanDiffPatch.leanify(m1);
		 */
		FCAGraph[] graphs = new FCAGraph[4];
		graphs[0] = readGraph("test17-m0.rdf");
		graphs[1] = readGraph("test17-m1.rdf");
		graphs[2] = readGraph("test17-m2.rdf");
		graphs[3] = readGraph("test17-m3.rdf");
		/*MinimumFailingGraphDetector detector = */new MinimumFailingGraphDetector(
				graphs, new FullRetrievalTester()); //RepeatedRetrievalTester()); // 
		for (int i = 0; i < graphs.length; i++) {
			System.out.println("minimum failing gaphs[" + i + "] (of size "
					+ graphs[i].size() + ") :");
			JenaUtil.getModelFromGraph(graphs[i]).write(System.out, "N-TRIPLE");
		}
		for (int i = 0; i < graphs.length; i++) {
			FileOutputStream fileOut = new FileOutputStream("failing"+i+"-minimized.rdf");
			JenaUtil.getModelFromGraph(graphs[i]).write(fileOut);
			fileOut.close();
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private static FCAGraph readGraph(String fileName) {
		Model model = ModelFactory.createDefaultModel();
		try {
			//model.read(new File(fileName).toURL().toString());
			model.read(MinimumFailingGraphDetector.class.getResource(fileName).toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
