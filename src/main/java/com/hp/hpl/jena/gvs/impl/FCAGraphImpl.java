/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FCAGraphImpl.java,v 1.5 2007/05/07 18:50:06 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.LiteralNode;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.Node;
import org.wymiwyg.rdf.graphs.PlainLiteralNode;
import org.wymiwyg.rdf.graphs.PropertyNode;
import org.wymiwyg.rdf.graphs.Triple;
import org.wymiwyg.rdf.graphs.TypedLiteralNode;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.fgnodes.impl.FunctionallyGroundedNodeImpl;
import org.wymiwyg.rdf.graphs.fgnodes.impl.InverseFunctionalPropertyNodeImpl;
import org.wymiwyg.rdf.graphs.impl.AnonymizedGraph;
import org.wymiwyg.rdf.graphs.impl.DeAnonymizedGraph;
import org.wymiwyg.rdf.graphs.impl.NodeImpl;
import org.wymiwyg.rdf.graphs.impl.TripleImpl;
import org.wymiwyg.rdf.graphs.impl.TypedLiteralNodeImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.NonTerminalMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.diff.vocabulary.MODELDIFF;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;
import org.wymiwyg.rdf.molecules.functref.impl.ReferenceGroundedDecompositionImpl;
import org.wymiwyg.rdf.molecules.functref.impl.ReferenceGroundedUtil;
import org.wymiwyg.rdf.molecules.impl.SimpleNonTerminalMolecule;
import org.wymiwyg.rdf.molecules.model.modelref.implgraph.ModelReferencingDecompositionImpl;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.FilterableDecomposition;
import com.hp.hpl.jena.mem.TrackingTripleIterator;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This class implements FCAGraph, it does decomposition and reconstruction of
 * the graph as needed. Once a decomposition or a reconstruction is created both
 * graph and decomposition are kept in memory.
 * 
 * Instances of this class are inmutable.
 * 
 * When an instance is accessed as a Jena Graph a mapping from
 * org.wymiwyg.rdf.graphs.NodeS to com.hp.hpl.jena.graph.NodeS is made.
 * 
 * @author reto
 * 
 */
public class FCAGraphImpl extends GraphBase implements FCAGraph {

	private final static URI anyURIDataType;

	private final static PropertyNode nameProp = new InverseFunctionalPropertyNodeImpl(
			MODELDIFF.name.getURI());

