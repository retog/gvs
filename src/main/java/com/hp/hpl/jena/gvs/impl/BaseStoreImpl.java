/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: BaseStoreImpl.java,v 1.20 2007/07/02 11:44:46 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl;

import java.security.AccessController;
import java.security.Policy;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.impl.GraphUtil;
import org.wymiwyg.rdf.graphs.impl.SourceNodeNotFoundException;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;
import org.wymiwyg.rdf.molecules.functref.impl.FgNodeMerger;
import org.wymiwyg.rdf.molecules.impl.SimpleContextualMolecule;
import org.wymiwyg.rdf.molecules.impl.SimpleTerminalMolecule;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.SourceStoreView;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.StoreException;
import com.hp.hpl.jena.gvs.StoreTransaction;
import com.hp.hpl.jena.gvs.security.GVSImpersonatePermission;
import com.hp.hpl.jena.gvs.security.GVSSetClockPermission;
import com.hp.hpl.jena.gvs.security.NoRestrictionsPolicy;
import com.hp.hpl.jena.gvs.storage.MetaStore;
import com.hp.hpl.jena.gvs.storage.MoleculeStore;

/**
 * @author reto
 * 
 */
public class BaseStoreImpl implements Store {

	/**
	 * @author reto
	 * 
	 */
	public class BaseStoreDecomposition implements
			ReferenceGroundedDecomposition {

		private Set<MaximumContextualMolecule> cMolecules;
		private Map<FunctionallyGroundedNode, FunctionallyGroundedNode> fgNodeMap;
		private Set<FunctionallyGroundedNode> fgNodes;
		private Set<TerminalMolecule> tMolecules;
		private Date moment;
		private Set<Source> sources;

		/**
		 * @param sources
		 * @param moment
		 */
		public BaseStoreDecomposition(Set<Source> sources, Date moment) {
			this.sources = sources;
			this.moment = moment;
			synchronized (BaseStoreImpl.this) {
				Set<NamedNode> fgNodeRefs = metaStore.getAsserted(sources,
						moment, METAMODEL.FunctionallyGroundedNode);

				Set<FunctionallyGroundedNode> unmergedFGNodes = new HashSet<FunctionallyGroundedNode>();
				for (NamedNode fgNodeRef : fgNodeRefs) {
					FunctionallyGroundedNode fgNode = moleculeStore
							.getFunctionallyGroundedNode(fgNodeRef);
					unmergedFGNodes.add(fgNode);
				}
				fgNodeMap = new HashMap<FunctionallyGroundedNode, FunctionallyGroundedNode>();
				for (FunctionallyGroundedNode fgNode : unmergedFGNodes) {
					fgNodeMap.put(fgNode, fgNode);
				}
				fgNodeMap = new FgNodeMerger<FunctionallyGroundedNode>(
						fgNodeMap);
				fgNodes = new HashSet<FunctionallyGroundedNode>(fgNodeMap
						.values());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getContextualMolecules()
		 */
		public Set<MaximumContextualMolecule> getContextualMolecules() {
			if (cMolecules == null) {
				synchronized (BaseStoreImpl.this) {
					Set<NamedNode> cMoleculeRefs = metaStore.getAsserted(
							sources, moment, METAMODEL.ContextualMolecule);

					cMolecules = new HashSet<MaximumContextualMolecule>();
					for (NamedNode cMoleculeRef : cMoleculeRefs) {
						MaximumContextualMolecule cMolecule = moleculeStore
								.getContextualMolecule(cMoleculeRef);
						cMolecules.add(cMolecule);

					}
					for (Entry<FunctionallyGroundedNode, FunctionallyGroundedNode> entry : fgNodeMap
							.entrySet()) {
						FunctionallyGroundedNode orig = entry.getKey();
						FunctionallyGroundedNode current = entry.getValue();
						if (!current.equals(orig)) {
							Set<MaximumContextualMolecule> originalContextualMolecules = cMolecules;
							cMolecules = new HashSet<MaximumContextualMolecule>();
							for (MaximumContextualMolecule molecule : originalContextualMolecules) {
								try {
									SimpleContextualMolecule replacement = new GraphUtil<SimpleContextualMolecule>()
											.replaceNode(
													molecule,
													orig,
													current,
													new SimpleContextualMolecule());
									replacement.markFinalized();
									cMolecules.add(replacement);
								} catch (SourceNodeNotFoundException e) {
									cMolecules.add(molecule);
								}
							}
						}
					}
				}
			}
			return Collections.unmodifiableSet(cMolecules);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getFunctionallyGroundedNodes()
		 */
		public Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes() {
			return Collections.unmodifiableSet(fgNodes);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getTerminalMolecules()
		 */
		public Set<TerminalMolecule> getTerminalMolecules() {
			if (tMolecules == null) {
				synchronized (BaseStoreImpl.this) {
					Set<NamedNode> tMoleculeRefs = metaStore.getAsserted(
							sources, moment, METAMODEL.TerminalMolecule);
					tMolecules = new HashSet<TerminalMolecule>();
					for (NamedNode tMoleculesRef : tMoleculeRefs) {
						TerminalMolecule tMolecule = moleculeStore
								.getTerminalMolecule(tMoleculesRef);
						tMolecules.add(tMolecule);
					}
					for (Entry<FunctionallyGroundedNode, FunctionallyGroundedNode> entry : fgNodeMap
							.entrySet()) {
						FunctionallyGroundedNode orig = entry.getKey();
						FunctionallyGroundedNode current = entry.getValue();
						if (!current.equals(orig)) {
							Set<TerminalMolecule> originalTerminalMolecules = tMolecules;
							tMolecules = new HashSet<TerminalMolecule>();
							for (TerminalMolecule molecule : originalTerminalMolecules) {
								try {
									SimpleTerminalMolecule replacement = new GraphUtil<SimpleTerminalMolecule>()
											.replaceNode(
													molecule,
													orig,
													current,
													new SimpleTerminalMolecule());
									replacement.markFinalized();
									tMolecules.add(replacement);
								} catch (SourceNodeNotFoundException e) {
									tMolecules.add(molecule);
								}
							}
						}
					}
				}
			}
			return Collections.unmodifiableSet(tMolecules);
		}

	}

	/**
	 * @author reto
	 * 
	 */
	public class BaseSourceStoreView implements SourceStoreView {

		private Source source;
		private Date moment;
		private Set<MaximumContextualMolecule> addingCMolecules = new HashSet<MaximumContextualMolecule>();
		private Set<MaximumContextualMolecule> removingCMolecules = new HashSet<MaximumContextualMolecule>();
		private Set<TerminalMolecule> addingTMolecules = new HashSet<TerminalMolecule>();
		private Set<TerminalMolecule> removingTMolecules = new HashSet<TerminalMolecule>();
		private Set<FunctionallyGroundedNode> addingFGNodes = new HashSet<FunctionallyGroundedNode>();
		private Set<FunctionallyGroundedNode> removingFGNodes = new HashSet<FunctionallyGroundedNode>();

		/**
		 * @param source
		 * @param moment
		 */
		public BaseSourceStoreView(Source source, Date moment) {
			this.source = source;
			this.moment = moment;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.gvs.SourceStoreView#assertGraph(com.hp.hpl.jena.gvs.FCAGraph)
		 */
		public void assertGraph(FCAGraph graph) {
			addingCMolecules.addAll(graph.getContextualMolecules());
			removingCMolecules.removeAll(graph.getContextualMolecules());
			addingTMolecules.addAll(graph.getTerminalMolecules());
			removingTMolecules.removeAll(graph.getTerminalMolecules());
			addingFGNodes.addAll(graph.getFunctionallyGroundedNodes());

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.gvs.SourceStoreView#getGraph()
		 */
		public FCAGraph getGraph() {
			return new FCAGraphImpl(getDecomposition(Collections
					.singleton(source), moment));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.gvs.SourceStoreView#revokeGraph(com.hp.hpl.jena.gvs.FCAGraph)
		 */
		public void revokeGraph(FCAGraph graph) {
			removingCMolecules.addAll(graph.getContextualMolecules());
			addingCMolecules.removeAll(graph.getContextualMolecules());
			removingTMolecules.addAll(graph.getTerminalMolecules());
			addingTMolecules.removeAll(graph.getTerminalMolecules());
			// TODO smart revoking of the nt-molecules of the fg-nodes that are
			// not used in other statements
			// removingFGNodes.addAll(graph.getFunctionallyGroundedNodes());

		}

		private void commit() {
			AccessController.checkPermission(new GVSImpersonatePermission(
					source));
			revokeCMolecules(source, removingCMolecules, moment);
			addCMolecules(source, addingCMolecules, moment);
			revokeTMolecules(source, removingTMolecules, moment);
			addTMolecules(source, addingTMolecules, moment);
			addFGNodes(source, addingFGNodes, moment);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.gvs.SourceStoreView#revokeAll()
		 */
		public void revokeAll() {
			addingCMolecules.clear();
			addingTMolecules.clear();
			addingFGNodes.clear();
			FCAGraph currentGraph = getGraph();
			removingCMolecules.addAll(currentGraph.getContextualMolecules());
			removingTMolecules.addAll(currentGraph.getTerminalMolecules());
			removingFGNodes.addAll(currentGraph.getFunctionallyGroundedNodes());
		}

	}

	private Clock clock;
	private MetaStore metaStore;
	private MoleculeStore moleculeStore;

	static {
		// Policy.setPolicy(new DefaultPolicy(getGraphOverTime(new
		// HashSet<Source>())));
		Policy.setPolicy(new NoRestrictionsPolicy());
	}

	/**
	 * @param metaStore
	 * @param moleculeStore
	 */
	public BaseStoreImpl(MetaStore metaStore, MoleculeStore moleculeStore) {
		this();
		this.metaStore = metaStore;
		this.moleculeStore = moleculeStore;
	}

	/**
	 * @param metaStore
	 * @param moleculeStore
	 * @param clock
	 */
	public BaseStoreImpl(MetaStore metaStore, MoleculeStore moleculeStore,
			Clock clock) {
		this(clock);
		this.metaStore = metaStore;
		this.moleculeStore = moleculeStore;
	}

	/**
	 * subclasses invoking this constructor will use seMoleculeStore and
	 * -MetaStore
	 * 
	 * 
	 */
	protected BaseStoreImpl() {
		this(new Clock() {

			public Date getTime() {
				return new Date();
			}

		});
	}

	/**
	 * @param clock
	 */
	protected BaseStoreImpl(Clock clock) {
		checkClock(clock);
		this.clock = clock;
	}

	/**
	 * @param clock
	 */
	private void checkClock(Clock clock) {
		// TODO check that current time according to clock is not earlier than
		// the newest in the store

	}

	public synchronized void assertGraph(Source source, FCAGraph graph) {
		AccessController.checkPermission(new GVSImpersonatePermission(source));
		ReferenceGroundedDecomposition dec = graph;
		Date moment = clock.getTime();
		addFGNodes(source, dec.getFunctionallyGroundedNodes(), moment);
		addTMolecules(source, dec.getTerminalMolecules(), moment);
		addCMolecules(source, dec.getContextualMolecules(), moment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.Store#changeGraph(com.hp.hpl.jena.gvs.Source,
	 *      com.hp.hpl.jena.gvs.FCAGraph, com.hp.hpl.jena.gvs.FCAGraph)
	 */
	public void changeGraph(Source source, FCAGraph revokedGraph,
			FCAGraph assertedGraph) {
		AccessController.checkPermission(new GVSImpersonatePermission(source));
		Date moment = clock.getTime();

		// c-mols
		Set<MaximumContextualMolecule> newCMols = new HashSet<MaximumContextualMolecule>(
				assertedGraph.getContextualMolecules());
		newCMols.removeAll(revokedGraph.getContextualMolecules());
		addCMolecules(source, newCMols, moment);
		Set<MaximumContextualMolecule> revokedCMols = new HashSet<MaximumContextualMolecule>(
				revokedGraph.getContextualMolecules());
		revokedCMols.removeAll(assertedGraph.getContextualMolecules());
		revokeCMolecules(source, revokedCMols, moment);
		// t-mols
		Set<TerminalMolecule> newTMols = new HashSet<TerminalMolecule>(
				assertedGraph.getTerminalMolecules());
		newTMols.removeAll(revokedGraph.getTerminalMolecules());
		addTMolecules(source, newTMols, moment);
		Set<TerminalMolecule> revokedTMols = new HashSet<TerminalMolecule>(
				revokedGraph.getTerminalMolecules());
		revokedTMols.removeAll(assertedGraph.getTerminalMolecules());
		revokeTMolecules(source, revokedTMols, moment);
		// fg-nodes
		Set<FunctionallyGroundedNode> newFGNodes = new HashSet<FunctionallyGroundedNode>(
				assertedGraph.getFunctionallyGroundedNodes());
		newFGNodes.removeAll(revokedGraph.getFunctionallyGroundedNodes());
		addFGNodes(source, newFGNodes, moment);
		Set<FunctionallyGroundedNode> revokedFGNodes = new HashSet<FunctionallyGroundedNode>(
				revokedGraph.getFunctionallyGroundedNodes());
		revokedFGNodes.removeAll(assertedGraph.getFunctionallyGroundedNodes());
		revokeFGNodes(source, revokedFGNodes, moment);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.Store#getClock()
	 */
	public Clock getClock() {
		return clock;
	}

	public GraphOverTime getGraphOverTime(final Set<Source> sources) {
		return new GraphOverTime() {

			public Iterator<Date> followingChanges(Date d) {
				synchronized (BaseStoreImpl.this) {
					return getDateList(sources, d, true);
				}
			}

			public FCAGraph getGraph(Date moment) {
				ReferenceGroundedDecomposition dec = getDecomposition(sources,
						moment);
				return new FCAGraphImpl(dec);
			}

			public Iterator<Date> previousChanges(Date d) {
				synchronized (BaseStoreImpl.this) {
					return getDateList(sources, d, false);
				}
			}

			public GraphOverTime filter(Set<GroundedNode> onlyForGroundedNodes) {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}

	public Set<Source> getSources() {
		return metaStore.getSources();
	}

	public synchronized void revokeGraph(Source source, FCAGraph graph) {
		// not that easy: find existing assertions
		// processGraph(source, graph, moment, METAMODEL.revocationTime);
		AccessController.checkPermission(new GVSImpersonatePermission(source));
		ReferenceGroundedDecomposition dec = graph;
		// TODO smart revoking of the nt-molecules of the fg-nodes that are not
		// used in other statements
		Date moment = clock.getTime();
		// revokeFGNodes(source, dec.getFunctionallyGroundedNodes(), moment);
		revokeTMolecules(source, dec.getTerminalMolecules(), moment);
		revokeCMolecules(source, dec.getContextualMolecules(), moment);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.Store#setClock(com.hp.hpl.jena.gvs.Clock)
	 */
	public void setClock(Clock clock) {
		AccessController.checkPermission(new GVSSetClockPermission());
		this.clock = clock;

	}

	public synchronized void updateGraph(Source source,
			final FCAGraph assertedGraph) {
		AccessController.checkPermission(new GVSImpersonatePermission(source));

		/*
		 * Doesn't wor as it doesn't remove fg-nodes perform(source, new
		 * StoreTransaction() {
		 * 
		 * public void execute(SourceStoreView storeView) { FCAGraph oldGraph =
		 * storeView.getGraph(); storeView.revokeGraph(oldGraph);
		 * storeView.assertGraph(graph); }
		 * 
		 * });
		 */
		// TODO avoid unnecessary checking if component already present in
		// metastore
		Date moment = clock.getTime();
		FCAGraph revokedGraph = getGraphOverTime(Collections.singleton(source))
				.getGraph(moment);
		// c-mols
		Set<MaximumContextualMolecule> newCMols = new HashSet<MaximumContextualMolecule>(
				assertedGraph.getContextualMolecules());
		newCMols.removeAll(revokedGraph.getContextualMolecules());
		addCMolecules(source, newCMols, moment);
		Set<MaximumContextualMolecule> revokedCMols = new HashSet<MaximumContextualMolecule>(
				revokedGraph.getContextualMolecules());
		revokedCMols.removeAll(assertedGraph.getContextualMolecules());
		revokeCMolecules(source, revokedCMols, moment);
		// t-mols
		Set<TerminalMolecule> newTMols = new HashSet<TerminalMolecule>(
				assertedGraph.getTerminalMolecules());
		newTMols.removeAll(revokedGraph.getTerminalMolecules());
		addTMolecules(source, newTMols, moment);
		Set<TerminalMolecule> revokedTMols = new HashSet<TerminalMolecule>(
				revokedGraph.getTerminalMolecules());
		revokedTMols.removeAll(assertedGraph.getTerminalMolecules());
		revokeTMolecules(source, revokedTMols, moment);
		// fg-nodes
		Set<FunctionallyGroundedNode> newFGNodes = new HashSet<FunctionallyGroundedNode>(
				assertedGraph.getFunctionallyGroundedNodes());
		newFGNodes.removeAll(revokedGraph.getFunctionallyGroundedNodes());
		addFGNodes(source, newFGNodes, moment);
		Set<FunctionallyGroundedNode> revokedFGNodes = new HashSet<FunctionallyGroundedNode>(
				revokedGraph.getFunctionallyGroundedNodes());
		revokedFGNodes.removeAll(assertedGraph.getFunctionallyGroundedNodes());
		revokeFGNodes(source, revokedFGNodes, moment);

	}

	/**
	 * @param contextualMolecules
	 * @param assertion
	 */
	private void addCMolecules(Source source,
			Set<MaximumContextualMolecule> contextualMolecules, Date moment) {
		/*
		 * addMolecules(contextualMolecules, sourceRes, momentLit,
		 * cMoleculesMap, METAMODEL.ContextualMolecule);
		 */
		for (MaximumContextualMolecule molecule : contextualMolecules) {
			NamedNode moleculeDescription = moleculeStore
					.addContextualMolecule(molecule);
			metaStore.assertComponent(source, moleculeDescription, moment,
					METAMODEL.ContextualMolecule);

		}

	}

	/**
	 * @param source
	 * @param functionallyGroundedNodes
	 * @param moment
	 * @param moment
	 */
	private void addFGNodes(Source source,
			Set<FunctionallyGroundedNode> functionallyGroundedNodes, Date moment) {
		for (FunctionallyGroundedNode fgNode : functionallyGroundedNodes) {
			NamedNode fgNodeRef = moleculeStore
					.addFunctionallyGroundedNode(fgNode);
			metaStore.assertComponent(source, fgNodeRef, moment,
					METAMODEL.FunctionallyGroundedNode);
		}

	}

	private void addTMolecules(Source source,
			Set<TerminalMolecule> terminalMolecules, Date moment) {
		for (TerminalMolecule molecule : terminalMolecules) {
			NamedNode moleculeDescription = moleculeStore
					.addTerminalMolecule(molecule);
			metaStore.assertComponent(source, moleculeDescription, moment,
					METAMODEL.TerminalMolecule);

		}

	}

	/**
	 * 
	 * @param sources
	 * @param moment
	 * @param forward
	 *            determines also the order of the list
	 * @return
	 */
	private Iterator<Date> getDateList(Set<Source> sources, Date moment,
			final boolean forward) {
		if (forward) {
			return metaStore.getFollowingChanges(sources, moment);
		} else {
			return metaStore.getPreviousChanges(sources, moment);
		}

	}

	/**
	 * 
	 */
	private ReferenceGroundedDecomposition getDecomposition(
			Set<Source> sources, Date moment) {
		return new BaseStoreDecomposition(sources, moment);

	}

	/**
	 * @param source
	 * @param contextualMolecules
	 * @param moment
	 */
	private void revokeCMolecules(Source source,
			Set<MaximumContextualMolecule> contextualMolecules, Date moment) {

		for (MaximumContextualMolecule cMolecule : contextualMolecules) {
			NamedNode componentName = moleculeStore
					.getNameOfContextualMolecule(cMolecule);// MoleculesMap.get(cMolecule);
			if (componentName == null) {
				throw new StoreException(
						"Revoking compononent unknown to the store");
			}
			metaStore.revokeComponent(source, componentName, moment);
		}

	}

	/**
	 * @param functionallyGroundedNodes
	 * @param momentLit
	 */
	private void revokeFGNodes(Source source,
			Set<FunctionallyGroundedNode> functionallyGroundedNodes, Date moment) {
		for (FunctionallyGroundedNode fgNode : functionallyGroundedNodes) {
			NamedNode componentName = moleculeStore
					.getNameOfFunctionallyGroundedNode(fgNode);// fgNodesMap.get(fgNode);
			if (componentName == null) {
				throw new StoreException(
						"Revoking compononent unknown to the store");
			}
			metaStore.revokeComponent(source, componentName, moment);
		}

	}

	/**
	 * @param source
	 * @param terminalMolecules
	 * @param moment
	 */
	private void revokeTMolecules(Source source,
			Set<TerminalMolecule> terminalMolecules, Date moment) {

		for (TerminalMolecule tMolecule : terminalMolecules) {
			NamedNode componentName = moleculeStore
					.getNameOfTerminalMolecule(tMolecule);// tMoleculesMap.get(tMolecule);
			if (componentName == null) {
				throw new StoreException(
						"Revoking compononent unknown to the store");
			}
			metaStore.revokeComponent(source, componentName, moment);
		}

	}

	/**
	 * subclasses using one of the protected constructor use this method
	 * 
	 * @param metaStore
	 *            the Metastore to be set
	 */
	protected void setMetaStore(MetaStore metaStore) {
		this.metaStore = metaStore;
	}

	/**
	 * subclasses using one of the protected constructor use this method
	 * 
	 * @param moleculeStore
	 *            the MoleculeStore to be set
	 */
	protected void setMoleculeStore(MoleculeStore moleculeStore) {
		this.moleculeStore = moleculeStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.Store#perform(com.hp.hpl.jena.gvs.Source,
	 *      com.hp.hpl.jena.gvs.StoreTransaction)
	 */
	public void perform(Source source, StoreTransaction transaction) {
		Date moment = clock.getTime();
		// TODO lock on source
		BaseSourceStoreView storeView = new BaseSourceStoreView(source, moment);
		transaction.execute(storeView);
		storeView.commit();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.Store#close()
	 */
	public void close() {
		metaStore = null;
		moleculeStore = null;
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

