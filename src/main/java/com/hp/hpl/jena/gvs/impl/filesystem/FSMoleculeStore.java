/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FSMoleculeStore.java,v 1.17 2007/05/15 09:11:30 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.rdf.graphs.AbstractGraph;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.Node;
import org.wymiwyg.rdf.graphs.Triple;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.fgnodes.impl.FunctionallyGroundedNodeImpl;
import org.wymiwyg.rdf.graphs.fgnodes.impl.NaturalizedGraph;
import org.wymiwyg.rdf.graphs.fgnodes.impl.Naturalizer;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.impl.NodeImpl;
import org.wymiwyg.rdf.graphs.impl.PropertyNodeImpl;
import org.wymiwyg.rdf.graphs.impl.SimpleGraph;
import org.wymiwyg.rdf.graphs.impl.TripleImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.Molecule;
import org.wymiwyg.rdf.molecules.NonTerminalMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.diff.vocabulary.MODELDIFF;
import org.wymiwyg.rdf.molecules.impl.SimpleContextualMolecule;
import org.wymiwyg.rdf.molecules.impl.SimpleTerminalMolecule;

import com.hp.hpl.jena.gvs.impl.METAMODEL;
import com.hp.hpl.jena.gvs.impl.util.java.BidiMap;
import com.hp.hpl.jena.gvs.impl.util.java.BidiMapImpl;
import com.hp.hpl.jena.gvs.storage.MoleculeStore;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
public class FSMoleculeStore implements MoleculeStore {

	GraphCache cache = new MapGraphCache();

	private static final int slashesPerID = 3;