	static {
		try {
			anyURIDataType = new URI("http://www.w3.org/2001/XMLSchema#anyURI");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private FilterableDecomposition decomposition = null;

	private Graph graph = null;

	private Map<Node, com.hp.hpl.jena.graph.Node> node2jenaNodeMap = new HashMap<Node, com.hp.hpl.jena.graph.Node>();

	// private Collection<com.hp.hpl.jena.graph.Triple> jenaTriples;

	/**
	 * Creates a FCAGraph using a FilterableDecomposition
	 * 
	 * @param decomposition
	 *            the decomposition to be decorated
	 */
	public FCAGraphImpl(FilterableDecomposition decomposition) {
		super();
		this.decomposition = decomposition;
	}

	/**
	 * Creates a FCAGraph using a graph
	 * 
	 * @param graph
	 *            the graph to be decorated
	 */
	public FCAGraphImpl(Graph graph) {
		super();
		this.graph = graph;
	}

	/**
	 * Creates a FCAGraph using a Model
	 * 
	 * @param model
	 *            the model to be converted
	 */
	public FCAGraphImpl(Model model) {
		super();
		this.graph = JenaUtil.getGraphFromModel(model, true);
	}

	/**
	 * Creates a FCAGraph using a ReferenceGroundedDecomposition
	 * 
	 * @param decomposition
	 *            the decomposition to be decorated
	 */
	public FCAGraphImpl(ReferenceGroundedDecomposition decomposition) {
		super();
		this.decomposition = new SimpleFilterableDecomposition(decomposition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(Triple arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends Triple> arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object triple) {
		if (graph == null)
			createGraph();
		return graph.contains(triple);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> arg0) {
		if (graph == null)
			createGraph();
		return graph.containsAll(arg0);
	}

	@Override
	public boolean equals(Object obj) {
		if (graph == null)
			createGraph();
		return graph.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.FCAGraph#filter(java.util.Set)
	 */
	public FCAGraph filter(Set<GroundedNode> onlyForGroundedNodes) {
		onlyForGroundedNodes = functionalyze(onlyForGroundedNodes);
		Set<GroundedNode> expandedGroundedNodeSet = new HashSet<GroundedNode>();
		expandedGroundedNodeSet.addAll(onlyForGroundedNodes);
		ReconstructedReferenceGroundedDecompositionImpl resultDec = new ReconstructedReferenceGroundedDecompositionImpl();
		Set<FunctionallyGroundedNode> resultFgNodes = resultDec
				.getEditableFunctionallyGroundedNodes();
		for (GroundedNode node : expandedGroundedNodeSet) {
			if (node instanceof FunctionallyGroundedNode) {
				resultFgNodes.add((FunctionallyGroundedNode) node);
			}
		}

		int previousSize = -1;
		while (resultFgNodes.size() > previousSize) {
			previousSize = resultFgNodes.size();
			Set<FunctionallyGroundedNode> addition = getFunctionallyGroundedNodes(expandedGroundedNodeSet);
			resultFgNodes.addAll(addition);
			expandedGroundedNodeSet.addAll(addition);
		}

		Set<TerminalMolecule> resultTerminalMolecules = resultDec
				.getEditableTerminalMolecules();
		resultTerminalMolecules
				.addAll(getTerminalMolecules(expandedGroundedNodeSet));

		Set<MaximumContextualMolecule> resultContextualMolecules = resultDec
				.getEditableContextualMolecules();
		resultContextualMolecules
				.addAll(getContextualMolecules(expandedGroundedNodeSet));

		resultFgNodes.addAll(getContainedFgNodes(resultTerminalMolecules));
		resultFgNodes.addAll(getContainedFgNodes(resultContextualMolecules));

		// add fg-nodes contained in fg-nodes
		previousSize = 0;
		while (resultFgNodes.size() > previousSize) {
			previousSize = resultFgNodes.size();
			Set<FunctionallyGroundedNode> addition = new HashSet<FunctionallyGroundedNode>();
			for (FunctionallyGroundedNode node : resultFgNodes) {
				addition.addAll(getContainedFgNodes(node.getGroundingMolecules()));
			}
			resultFgNodes.addAll(addition);
		}
		return new FCAGraphImpl(resultDec);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getContextualMolecules()
	 */
	public Set<MaximumContextualMolecule> getContextualMolecules() {
		if (decomposition == null)
			createDecomposition();
		return decomposition.getContextualMolecules();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getFunctionallyGroundedNodes()
	 */
	public Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes() {
		if (decomposition == null)
			createDecomposition();
		return decomposition.getFunctionallyGroundedNodes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getTerminalMolecules()
	 */
	public Set<TerminalMolecule> getTerminalMolecules() {
		if (decomposition == null)
			createDecomposition();
		return decomposition.getTerminalMolecules();
	}

	@Override
	public int hashCode() {
		if (graph == null)
			createGraph();
		return graph.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<Triple> iterator() {
		if (graph == null)
			createGraph();
		return graph.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		if (graph == null)
			createGraph();
		return graph.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	public <T> T[] toArray(T[] arg0) {
		if (graph == null)
			createGraph();
		return graph.toArray(arg0);
	}


	private Set<FunctionallyGroundedNode> getContainedFgNodes(Set<? extends Graph> graphs) {
		Set<FunctionallyGroundedNode> resultFgNodes = new HashSet<FunctionallyGroundedNode>();
		for (Graph graph : graphs) {
			for (Triple triple : graph) {
				if (triple.getSubject() instanceof FunctionallyGroundedNode) {
					resultFgNodes.add((FunctionallyGroundedNode) triple
							.getSubject());
				}
				if (triple.getObject() instanceof FunctionallyGroundedNode) {
					resultFgNodes.add((FunctionallyGroundedNode) triple
							.getObject());
				}
			}
		}
		return resultFgNodes;
	}

	private Iterator<com.hp.hpl.jena.graph.Triple> convertNodes(
			final Iterator<Triple> base) {
		return new Iterator<com.hp.hpl.jena.graph.Triple>() {

			public boolean hasNext() {
				return base.hasNext();
			}

			public com.hp.hpl.jena.graph.Triple next() {
				return convertTriple(base.next());
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			private com.hp.hpl.jena.graph.Node convert(Node node) {
				com.hp.hpl.jena.graph.Node result = node2jenaNodeMap.get(node);
				if (result != null)
					return result;
				if (node instanceof GroundedNode) {
					if (node instanceof NamedNode) {
						result = com.hp.hpl.jena.graph.Node
								.createURI(((NamedNode) node).getURIRef());
					} else {
						if (node instanceof PlainLiteralNode) {
							PlainLiteralNode lit = (PlainLiteralNode) node;
							result = com.hp.hpl.jena.graph.Node.createLiteral(
									lit.getLexicalForm(),
									lit.getLocale() == null ? null : lit
											.getLocale().toString(), false);
						} else {
							TypedLiteralNode lit = (TypedLiteralNode) node;
							result = com.hp.hpl.jena.graph.Node.createLiteral(
									lit.getLexicalForm(), null, TypeMapper
											.getInstance().getSafeTypeByName(
													lit.getDataType()
															.toString()));
						}
					}
				} else {
					result = com.hp.hpl.jena.graph.Node.createAnon();
				}
				node2jenaNodeMap.put(node, result);
				return result;
			}

			private com.hp.hpl.jena.graph.Triple convertTriple(Triple triple) {
				return new com.hp.hpl.jena.graph.Triple(convert(triple
						.getSubject()), convert(triple.getPredicate()),
						convert(triple.getObject()));
			}

		};
	}

	/**
	 * 
	 */
	private void createDecomposition() {
		decomposition = new SimpleFilterableDecomposition(
				new ReferenceGroundedDecompositionImpl(
						new ModelReferencingDecompositionImpl(
								new AnonymizedGraph(graph))));

	}

	/**
	 * 
	 */
	private void createGraph() {
		graph = new DeAnonymizedGraph(ReferenceGroundedUtil
				.reconstructGraph(decomposition));

	}

	private Iterator filterTriples(
			final Iterator<com.hp.hpl.jena.graph.Triple> base,
			final TripleMatch filter) {
		return new Iterator<com.hp.hpl.jena.graph.Triple>() {

			com.hp.hpl.jena.graph.Triple next = getNext();

			public boolean hasNext() {
				return next != null;
			}

			public com.hp.hpl.jena.graph.Triple next() {
				com.hp.hpl.jena.graph.Triple result = next;
				next = getNext();
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException();

			}

			private com.hp.hpl.jena.graph.Triple getNext() {
				while (base.hasNext()) {
					com.hp.hpl.jena.graph.Triple nextInBase = base.next();
					if (match(nextInBase, filter)) {
						return nextInBase;
					}
				}
				return null;
			}

			private boolean match(com.hp.hpl.jena.graph.Triple triple,
					TripleMatch filter) {
				if (filter.getMatchSubject() != null) {
					if (!filter.getMatchSubject().equals(triple.getSubject())) {
						return false;
					}
				}
				if (filter.getMatchObject() != null) {
					if (!filter.getMatchObject().equals(triple.getObject())) {
						return false;
					}
				}
				if (filter.getMatchPredicate() != null) {
					if (!filter.getMatchPredicate().equals(
							triple.getPredicate())) {
						return false;
					}
				}
				return true;
			}

		};
	}

	private FunctionallyGroundedNode functionalyze(FunctionallyGroundedNode node) {
		FunctionallyGroundedNodeImpl result = new FunctionallyGroundedNodeImpl();
		for (NonTerminalMolecule molecule : node.getGroundingMolecules()) {
			result.addMolecule(functionalyze(molecule));
		}
		result.markFinalized();
		return result;
	}

	private GroundedNode functionalyze(GroundedNode node) {
		if (node instanceof NamedNode) {
			return functionalyze((NamedNode) node);
		} else {
			if (node instanceof FunctionallyGroundedNode) {
				return functionalyze((FunctionallyGroundedNode) node);
			} else {
				return node;
			}
		}
	}

	private FunctionallyGroundedNode functionalyze(NamedNode node) {
		FunctionallyGroundedNodeImpl result = new FunctionallyGroundedNodeImpl();
		Node afgn = new NodeImpl();
		SimpleNonTerminalMolecule molecule = new SimpleNonTerminalMolecule(afgn);
		String uriString = node.getURIRef();
		LiteralNode uriLit = new TypedLiteralNodeImpl(uriString, anyURIDataType);
		Triple triple = new TripleImpl(afgn, nameProp, uriLit);
		molecule.add(triple);
		molecule.markFinalized();
		result.addMolecule(molecule);
		result.markFinalized();
		result.notifyAllFinalized();
		return result;
	}

	private NonTerminalMolecule functionalyze(NonTerminalMolecule molecule) {
		SimpleNonTerminalMolecule result = new SimpleNonTerminalMolecule(NonTerminalMolecule.GROUNDED_NODE);
		Triple origTriple = molecule.iterator().next();
		Node subject = origTriple.getSubject();
		if (subject instanceof GroundedNode) {
			subject = functionalyze((GroundedNode)subject);
		}
		Node object = origTriple.getObject();
		if (object instanceof GroundedNode) {
			object = functionalyze((GroundedNode)object);
		}
		Triple newTriple = new TripleImpl(subject, origTriple.getPredicate(), object);
		result.add(newTriple);
		return result;
	}

	/**
	 * Replaces NamedNode with FG-nodes
	 * 
	 */
	private Set<GroundedNode> functionalyze(
			Set<GroundedNode> onlyForGroundedNodes) {
		Set<GroundedNode> result = new HashSet<GroundedNode>();
		for (GroundedNode node : onlyForGroundedNodes) {
			result.add(functionalyze(node));
			
		}
		return result;
	}

	private Set<MaximumContextualMolecule> getContextualMolecules(
			Set<GroundedNode> onlyForGroundedNodes) {
		if (decomposition == null)
			createDecomposition();
		return decomposition.getContextualMolecules(onlyForGroundedNodes);
	}


	private Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes(
			Set<GroundedNode> onlyForGroundedNodes) {
		if (decomposition == null)
			createDecomposition();
		return decomposition.getFunctionallyGroundedNodes(onlyForGroundedNodes);
	}

	private Set<TerminalMolecule> getTerminalMolecules(
			Set<GroundedNode> onlyForGroundedNodes) {
		if (decomposition == null)
			createDecomposition();
		return decomposition.getTerminalMolecules(onlyForGroundedNodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#graphBaseFind(com.hp.hpl.jena.graph.TripleMatch)
	 */
	@Override
	protected ExtendedIterator graphBaseFind(TripleMatch m) {
		if (graph == null)
			createGraph();
		return new TrackingTripleIterator(filterTriples(convertNodes(graph
				.iterator()), m));
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

