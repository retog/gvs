/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 <<<<<<< StoreTest.java
 $Id: StoreTest.java,v 1.30 2007/05/23 12:57:07 rebach Exp $
 =======
 $Id: StoreTest.java,v 1.30 2007/05/23 12:57:07 rebach Exp $
 >>>>>>> 1.3
 */
package com.hp.hpl.jena.gvs.test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.wymiwyg.commons.util.text.W3CDateFormat;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.impl.SimpleGraph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaModelGraph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.leanifier.MoleculeBasedLeanifier;
import org.wymiwyg.rdf.utils.jena.LeanDiffPatch;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.gvs.impl.util.CannedDataReader;
import com.hp.hpl.jena.gvs.impl.util.ToyClock;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * @author reto
 * 
 */
public class StoreTest extends ModelTestBase {

	private MemoryStoreImpl store;

	public StoreTest(String name) {
		super(name);
	}

	public void testSingleSourceVersions() {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");

		// graphs representing the state of a changing graph in chronological
		// order
		TimedGraphSequence testData = TimedGraphSequence.getRandomSequence(12,
				100);
		for (int i = 0; i < testData.getSize(); i++) {
			clock.setTime(testData
					.getDate(i));
			store
					.updateGraph(source, testData.getGraph(i));
		}
		for (int i = 0; i < 1; i++) {
			assertTrue("retreved graph equals to original",
					compareVersionAtRandomDate(store, source, testData));
		}

	}

	public void testTwoSourcesVersions() {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source0 = new SourceImpl("http://example.org/graph-source0");
		Source source1 = new SourceImpl("http://example.org/graph-source1");
		// graphs representing the state of a changing graph in chronological
		// order
		TimedGraphSequence testData0 = TimedGraphSequence.getRandomSequence(12,
				100);
		for (int i = 0; i < testData0.getSize(); i++) {
			clock.setTime(testData0
					.getDate(i));
			store.updateGraph(source0, testData0.getGraph(i));
		}
		TimedGraphSequence testData1 = TimedGraphSequence.getRandomSequence(12,
				100);
		for (int i = 0; i < testData1.getSize(); i++) {
			clock.setTime(testData1
					.getDate(i));
			store.updateGraph(source1, testData1.getGraph(i));
		}
		for (int i = 0; i < 1; i++) {
			assertTrue("retreved graph equals to original",
					compareVersionAtRandomDate(store, source0, testData0));
		}
		for (int i = 0; i < 1; i++) {
			assertTrue("retreved graph equals to original",
					compareVersionAtRandomDate(store, source1, testData1));
		}

	}