	/**
	 * @author reto
	 * 
	 */
	public class FSNTMolecule extends AbstractGraph implements
			NonTerminalMolecule {

		private Resource ntMolRes;

		// the problem with a weak reference is that it might get lost during
		// graph isomorphism and
		// thus invalidate existing pairs between the compared graphs
		private Graph triples = null;

		/**
		 * @param ntMolRes
		 */
		public FSNTMolecule(Resource ntMolRes) {
			this.ntMolRes = ntMolRes;
			markFinalized();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#iterator()
		 */
		@Override
		public Iterator<Triple> iterator() {
			return getTripleSet().iterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return getTripleSet().size();
		}

		private Graph getTripleSet() {
			if (triples == null) {
				synchronized (this) {

					if (triples == null) {
						ComponentDirectory componentDir = new ComponentDirectory(
								ntMolRes.getURI());
						synchronized (ntCurrentlyBeingAdded) {
							triples = ntCurrentlyBeingAdded.getKey(componentDir
									.getNode());
						}
						if (triples == null) {
							triples = new MoleculeReader<Graph>() {

								@Override
								protected Graph asMolecule(Graph graphFromModel) {
									Graph deNaturalized = deNaturalize(graphFromModel);
									if (deNaturalized.size() > 1) {
										throw new RuntimeException(
												"nt-molecules should be of size 1");
									}
									Triple triple = deNaturalized.iterator()
											.next();
									Node subject = triple.getSubject();
									if (!(subject instanceof GroundedNode)) {
										subject = NonTerminalMolecule.GROUNDED_NODE;
									}
									Node object = triple.getObject();
									if (!(object instanceof GroundedNode)) {
										object = NonTerminalMolecule.GROUNDED_NODE;
									}
									SimpleGraph result = new SimpleGraph();
									result.add(new TripleImpl(subject, triple
											.getPredicate(), object));
									result.markFinalized();
									return result;
								}

							}.readComponent(componentDir);
						}
					}
				}
			}
			if (triples == null) {
				throw new RuntimeException("couldn't get graph in "
						+ new ComponentDirectory(ntMolRes.getURI()).directory);
			}
			return triples;
		}

	}

	/**
	 * @author reto
	 * 
	 */
	public class FSFGNode extends FunctionallyGroundedNodeImpl {

		private Resource resource;

		private Set<NonTerminalMolecule> groundingMolecules;

		/**
		 * @param resource
		 */
		public FSFGNode(Resource resource) {
			this.resource = resource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode#getGroundingMolecules()
		 */
		public Set<NonTerminalMolecule> getGroundingMolecules() {
			if (groundingMolecules == null) {
				final List<NonTerminalMolecule> resultList = new ArrayList<NonTerminalMolecule>();
				synchronized (this) {
					if (groundingMolecules == null) {

						StmtIterator containsStmts = resource
								.listProperties(METAMODEL.containsNonTerminalMolecule);
						while (containsStmts.hasNext()) {
							Resource ntMolRes = containsStmts.nextStatement()
									.getResource();
							resultList.add(new FSNTMolecule(ntMolRes));
						}
						groundingMolecules = new AbstractSet<NonTerminalMolecule>() {

							@Override
							public Iterator<NonTerminalMolecule> iterator() {
								return resultList.iterator();
							}

							@Override
							public int size() {
								return resultList.size();
							}

						};
					}
				}
			}
			return groundingMolecules;
		}

		/*
		 * public boolean equals(Object obj) { if (this == obj) { return true; }
		 * return getGroundingMolecules().equals( ((FunctionallyGroundedNode)
		 * obj).getGroundingMolecules()); }
		 */

		public int hashCode() {
			if (!resource.hasProperty(METAMODEL.hashCode)) {
				org.wymiwyg.commons.jena.JenaUtil.getExpandedResource(resource,
						3).write(System.out);
			}
			return resource.getProperty(METAMODEL.hashCode).getInt();
		}

		@Override
		public byte[] strongHashCode() {
			if (!resource.hasProperty(METAMODEL.strongHashCode)) {
				org.wymiwyg.commons.jena.JenaUtil.getExpandedResource(resource,
						3).write(System.out);
			}
			return (byte[]) resource.getProperty(METAMODEL.strongHashCode)
					.getLiteral().getValue();
		}
	}

	/**
	 * @author reto
	 * 
	 */
	public interface ComponentReader<T> {
		T readComponent(ComponentDirectory componentDirectory);
	}

	public abstract class MoleculeReader<M extends Graph> implements
			ComponentReader<M> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.gvs.impl.filesystem.FSMoleculeStore.ComponentReader#readComponent(com.hp.hpl.jena.gvs.impl.filesystem.FSMoleculeStore.ComponentDirectory)
		 */

		public M readComponent(ComponentDirectory componentDirectory) {
			File modelFile = new File(componentDirectory.getDirectory(),
					"graph");
			Graph graph = cache.get(modelFile);
			if (graph == null) {
				Model model = ModelFactory.createDefaultModel();
				try {
					URL modelFileUrl = modelFile.toURL();
					if (log.isDebugEnabled()) {
						log.debug("reading: " + modelFileUrl.toString());
					}
					InputStream modelFileInputStream = modelFileUrl
							.openStream();
					try {
						model.read(modelFileInputStream, modelFileUrl
								.toString(), "N-TRIPLE");
					} finally {
						modelFileInputStream.close();
					}

					if (log.isDebugEnabled()) {
						log.debug("finished reading: "
								+ modelFileUrl.toString());
					}
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				} catch (FileNotFoundException e) {
					// the molecule has not yet been finished writing
					return null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				graph = JenaUtil.getGraphFromModel(model, true);
				cache.put(modelFile, graph);
			}
			M result = asMolecule(graph);
			return result;
		}

		/**
		 * @param graphFromModel
		 * @return
		 */
		protected abstract M asMolecule(Graph graphFromModel);

	}

	/**
	 * Represents the directory containg the file(s) describing/storing a
	 * component
	 * 
	 * @author reto
	 * 
	 */
	public class ComponentDirectory {

		private NamedNode node;

		private File directory;

		ComponentDirectory() {
			char[] dirNameChars = new char[slashesPerID + 30];
			int pos = 0;
			for (int i = 0; i < slashesPerID; i++) {
				dirNameChars[pos++] = '/';
				dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
				dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
				dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
				if (i > 0) {
					dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
					dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
					dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
					dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
					if (i > 1) {
						for (int j = 0; j < 13; j++) {
							dirNameChars[pos++] = (char) ('a' + (Math.random() * ('z' - 'a')));
						}
					}
				}

			}
			String randomString = new String(dirNameChars);
			node = new NamedNodeImpl(baseURI + randomString);
			directory = new File(baseDir, randomString);
		}

		/**
		 * @param uri
		 */
		public ComponentDirectory(String uri) {
			node = new NamedNodeImpl(uri);
			String localName = uri.substring(baseURI.length());
			directory = new File(baseDir, localName);
		}

		public File getDirectory() {
			return directory;
		}

		public NamedNode getNode() {
			return node;
		}

		/**
		 * @return
		 */
		public Resource getResource() {
			return indexModel.createResource(node.getURIRef());
		}

	}

	private final String baseURI = "http://gvs.localhost/";

	private File baseDir;

	private Model indexModel = ModelFactory.createDefaultModel();

	private final static Log log = LogFactory.getLog(FSMoleculeStore.class);

	public FSMoleculeStore(File baseDir, GraphCache cache) {
		this.cache = cache;
		if (!baseDir.exists()) {
			throw new RuntimeException("No such directory: " + baseDir);
		}
		this.baseDir = baseDir;
		File[] subDirs = listComponentDirs();// baseDir.listFiles();
		for (File componentDir : subDirs) {
			File metaFile = new File(componentDir, "meta-inf");
			try {
				URL metaFileUrl = metaFile.toURL();
				if (log.isDebugEnabled()) {
					log.debug("reading: " + metaFileUrl.toString());
				}
				InputStream modelFileInputStream = metaFileUrl.openStream();
				try {
					indexModel.read(modelFileInputStream, metaFileUrl
							.toString(), "N-TRIPLE");
				} finally {
					modelFileInputStream.close();
				}

				if (log.isDebugEnabled()) {
					log.debug("finished reading: " + metaFileUrl.toString());
				}
				// indexModel.read(metaFile.toURL().toString(),"N-TRIPLES");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private File[] listComponentDirs() {
		List<File> resultList = new ArrayList<File>(50000);
		listSubDirs(baseDir, resultList, slashesPerID - 1);
		return resultList.toArray(new File[resultList.size()]);
	}

	/**
	 * @param baseDir2
	 * @param resultList
	 * @param i
	 */
	private static void listSubDirs(File dir, List<File> resultList,
			int deepness) {
		if (deepness == 0) {
			resultList.addAll(Arrays.asList(dir.listFiles()));
		} else {
			deepness--;
			for (File file : dir.listFiles()) {
				listSubDirs(file, resultList, deepness);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#addContextualMolecule(org.wymiwyg.rdf.molecules.MaximumContextualMolecule)
	 */
	public NamedNode addContextualMolecule(MaximumContextualMolecule molecule) {
		NamedNode result = getNameOfContextualMolecule(molecule);
		if (result != null) {
			return result;
		}
		try {
			result = storeMolecule(molecule, METAMODEL.ContextualMolecule);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * @param molecule
	 * @param type
	 * @return
	 * @throws IOException
	 */
	private NamedNode storeMolecule(Molecule molecule, Resource type)
			throws IOException {
		ComponentDirectory componentDirectory = new ComponentDirectory();
		File directory = componentDirectory.getDirectory();
		if (!directory.mkdirs()) {
			throw new IOException("Failed to create directory: " + directory);
		}
		writeMetaInf(directory, componentDirectory.getNode(), type, molecule);
		writeGraph(directory, molecule);
		return componentDirectory.getNode();
	}

	/**
	 * this methods makes sure the nt-molecule can be retreived while it is
	 * written, to
	 * 
	 * @param molecule
	 * @return
	 * @throws IOException
	 */
	private NamedNode storeNTMolecule(NonTerminalMolecule molecule)
			throws IOException {
		NamedNode result;
		ComponentDirectory componentDirectory;
		synchronized (ntCurrentlyBeingAdded) {
			result = ntCurrentlyBeingAdded.get(molecule);
			if (result != null) {
				return result;

			}
			componentDirectory = new ComponentDirectory();
			ntCurrentlyBeingAdded.put(molecule, componentDirectory.getNode());
		}
		result = getNameOfMolecule(molecule, METAMODEL.NonTerminalMolecule);
		if (result == null) {
			File directory = componentDirectory.getDirectory();
			directory.mkdirs();
			writeMetaInf(directory, componentDirectory.getNode(),
					METAMODEL.NonTerminalMolecule, molecule);
			writeGraph(directory, molecule);
			result = componentDirectory.getNode();
			synchronized (ntCurrentlyBeingAdded) {
				ntCurrentlyBeingAdded.remove(molecule);
			}
		}
		return result;
	}

	private void writeGraph(File directory, Graph graph) throws IOException {
		NaturalizedGraph naturalizedGraph = new NaturalizedGraph(graph,
				new Naturalizer() {
					public Node naturalize(FunctionallyGroundedNode fgNode,
							Graph graph) {
						Node anonymousNode = new NodeImpl();
						graph.add(new TripleImpl(anonymousNode,
								new PropertyNodeImpl(
										MODELDIFF.functionallyGroundedIn
												.getURI()),
								addFunctionallyGroundedNode(fgNode)));
						return anonymousNode;
					}
				});
		File modelFile = new File(directory, "graph");
		Model model = JenaUtil.getModelFromGraph(naturalizedGraph);
		OutputStream modelOut = new FileOutputStream(modelFile);
		try {
			model.write(modelOut, "N-TRIPLE");
		} finally {
			modelOut.close();
		}
	}

	/**
	 * @param directory
	 * @param node
	 * @param type
	 * @param component
	 * @throws IOException
	 */
	private void writeMetaInf(File directory, NamedNode node, Resource type,
			Object component) throws IOException {
		File metaInfFile = new File(directory, "meta-inf");
		Model metaInfModel = ModelFactory.createDefaultModel();
		Resource componentRes = metaInfModel.createResource(node.getURIRef());
		componentRes.addProperty(RDF.type, type);
		componentRes.addProperty(METAMODEL.hashCode, metaInfModel
				.createTypedLiteral(component.hashCode()));

		OutputStream metaInfOut = new FileOutputStream(metaInfFile);
		try {
			metaInfModel.write(metaInfOut, "N-TRIPLE");
		} finally {
			metaInfOut.close();
		}
		indexModel.add(metaInfModel);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#addFunctionallyGroundedNode(org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode)
	 */
	private BidiMap<FunctionallyGroundedNode, NamedNode> currentlyBeingAdded = new BidiMapImpl<FunctionallyGroundedNode, NamedNode>();

	private BidiMap<NonTerminalMolecule, NamedNode> ntCurrentlyBeingAdded = new BidiMapImpl<NonTerminalMolecule, NamedNode>();

	public NamedNode addFunctionallyGroundedNode(
			FunctionallyGroundedNode functionallyGroundedNode) {
		NamedNode result = getNameOfFunctionallyGroundedNode(functionallyGroundedNode);
		if (result != null) {
			return result;
		}
		try {
			ComponentDirectory componentDirectory;
			synchronized (currentlyBeingAdded) {
				componentDirectory = new ComponentDirectory();
				currentlyBeingAdded.put(functionallyGroundedNode,
						componentDirectory.getNode());
			}

			File directory = componentDirectory.getDirectory();
			directory.mkdirs();
			// writeMetaInf(directory, componentDirectory.getNode(),
			// METAMODEL.FunctionallyGroundedNode, functionallyGroundedNode);
			File metaInfFile = new File(directory, "meta-inf");
			Model metaInfModel = ModelFactory.createDefaultModel();
			Resource componentRes = metaInfModel
					.createResource(componentDirectory.getNode().getURIRef());
			componentRes.addProperty(RDF.type,
					METAMODEL.FunctionallyGroundedNode);
			componentRes.addProperty(METAMODEL.hashCode, metaInfModel
					.createTypedLiteral(functionallyGroundedNode.hashCode()));
			componentRes.addProperty(METAMODEL.strongHashCode, metaInfModel
					.createTypedLiteral(functionallyGroundedNode
							.strongHashCode()));
			for (NonTerminalMolecule molecule : functionallyGroundedNode
					.getGroundingMolecules()) {
				// storeNTMolecule doesn't add duplicate
				NamedNode moleculeName;
				try {
					moleculeName = storeNTMolecule(molecule);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				componentRes.addProperty(METAMODEL.containsNonTerminalMolecule,
						metaInfModel.createResource(moleculeName.getURIRef()));
			}
			OutputStream metaInfOut = new FileOutputStream(metaInfFile);
			try {
				metaInfModel.write(metaInfOut, "N-TRIPLE");
			} finally {
				metaInfOut.close();
			}
			indexModel.add(metaInfModel);
			result = componentDirectory.node;
			synchronized (currentlyBeingAdded) {
				currentlyBeingAdded.remove(functionallyGroundedNode);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#addTerminalMolecule(org.wymiwyg.rdf.molecules.TerminalMolecule)
	 */
	public NamedNode addTerminalMolecule(TerminalMolecule molecule) {
		NamedNode result = getNameOfTerminalMolecule(molecule);
		if (result != null) {
			return result;
		}
		try {
			result = storeMolecule(molecule, METAMODEL.TerminalMolecule);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getContextualMolecule(org.wymiwyg.rdf.graphs.NamedNode)
	 */
	public MaximumContextualMolecule getContextualMolecule(NamedNode moleculeRef) {
		return new MoleculeReader<MaximumContextualMolecule>() {

			@Override
			protected MaximumContextualMolecule asMolecule(Graph graphFromModel) {
				SimpleContextualMolecule result = new SimpleContextualMolecule();
				result.addAll(deNaturalize(graphFromModel));
				result.markFinalized();
				return result;
			}

		}.readComponent(new ComponentDirectory(moleculeRef.getURIRef()));
	}

	Map<NamedNode, WeakReference<FunctionallyGroundedNode>> fgNodeCache = Collections
			.synchronizedMap(new HashMap<NamedNode, WeakReference<FunctionallyGroundedNode>>());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getFunctionallyGroundedNode(org.wymiwyg.rdf.graphs.NamedNode)
	 */
	public FunctionallyGroundedNode getFunctionallyGroundedNode(
			NamedNode functionallyGroundedNodeRef) {
		FunctionallyGroundedNode result = null;
		result = currentlyBeingAdded.getKey(functionallyGroundedNodeRef);
		if (result != null) {
			return result;
		}
		Resource fgRes = indexModel.getResource(functionallyGroundedNodeRef
				.getURIRef());
		if (!fgRes.hasProperty(RDF.type, METAMODEL.FunctionallyGroundedNode)) {
			throw new RuntimeException(functionallyGroundedNodeRef
					+ ": what's that?");
		}
		WeakReference<FunctionallyGroundedNode> resultRef = fgNodeCache
				.get(functionallyGroundedNodeRef);
		if (resultRef != null) {
			result = resultRef.get();

		}
		if (result == null) {
			FSFGNode fsfgNode = new FSFGNode(indexModel
					.getResource(functionallyGroundedNodeRef.getURIRef()));
			fsfgNode.markFinalized();
			fsfgNode.notifyAllFinalized();
			result = fsfgNode;
			fgNodeCache.put(functionallyGroundedNodeRef,
					new WeakReference<FunctionallyGroundedNode>(result));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getNameOfContextualMolecule(org.wymiwyg.rdf.molecules.MaximumContextualMolecule)
	 */
	public NamedNode getNameOfContextualMolecule(
			MaximumContextualMolecule molecule) {
		return getNameOfMolecule(molecule, METAMODEL.ContextualMolecule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getNameOfFunctionallyGroundedNode(org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode)
	 */
	public NamedNode getNameOfFunctionallyGroundedNode(
			FunctionallyGroundedNode functionallyGroundedNode) {
		synchronized (currentlyBeingAdded) {
			NamedNode result = currentlyBeingAdded
					.get(functionallyGroundedNode);
			if (result != null) {
				// TODO the resource may not have its properties. may be a
				// problem with concurrent access
				return result;

			}
		}
		Iterator<ComponentDirectory> hashMatchingComponents = getComponentsByHash(functionallyGroundedNode
				.hashCode());
		while (hashMatchingComponents.hasNext()) {
			ComponentDirectory componentDirectory = hashMatchingComponents
					.next();
			if (componentDirectory.getResource().hasProperty(RDF.type,
					METAMODEL.FunctionallyGroundedNode)) {
				NamedNode result = componentDirectory.getNode();
				if (functionallyGroundedNode
						.equals(getFunctionallyGroundedNode(result))) {
					return result;
				} else {
					log.debug("the hash wasn't strong enough");
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getNameOfTerminalMolecule(org.wymiwyg.rdf.molecules.TerminalMolecule)
	 */
	public NamedNode getNameOfTerminalMolecule(TerminalMolecule molecule) {
		return getNameOfMolecule(molecule, METAMODEL.TerminalMolecule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getTerminalMolecule(org.wymiwyg.rdf.graphs.NamedNode)
	 */
	public TerminalMolecule getTerminalMolecule(NamedNode moleculeRef) {
		return new MoleculeReader<TerminalMolecule>() {

			@Override
			protected TerminalMolecule asMolecule(Graph graphFromModel) {
				SimpleTerminalMolecule result = new SimpleTerminalMolecule();
				result.addAll(deNaturalize(graphFromModel));
				result.markFinalized();
				return result;
			}

		}.readComponent(new ComponentDirectory(moleculeRef.getURIRef()));
	}

	private Graph deNaturalize(Graph graph) {
		Map<Node, FunctionallyGroundedNode> map = new HashMap<Node, FunctionallyGroundedNode>();
		Set<Triple> relevantTriples = new HashSet<Triple>();
		for (Triple triple : graph) {
			if (triple.getPredicate().getURIRef().equals(
					MODELDIFF.functionallyGroundedIn.getURI())) {
				Node subject = triple.getSubject();
				if (!map.containsKey(subject)) {
					map.put(subject,
							getFunctionallyGroundedNode((NamedNode) triple
									.getObject()));
				}
			} else {
				relevantTriples.add(triple);
			}
		}
		SimpleGraph result = new SimpleGraph();
		for (Triple triple : relevantTriples) {
			result.add(new TripleImpl(replaceIfPossible(triple.getSubject(),
					map), triple.getPredicate(), replaceIfPossible(triple
					.getObject(), map)));
		}
		result.markFinalized();
		return result;
	}

	/**
	 * @param subject
	 * @param map
	 * @return
	 */
	private Node replaceIfPossible(Node subject,
			Map<Node, FunctionallyGroundedNode> map) {
		Node result = map.get(subject);
		if (result == null) {
			result = subject;
		}
		return result;
	}

	private NamedNode getNameOfMolecule(Molecule molecule, Resource type) {
		Iterator<ComponentDirectory> hashMatchingComponents = getComponentsByHash(molecule
				.hashCode());
		while (hashMatchingComponents.hasNext()) {
			ComponentDirectory componentDirectory = hashMatchingComponents
					.next();
			if (componentDirectory.getResource().hasProperty(RDF.type, type)) {
				Graph moleculeGraph = new MoleculeReader<Graph>() {

					@Override
					protected Graph asMolecule(Graph graphFromModel) {
						return deNaturalize(graphFromModel);
					}

				}.readComponent(componentDirectory);
				if (moleculeGraph == null) {
					// the molecule has not yet been finished writing, this is
					// not the one we're looking for
					continue;
				}
				if (moleculeGraph.equals(molecule)) {
					return componentDirectory.getNode();
				} else {
					log.debug("hash too weak");
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param deleteObsoltete
	 * @return a map mapping from obsolete and deleteted component-names to the
	 *         kept version
	 */

	public Map<NamedNode, NamedNode> checkAndFixConsistency(
			boolean deleteObsoltete) {
		Map<NamedNode, NamedNode> result = new HashMap<NamedNode, NamedNode>();
		checkConsistency(result, METAMODEL.NonTerminalMolecule);
		checkConsistency(result, METAMODEL.ContextualMolecule);
		checkConsistency(result, METAMODEL.TerminalMolecule);
		checkConsistency(result, METAMODEL.FunctionallyGroundedNode);
		return result;
	}

	private void checkConsistency(Map<NamedNode, NamedNode> result,
			Resource type) {
		Iterator<ComponentDirectory> hashMatchingComponents = getComponentsByType(type);
		BidiMap<NamedNode, Graph> foundMolecules = new BidiMapImpl<NamedNode, Graph>();
		while (hashMatchingComponents.hasNext()) {
			ComponentDirectory componentDirectory = hashMatchingComponents
					.next();
			Graph moleculeGraph = new MoleculeReader<Graph>() {

				@Override
				protected Graph asMolecule(Graph graphFromModel) {
					return deNaturalize(graphFromModel);
				}

			}.readComponent(componentDirectory);
			if (moleculeGraph == null) {
				// the molecule has not yet been finished writing, this is not
				// the one we're looking for
				continue;
			}
			if (foundMolecules.containsValue(moleculeGraph)) {
				System.out.println("double " + type + ": "
						+ componentDirectory.getNode() + " and "
						+ foundMolecules.getKey(moleculeGraph));
				System.out.println("details: "
						+ componentDirectory.getDirectory().lastModified()
						+ " and "
						+ new ComponentDirectory(foundMolecules.getKey(
								moleculeGraph).getURIRef()).getDirectory()
								.lastModified());

			} else {
				foundMolecules.put(componentDirectory.getNode(), moleculeGraph);
			}
		}
	}

	/**
	 * @param i
	 * @return
	 */
	private Iterator<ComponentDirectory> getComponentsByHash(int hashCode) {
		Literal hashLit = indexModel.createTypedLiteral(hashCode);
		final ResIterator resIterator = indexModel.listSubjectsWithProperty(
				METAMODEL.hashCode, hashLit);
		return new Iterator<ComponentDirectory>() {

			public boolean hasNext() {
				return resIterator.hasNext();
			}

			public ComponentDirectory next() {
				return new ComponentDirectory(resIterator.nextResource()
						.getURI());
			}

			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
	}

	private Iterator<ComponentDirectory> getComponentsByType(Resource type) {
		final ResIterator resIterator = indexModel.listSubjectsWithProperty(
				RDF.type, type);
		return new Iterator<ComponentDirectory>() {

			public boolean hasNext() {
				return resIterator.hasNext();
			}

			public ComponentDirectory next() {
				return new ComponentDirectory(resIterator.nextResource()
						.getURI());
			}

			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
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