	public void testUpdate() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source#");
		Model model0 = ModelFactory.createDefaultModel();
		model0.read(StoreTest.class.getResource("update-test0.rdf").toString());
		FCAGraph graph0 = new FCAGraphImpl(model0);
		clock.setTime(new Date());
		store.updateGraph(source, graph0);
		store = null;
		Thread.sleep(2);
		store = reGetStoreImpl();
		Model model1 = ModelFactory.createDefaultModel();
		model1.read(StoreTest.class.getResource("update-test1.rdf").toString());
		FCAGraph graph1 = new FCAGraphImpl(model1);
		clock.setTime(new Date());
		store.updateGraph(new SourceImpl("http://example.org/graph-source#"),
				graph1);
		FCAGraph graphR = store.getGraphOverTime(
				Collections.singleton((Source) new SourceImpl(
						"http://example.org/graph-source#"))).getGraph(
				new Date());
		assertEquals(graph1, graphR);
		store = reGetStoreImpl();
		graphR = store.getGraphOverTime(
				Collections.singleton((Source) new SourceImpl(
						"http://example.org/graph-source#"))).getGraph(
				new Date());
		assertEquals(graph1, graphR);
	}

	public void testSources() {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source0 = new SourceImpl("http://example.org/graph-source0");
		Source source1 = new SourceImpl("http://example.org/graph-source1");
		// graphs representing the state of a changing graph in chronological
		// order
		TimedGraphSequence testData0 = TimedGraphSequence.getRandomSequence(12,
				100);
		for (int i = 0; i < testData0.getSize(); i++) {
			clock.setTime(testData0
					.getDate(i));
			store.updateGraph(source0, testData0.getGraph(i));
		}
		TimedGraphSequence testData1 = TimedGraphSequence.getRandomSequence(12,
				100);
		for (int i = 0; i < testData1.getSize(); i++) {
			clock.setTime(testData1
					.getDate(i));
			store.updateGraph(source1, testData1.getGraph(i));
		}
		assertEquals(2, store.getSources().size());
		store = reGetStoreImpl();
		assertEquals(2, store.getSources().size());
	}

	protected static boolean compareVersionAtRandomDate(Store store,
			Source source, TimedGraphSequence testData) {
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source);

		Date retrievingDate = new Date(
				(long) (testData.getDate(0).getTime() + (Math.random()
						* (1 + 1f / testData.getSize()) * (testData.getDate(
						testData.getSize() - 1).getTime() - testData.getDate(0)
						.getTime()))));
		FCAGraph expectedGraph = null;
		int graphNumber = -1;
		for (int j = 0; j < testData.getSize(); j++) {
			if (!retrievingDate.before(testData.getDate(j))) {
				graphNumber = j;
			} else {
				System.out.println(retrievingDate.getTime() + " is before "
						+ testData.getDate(j).getTime());
				break;
			}
		}
		expectedGraph = testData.getGraph(graphNumber);
		System.out.println("Expecting graph number " + graphNumber + " for "
				+ retrievingDate);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				retrievingDate);
		retrievedGraph = new FCAGraphImpl(MoleculeBasedLeanifier
				.getLeanVersionOf(retrievedGraph));
		boolean result = expectedGraph.equals(retrievedGraph);
		if (!result) {
			System.out.println("Jena isomorphism: "
					+ JenaUtil.getModelFromGraph(retrievedGraph)
							.isIsomorphicWith(
									JenaUtil.getModelFromGraph(expectedGraph)));
			System.out.println("But got: ");
			JenaUtil.getModelFromGraph(retrievedGraph).write(System.out);
		}
		return result;
	}

	static boolean compareVersionAtPosition(Store store, Source source,
			TimedGraphSequence testData, int position) {
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source);

		Date retrievingDate = new Date(
				(long) (testData.getDate(position).getTime() + (Math.random() * (testData
						.getSize() > (position + 1) ? (testData.getDate(
						position + 1).getTime() - testData
						.getDate(position + 1).getTime()) : 100))));
		FCAGraph expectedGraph = null;
		int graphNumber = -1;
		for (int j = 0; j < testData.getSize(); j++) {
			if (!retrievingDate.before(testData.getDate(j))) {
				graphNumber = j;
			} else {
				break;
			}
		}
		expectedGraph = testData.getGraph(graphNumber);
		System.out.println("Expecting graph number " + graphNumber + " for "
				+ retrievingDate);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				retrievingDate);
		retrievedGraph = new FCAGraphImpl(MoleculeBasedLeanifier
				.getLeanVersionOf(retrievedGraph));
		boolean result = expectedGraph.equals(retrievedGraph);
		if (!result) {
			System.out.println("But got: ");
			JenaUtil.getModelFromGraph(retrievedGraph).write(System.out);
			System.out.println("from ");
			try {
				testData.write(System.out);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public void testTwoSingleSourceVersions() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph[] graphs = new FCAGraph[2];

		// Model model1 = ModelFactory.createDefaultModel();
		// model1.createResource().addProperty(DC.subject, "test1");

		// Model model1 = modelWithStatements("_a dc:subject 'test1';
		// http://www.google.com/ foaf:maker 'ygf'");
		Model model1 = modelWithStatements("_a dc:subject _b");
		Model model2 = ModelFactory.createDefaultModel();
		model2.createResource().addProperty(DC.subject, "test2");
		graphs[0] = new FCAGraphImpl(model1);
		graphs[1] = new FCAGraphImpl(model2);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:30:00Z"));
		store.updateGraph(source, graphs[0]);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:32:00Z"));
		store.updateGraph(source, graphs[1]);
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-05-01T16:31:00Z"));
		assertEquals(graphs[0], retrievedGraph);
		retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-05-01T16:32:00Z"));
		assertEquals(graphs[1], retrievedGraph);
		retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-06-01"));
		assertEquals(graphs[1], retrievedGraph);
	}

	public void testSimpleIFP() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source0 = new SourceImpl("http://example.org/graph-source0");
		Source source1 = new SourceImpl("http://example.org/graph-source1");
		Model model0 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox 'm'");
		Model model1 = modelWithStatements("_a rdfs:label 'hi'; _a http://xmlns.com/foaf/0.1/mbox 'm'");
		FCAGraph graph0 = new FCAGraphImpl(model0);
		FCAGraph graph1 = new FCAGraphImpl(model1);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:30:00Z"));
		store.updateGraph(source0, graph0);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:32:00Z"));
		store.updateGraph(source1, graph1);
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source0);
		sourceSet.add(source1);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-06-01"));
		assertEquals(3, retrievedGraph.size());
	}

	public void testOneAndTwoNTifp() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source0 = new SourceImpl("http://example.org/graph-source0");
		Source source1 = new SourceImpl("http://example.org/graph-source1");
		Model model0 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox 'm'; _a http://xmlns.com/foaf/0.1/mbox 'n'");
		Model model1 = modelWithStatements("_a rdfs:label 'hi'; _a http://xmlns.com/foaf/0.1/mbox 'm'");
		FCAGraph graph0 = new FCAGraphImpl(model0);
		FCAGraph graph1 = new FCAGraphImpl(model1);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:30:00Z"));
		store.updateGraph(source0, graph0);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:32:00Z"));
		store.updateGraph(source1, graph1);
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source0);
		sourceSet.add(source1);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-06-01"));
		assertEquals(4, retrievedGraph.size());
	}

	public void testCrossoverIFP() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source0 = new SourceImpl("http://example.org/graph-source0");
		Source source1 = new SourceImpl("http://example.org/graph-source1");
		Source source2 = new SourceImpl("http://example.org/graph-source2");
		Source source3 = new SourceImpl("http://example.org/graph-source3");
		Model model0 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox 'm'; _a http://xmlns.com/foaf/0.1/mbox 'n'");
		Model model1 = modelWithStatements("_a rdfs:label 'hi'; _a http://xmlns.com/foaf/0.1/mbox 'm'");
		Model model2 = modelWithStatements("_a rdfs:comment 'foo'; _a http://xmlns.com/foaf/0.1/mbox 'n'; _a http://xmlns.com/foaf/0.1/mbox 'o'");
		Model model3 = modelWithStatements("_a rdfs:comment 'bar'; _a http://xmlns.com/foaf/0.1/mbox 'o'");
		FCAGraph graph0 = new FCAGraphImpl(model0);
		FCAGraph graph1 = new FCAGraphImpl(model1);
		FCAGraph graph2 = new FCAGraphImpl(model2);
		FCAGraph graph3 = new FCAGraphImpl(model3);
		clock.setTime(new W3CDateFormat()
				.parse("2006-05-01T16:30:00Z"));
		store.updateGraph(source0, graph0);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:32:00Z"));
		store.updateGraph(source1, graph1);
		store.updateGraph(source2, graph2);
		store.updateGraph(source3, graph3);
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source0);
		sourceSet.add(source1);
		sourceSet.add(source2);
		sourceSet.add(source3);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-06-01"));
		Model model = JenaUtil.getModelFromGraph(retrievedGraph);
		model.write(System.out);
		assertEquals(7, retrievedGraph.size());
	}

	public void testReaddingSame() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph[] graphs = new FCAGraph[3];
		graphs[0] = new FCAGraphImpl(
				modelWithStatements("_a dc:subject ''"));
		graphs[1] = new FCAGraphImpl(
				modelWithStatements("_a dc:author 'bla'"));
		graphs[2] = new FCAGraphImpl(
				modelWithStatements("_a dc:subject ''"));
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:30:00Z"));
		store.updateGraph(source, graphs[0]);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:32:00Z"));
		store.updateGraph(source, graphs[1]);
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-02T02:16:00Z"));
		store.updateGraph(source, graphs[2]);
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-05-01T16:31:00Z"));
		assertEquals(graphs[0], retrievedGraph);
		retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-05-01T16:32:00Z"));
		assertEquals(graphs[1], retrievedGraph);
		retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-06-01"));
		assertEquals(graphs[2], retrievedGraph);
	}

	public void testNamedUnnamed() {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);;
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph[] graphs = new FCAGraph[3];
		graphs[0] = new FCAGraphImpl(
				modelWithStatements("_a dc:subject 'nbmnb'"));
		graphs[1] = new FCAGraphImpl(
						modelWithStatements("urn:urn-5:BvkoXZbrJ83FXCkas0+D67bgHJo= dc:subject _b"));
		graphs[2] = new FCAGraphImpl(
						modelWithStatements("urn:urn-5:BvkoXZbrJ83FXCkas0+D67bgHJo= dc:author _b"));
		TimedGraphSequence tgs = TimedGraphSequence
				.getSequenceWithRandomDates(graphs);
		for (int i = 0; i < tgs.getSize(); i++) {
			clock.setTime(tgs.getDate(i));
			store.updateGraph(source, tgs.getGraph(i));
		}
		compareVersionAtPosition(store, source, tgs, 1);
		// tgs.write(System.out);
	}

	/**
	 * An assertion is valid from the moment of assertion included till the
	 * moment of revocation excluded
	 * 
	 * @return
	 * @throws Exception
	 */
	public void testBorderTime() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = new FCAGraphImpl(
				modelWithStatements("_a dc:subject _b"));
		graphs[1] = new FCAGraphImpl(
				modelWithStatements("http://example.org/ dc:author 'foo'"));
		clock.setTime(new W3CDateFormat()
		.parse("2006-05-01T16:30:00Z"));
		store.updateGraph(source, graphs[0]);
		Date date2 = new W3CDateFormat().parse("2006-05-01T16:32:00Z");
		clock.setTime(date2);
		store.updateGraph(source, graphs[1]);
		Set<Source> sourceSet = new HashSet<Source>();
		sourceSet.add(source);
		FCAGraph retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(
				new W3CDateFormat().parse("2006-05-01T16:30:00Z"));
		assertEquals(graphs[0], retrievedGraph);
		retrievedGraph = store.getGraphOverTime(sourceSet).getGraph(date2);
		assertEquals(graphs[1], retrievedGraph);
	}

	protected Model getModelFromResource(String resName) {
		Model model = ModelFactory.createDefaultModel();
		if (resName.endsWith("nt")) {
			model.read(StoreTest.class.getResource(resName).toString(), "N-TRIPLE");
		} else {
			model.read(StoreTest.class.getResource(resName).toString());
		}
		return model;
	}

	protected FCAGraph getGraphFromResource(String resName) {
		Graph graph = JenaUtil.getGraphFromModel(getModelFromResource(resName),
				true);
		graph = MoleculeBasedLeanifier.getLeanVersionOf(graph);
		// GraphLeanifier.makeLean(graph);
		return new FCAGraphImpl(graph);
	}

	public void testWithEmptyGraphs() throws Exception {
		FCAGraph[] graphs = new FCAGraph[4];
		for (int i = 0; i < graphs.length; i++) {
			SimpleGraph simpleGraph = new SimpleGraph();
			simpleGraph.markFinalized();
			graphs[i] = new FCAGraphImpl(simpleGraph);
		}
		performTestsWithGraphs(graphs);
	}

	public void testFromFiles1() throws Exception {
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = getGraphFromResource("test1-m0.rdf");
		graphs[1] = getGraphFromResource("test1-m1.rdf");
		performTestsWithGraphs(graphs);
	}

	public void testFromFiles2() throws Exception {
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = getGraphFromResource("test2-m0.rdf");
		graphs[1] = getGraphFromResource("test2-m1.rdf");
		performTestsWithGraphs(graphs);
	}

	public void testFromFiles3() throws Exception {
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = getGraphFromResource("test3-m0.rdf");
		// an unlean graph
		graphs[1] = getGraphFromResource("test3-m1.rdf");
		performTestsWithGraphs(graphs);
	}

	public void testFromFiles4() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = getGraphFromResource("test4-m0.rdf");
		graphs[1] = getGraphFromResource("test4-m1.rdf");
		// assertTrue(new RepeatedRetrievalTester().performTest(graphs));
		performTestsWithGraphs(graphs);
	}

	public void testFromFiles5() throws Exception {
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = getGraphFromResource("test5-m0.rdf");
		graphs[1] = getGraphFromResource("test5-m1.rdf");
		performTestsWithGraphs(graphs);
	}

	public void testFromFiles4b() throws Exception {
		// JenaParameters.disableBNodeUIDGeneration = true;
		Model m0 = ModelFactory.createDefaultModel();
		Model m1 = ModelFactory.createDefaultModel();
		m0.read(MinimumFailingGraphDetector.class.getResource("test4-m0.rdf")
				.toString());
		m1.read(MinimumFailingGraphDetector.class.getResource("test4-m1.rdf")
				.toString());
		// m1.read(MinimumFailingGraphDetector.class.getResource("test13-1.nt").toString(),
		// "N-TRIPLE");
		// m2.read(MinimumFailingGraphDetector.class.getResource("test14-2.nt").toString(),
		// "N-TRIPLE");
		m0 = LeanDiffPatch.leanify(m0);
		m1 = LeanDiffPatch.leanify(m1);
		FCAGraph g1 = new FCAGraphImpl(new JenaModelGraph(m0, true));
		// FCAGraph g2 = new JenaModelGraph(m1, true);
		FCAGraph[] graphs = new FCAGraph[2];
		graphs[0] = getGraphFromResource("test4-m0.rdf");
		graphs[1] = getGraphFromResource("test4-m1.rdf");
		// GraphLeanifier.makeLean(graphs[0]);
		// graphs[0] = MoleculeBasedLeanifier.getLeanVersionOf(graphs[0]);
		assertEquals(g1, graphs[0]);
		/*
		 * assertEquals(g2, graphs[1]); graphs[0] = g1; graphs[1] = g2;
		 * assertTrue(new RepeatedRetrievalTester().performTest(graphs));
		 */
	}

	public void testFromFiles6() throws Exception {
		for (int i = 0; i < 1; i++) {
			FCAGraph[] graphs = new FCAGraph[2];
			graphs[0] = getGraphFromResource("test6-m0.rdf");
			graphs[1] = getGraphFromResource("test6-m1.rdf");
			FCAGraph instead = getGraphFromResource("test6-instead-of-m1.rdf");
			assertEquals(instead, graphs[1]);
			performTestsWithGraphs(graphs);
		}
	}

	public void testFromFiles7() throws Exception {
		for (int i = 0; i < 1; i++) {
			FCAGraph[] graphs = new FCAGraph[2];
			graphs[0] = getGraphFromResource("test7-m0.rdf");
			graphs[1] = getGraphFromResource("test7-m1.rdf");
			performTestsWithGraphs(graphs);
		}
	}

	public void testFromFiles8() throws Exception {
		for (int i = 0; i < 1; i++) {
			FCAGraph[] graphs = new FCAGraph[2];
			graphs[0] = getGraphFromResource("test8-m0.rdf");
			graphs[1] = getGraphFromResource("test8-m1.rdf");
			performTestsWithGraphs(graphs);
		}
	}

	public void testFromFiles9() throws Exception {
		FCAGraph[] graphs = new FCAGraph[2];
		for (int i = 0; i < 1; i++) {
			graphs[0] = getGraphFromResource("test9-m0.rdf");
			graphs[1] = getGraphFromResource("test9-m1.rdf");
			FCAGraph wrongResult = getGraphFromResource("test9-m1-got.rdf");
			assertEquals(graphs[1], wrongResult);
			performTestsWithGraphs(graphs);
		}
	}

	public void testFromFiles10() throws Exception {
		for (int i = 0; i < 1; i++) {
			List<FCAGraph> graphList = new ArrayList<FCAGraph>();
			graphList.add(getGraphFromResource("test10-m0.rdf"));
			graphList.add(getGraphFromResource("test10-m1.rdf"));
			performTestsWithGraphs(graphList
					.toArray(new FCAGraph[graphList.size()]));
		}
	}

	public void testFromFiles10io() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph orig = getGraphFromResource("test10-m0.rdf");
		clock.setTime(new Date());
		store.assertGraph(source, orig);
		FCAGraph retrived = store.getGraphOverTime(Collections.singleton(source))
				.getGraph(new Date());
		assertEquals(orig, retrived);
	}

	public void testFromFiles11() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		for (int i = 0; i < 2; i++) {
			List<FCAGraph> graphList = new ArrayList<FCAGraph>();
			graphList.add(getGraphFromResource("test11-m0.rdf"));
			graphList.add(getGraphFromResource("test11-m1.rdf"));
			graphList.add(getGraphFromResource("test11-m2.rdf"));
			graphList.add(getGraphFromResource("test11-m3.rdf"));
			performTestsWithGraphs(graphList
					.toArray(new FCAGraph[graphList.size()]));
		}
	}

	public void testFromFiles12() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		for (int i = 0; i < 1; i++) {
			List<FCAGraph> graphList = new ArrayList<FCAGraph>();
			graphList.add(getGraphFromResource("test12-m0.rdf"));
			graphList.add(getGraphFromResource("test12-m1.rdf"));
			graphList.add(getGraphFromResource("test12-m2.rdf"));
			graphList.add(getGraphFromResource("test12-m3.rdf"));
			performTestsWithGraphs(graphList
					.toArray(new FCAGraph[graphList.size()]));
		}
	}
	
	public void testIfpChain() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		for (int i = 0; i < 1; i++) {
			List<FCAGraph> graphList = new ArrayList<FCAGraph>();
			//graphList.add(new SimpleGraph());
			graphList.add(getGraphFromResource("ifp-chain.nt"));
			//graphList.add(new SimpleGraph());
			//graphList.add(new SimpleGraph());
			performTestsWithGraphs(graphList
					.toArray(new FCAGraph[graphList.size()]));
		}
	}

	public void testFromFiles13() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph orig = getGraphFromResource("test13.rdf");
		clock.setTime(new Date());
		store.assertGraph(source, orig);
		FCAGraph retrived = store.getGraphOverTime(Collections.singleton(source))
				.getGraph(new Date());
		assertEquals(orig, retrived);
	}

	public void testFromFiles14() throws Exception {
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Source source = new SourceImpl("http://example.org/graph-source");
		FCAGraph orig = getGraphFromResource("test14.rdf");
		clock.setTime(new Date());
		store.assertGraph(source, orig);
		FCAGraph retrived = store.getGraphOverTime(Collections.singleton(source))
				.getGraph(new Date());
		assertEquals(orig, retrived);
	}

	public void testFromFiles15() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		for (int i = 0; i < 1; i++) {
			List<FCAGraph> graphList = new ArrayList<FCAGraph>();
			graphList.add(getGraphFromResource("test15-m0.rdf"));
			graphList.add(getGraphFromResource("test15-m1.rdf"));
			graphList.add(getGraphFromResource("test15-m2.rdf"));
			graphList.add(getGraphFromResource("test15-m3.rdf"));
			performTestsWithGraphs(graphList
					.toArray(new FCAGraph[graphList.size()]));
		}
	}
	
	public void testFromFiles17() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		for (int i = 0; i < 1; i++) {
			List<FCAGraph> graphList = new ArrayList<FCAGraph>();
			graphList.add(getGraphFromResource("test17-m0.rdf"));
			graphList.add(getGraphFromResource("test17-m1.rdf"));
			graphList.add(getGraphFromResource("test17-m2.rdf"));
			graphList.add(getGraphFromResource("test17-m3.rdf"));
			performTestsWithGraphs(graphList
					.toArray(new FCAGraph[graphList.size()]));
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
	}

	/**
	 * @param store
	 * @param source
	 * @param tgs
	 */
	protected static boolean compareAllVersions(Store store, Source source,
			TimedGraphSequence tgs) {
		for (int i = 0; i < tgs.getSize(); i++) {
			if (!compareVersionAtPosition(store, source, tgs, i)) {
				return false;
			}
		}
		return true;

	}

	public void testRepeated() throws Exception {
		JenaParameters.disableBNodeUIDGeneration = true;
		FCAGraph previousGraph = null;
		FCAGraph previousUnleanifiedGraph = null;
		// Model previousModel = null;

		for (int i = 0; i < 1; i++) {
			System.out.println("testing round " + i);
			Model model = ModelFactory.createDefaultModel();
			model.read(StoreTest.class.getResource("TestLeanError.rdf")
					.toString());

			FCAGraph unleanifiedGraph = new FCAGraphImpl(model);

			FCAGraph graph = new FCAGraphImpl(MoleculeBasedLeanifier
					.getLeanVersionOf(unleanifiedGraph));

			// FCAGraph graph = getGraphFromResource("test4-m0.rdf");
			if (previousGraph != null) {
				if (!graph.equals(previousGraph)) {
					System.out.println("Here we are!");
					System.out
							.println("Unleanified equals: "
									+ unleanifiedGraph
											.equals(previousUnleanifiedGraph));
					System.out.println("current:" + graph.size());
					JenaUtil.getModelFromGraph(graph).write(System.out);
					System.out.println("previous:" + previousGraph.size());
					JenaUtil.getModelFromGraph(previousGraph).write(System.out);
					/*
					 * PrintWriter pout = new PrintWriter(System.out); new
					 * MoleculeDiffImpl( new ReferenceGroundedDecompositionImpl(
					 * new ModelReferencingDecompositionImpl( graph)), new
					 * ReferenceGroundedDecompositionImpl( new
					 * ModelReferencingDecompositionImpl( previousGraph)))
					 * .print(pout); pout.flush();
					 */
					return;
				}
				// assertEquals(graphs[0], previousGraphs[0]);
				// assertEquals(graphs[1], previousGraphs[1]);
			}
			previousGraph = graph;
			previousUnleanifiedGraph = unleanifiedGraph;

			// performTestsWithGraphs(graphs);
		}
	}

	protected Store getStoreImpl(Clock clock) {
		store = new MemoryStoreImpl(clock);
		return store;
	}

	protected Store reGetStoreImpl() {
		return store;
	}

	public void testPreviousDates() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		String baseURLString = "http://localhost/";
		CannedDataReader.readCannedData(store, getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata"),
				baseURLString);
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton((Source) new SourceImpl(baseURLString + "danbri")));
		// one milli after last modification
		Iterator<Date> previousChanges = got.previousChanges(dateFormat
				.parse("20060920123618662"));
		// should get last modification
		assertEquals(dateFormat.parse("20060920123618661"), previousChanges
				.next());
		// eralier one
		assertEquals(dateFormat.parse("19980920123600000"), previousChanges
				.next());
	}

	// 20061006 changed as interface spec changed
	public void testPreviousDates2() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		String baseURLString = "http://localhost/";
		CannedDataReader.readCannedData(store, getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata"),
				baseURLString);
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton((Source) new SourceImpl(baseURLString + "danbri")));
		Iterator<Date> previousChanges = got.previousChanges(dateFormat
				.parse("20060920123618661"));
		assertEquals(dateFormat.parse("20060920123618661"), previousChanges
				.next());
		assertEquals(dateFormat.parse("19980920123600000"), previousChanges
				.next());
	}

	public void testFollowingDates() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		//ToyClock clock = new ToyClock();
		Store store = getStoreImpl(null);
		String baseURLString = "http://localhost/";
		CannedDataReader.readCannedData(store, getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata"),
				baseURLString);
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton((Source) new SourceImpl(baseURLString + "danbri")));
		// one milli after last modification
		Iterator<Date> followingChanges = got.followingChanges(dateFormat
				.parse("19980920123559999"));
		// should get last modification
		assertEquals(dateFormat.parse("19980920123600000"), followingChanges
				.next());
		// eralier one
		Date nextDate = followingChanges.next();
		assertEquals(dateFormat.parse("20060920123618661"), nextDate);
	}

	public void testFollowingDates2() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		//ToyClock clock = new ToyClock();
		Store store = getStoreImpl(new ToyClock());
		String baseURLString = "http://localhost/";
		CannedDataReader.readCannedData(store, getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata"),
				baseURLString);
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton((Source) new SourceImpl(baseURLString + "danbri")));
		// one milli after last modification
		Iterator<Date> followingChanges = got.followingChanges(dateFormat
				.parse("19980920123600000"));
		Date nextDate = followingChanges.next();
		assertEquals(dateFormat.parse("20060920123618661"), nextDate);
	}
	
	public void notyettestGOTFilter() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		//ToyClock clock = new ToyClock();
		Store store = getStoreImpl(new ToyClock());
		String baseURLString = "http://localhost/";
		CannedDataReader.readCannedData(store, getClass().getResource("/com/hp/hpl/jena/gvs/test/testdata"),
				baseURLString);
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton((Source) new SourceImpl(baseURLString + "danbri")));
		//filter by a resource present only in the older version
		got = got.filter(Collections.singleton((GroundedNode)new NamedNodeImpl("mailto:danbri@porklips.org")));
		// one milli after last modification
		Iterator<Date> followingChanges = got.followingChanges(dateFormat
				.parse("19980920123600000"));
		assertFalse(followingChanges.hasNext());
		Iterator<Date> previousChanges = got.previousChanges(dateFormat
				.parse("19980920123600000"));
		assertTrue(previousChanges.hasNext());
	}

	/*
	 * a second addition of the identical graph, must not cause a new
	 * modification date
	 */
	public void testAddingTheSame() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox 'm'; _a http://xmlns.com/foaf/0.1/mbox 'n'");
		Date date1 = dateFormat.parse("20050607");
		Date date2 = dateFormat.parse("20060912");
		FCAGraph graph = new FCAGraphImpl(model);
		Source source = new SourceImpl("http://example.org/graph-source");
		clock.setTime(date1);
		store.updateGraph(source, graph);
		clock.setTime(date2);
		store.updateGraph(source, graph);;
		GraphOverTime got = store.getGraphOverTime(Collections
				.singleton(source));
		Iterator<Date> followingChanges = got.followingChanges(dateFormat
				.parse("20060101"));
		assertFalse(followingChanges.hasNext());
	}
	
	public void testFGNodeMerging() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model1 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");
		Model model2 = modelWithStatements("_a dc:subject 'hullo'; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");

		Date date1 = dateFormat.parse("20050607");
		Date date2 = dateFormat.parse("20060912");
		FCAGraph graph1 = new FCAGraphImpl(model1);
		FCAGraph graph2 = new FCAGraphImpl(model2);
		Source source = new SourceImpl("http://example.org/graph-source");
		clock.setTime(date1);
		store.updateGraph(source, graph1);
		store = reGetStoreImpl();
		source = new SourceImpl("http://example.org/graph-source");
		clock.setTime(date2);
		store.assertGraph(source, graph2);
		FCAGraph graphR  = store.getGraphOverTime(Collections.singleton(source)).getGraph(dateFormat.parse("20060913"));
		JenaUtil.getModelFromGraph(graphR).write(System.out);
	}
	
	//FIX BaseStoreImp revokegraph
	public void notYetTestPartialFGNodeRevoke() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model1 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox \"foo\"; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");
		Model model2 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");

		Date date1 = dateFormat.parse("20050607");
		Date date2 = dateFormat.parse("20060912");
		FCAGraph graph1 = new FCAGraphImpl(model1);
		FCAGraph graph2 = new FCAGraphImpl(model2);
		Source source = new SourceImpl("http://example.org/graph-source");
		clock.setTime(date1);
		store.updateGraph(source, graph1);
		store = reGetStoreImpl();
		clock.setTime(date2);
		store.revokeGraph(source, graph2);
		FCAGraph graphR  = store.getGraphOverTime(Collections.singleton(source)).getGraph(dateFormat.parse("20060913"));
		JenaUtil.getModelFromGraph(graphR).write(System.out);
	}
	
	public void testPartialFGNodeRevoke2() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model1 = modelWithStatements("_a dc:subject 'sub1'; _a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");
		Model model1b = modelWithStatements("_a http://xmlns.com/foaf/0.1/mbox \"foo\"; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");
		Model model2 = modelWithStatements("_a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/mbox_sha1sum '49f9f8e38c3144e19e09c3e5fa10170242d1360f'");

		Date date1 = dateFormat.parse("20050607");
		Date date1b = dateFormat.parse("20050608");
		Date date2 = dateFormat.parse("20060912");
		FCAGraph graph1 = new FCAGraphImpl(model1);
		FCAGraph graph1b = new FCAGraphImpl(model1b);
		FCAGraph graph2 = new FCAGraphImpl(model2);
		Source source = new SourceImpl("http://example.org/graph-source");
		clock.setTime(date1);
		store.updateGraph(source, graph1);
		clock.setTime(date1b);
		store.assertGraph(source, graph1b);
		store = reGetStoreImpl();
		clock.setTime(date2);
		store.revokeGraph(source, graph2);
		FCAGraph graphR  = store.getGraphOverTime(Collections.singleton(source)).getGraph(dateFormat.parse("20060913"));
		//no exception retrieving graph containing fg-node that doesn't exist the same way as wehn stuff was added
		assertTrue(true);
		JenaUtil.getModelFromGraph(graphR).write(System.out);
	}
	
	public void testTransactions1() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model = modelWithStatements("_a dc:subject 'sub1'; _a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/knows http://example.org/");
		Source source = new SourceImpl("http://example.org/graph-source");
		Date date1 = dateFormat.parse("20050607");
		Date date2 = dateFormat.parse("20050608");
		Date date3 = dateFormat.parse("20060912");
		clock.setTime(date1);
		store.assertGraph(source, new FCAGraphImpl(model));
		clock.setTime(date2);
		store.perform(source, new StoreTransaction() {

			public void execute(SourceStoreView storeView) {
				storeView.revokeAll();
				
			}
			
		});
		Graph retrieved = store.getGraphOverTime(Collections.singleton(source)).getGraph(date3);
		assertEquals(0, retrieved.size());
	}
	
	public void testTransactions2() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model1 = modelWithStatements("_a dc:subject 'sub1'; _a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/knows http://example.org/");
		Model model2 = modelWithStatements("_b dc:subject 'sub2'; _b dc:subject 'hello'; _b http://xmlns.com/foaf/0.1/knows http://example.org/");
		final FCAGraph graph2 = new FCAGraphImpl(model2);
		Source source = new SourceImpl("http://example.org/graph-source");
		Date date1 = dateFormat.parse("20050607");
		Date date2 = dateFormat.parse("20050608");
		Date date3 = dateFormat.parse("20060912");
		clock.setTime(date1);
		store.assertGraph(source, new FCAGraphImpl(model1));
		store.assertGraph(source, graph2);
		clock.setTime(date2);
		store.perform(source, new StoreTransaction() {

			public void execute(SourceStoreView storeView) {
				storeView.revokeAll();
				storeView.assertGraph(graph2);
			}
			
		});
		Graph retrieved = store.getGraphOverTime(Collections.singleton(source)).getGraph(date3);
		assertEquals(graph2, retrieved);
	}
	
	public void testTransactions3() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		ToyClock clock = new ToyClock();
		Store store = getStoreImpl(clock);
		Model model1 = modelWithStatements("_a dc:subject 'sub1'; _a dc:subject 'hello'; _a http://xmlns.com/foaf/0.1/knows http://example.org/; _c dc:subject 'foo'");
		final FCAGraph graph1 = new FCAGraphImpl(model1);
		Model model2 = modelWithStatements("_b dc:subject 'sub2'; _b dc:subject 'hello'; _b http://xmlns.com/foaf/0.1/knows http://example.org/");
		final FCAGraph graph2 = new FCAGraphImpl(model2);
		Source source = new SourceImpl("http://example.org/graph-source");
		Date date1 = dateFormat.parse("20050607");
		Date date2 = dateFormat.parse("20050608");
		Date date3 = dateFormat.parse("20060912");
		clock.setTime(date1);
		store.assertGraph(source, graph1);
		store.assertGraph(source, graph1);
		store.assertGraph(source, graph2);
		clock.setTime(date2);
		store.perform(source, new StoreTransaction() {

			public void execute(SourceStoreView storeView) {
				storeView.revokeGraph(graph1);
			}
			
		});
		Graph retrieved = store.getGraphOverTime(Collections.singleton(source)).getGraph(date3);
		assertEquals(graph2, retrieved);
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

