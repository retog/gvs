/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: MemoryMoleculeStore.java,v 1.1 2006/11/01 11:32:22 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl.memory;

import org.wymiwyg.commons.util.Util;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;

import com.hp.hpl.jena.gvs.impl.util.java.BidiMap;
import com.hp.hpl.jena.gvs.impl.util.java.BidiMapImpl;
import com.hp.hpl.jena.gvs.storage.MoleculeStore;

/**
 * @author reto
 *
 */
public class MemoryMoleculeStore implements MoleculeStore {

	private BidiMap<MaximumContextualMolecule, NamedNode> cMoleculesMap = new BidiMapImpl<MaximumContextualMolecule, NamedNode>();

	private BidiMap<FunctionallyGroundedNode, NamedNode> fgNodesMap = new BidiMapImpl<FunctionallyGroundedNode, NamedNode>();

	private BidiMap<TerminalMolecule, NamedNode> tMoleculesMap = new BidiMapImpl<TerminalMolecule, NamedNode>();

	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#addContextualMolecule(org.wymiwyg.rdf.molecules.ContextualMolecule)
	 */
	public NamedNode addContextualMolecule(MaximumContextualMolecule molecule) {
		NamedNode result = cMoleculesMap.get(molecule);
		if (result == null) {
			result = createNewStorageURIRef();
			cMoleculesMap.put(molecule, result);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#addFunctionallyGroundedNode(org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode)
	 */
	public NamedNode addFunctionallyGroundedNode(
			FunctionallyGroundedNode functionallyGroundedNode) {
		NamedNode result = fgNodesMap.get(functionallyGroundedNode);
		if (result == null) {
			result = createNewStorageURIRef();
			fgNodesMap.put(functionallyGroundedNode, result);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#addTerminalMolecule(org.wymiwyg.rdf.molecules.TerminalMolecule)
	 */
	public NamedNode addTerminalMolecule(TerminalMolecule molecule) {
		NamedNode result = tMoleculesMap.get(molecule);
		if (result == null) {
			result = createNewStorageURIRef();
			tMoleculesMap.put(molecule, result);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getContextualMolecule(org.wymiwyg.rdf.graphs.NamedNode)
	 */
	public MaximumContextualMolecule getContextualMolecule(NamedNode moleculeRef) {
		return cMoleculesMap.getKey(moleculeRef);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getFunctionallyGroundedNode(org.wymiwyg.rdf.graphs.NamedNode)
	 */
	public FunctionallyGroundedNode getFunctionallyGroundedNode(
			NamedNode functionallyGroundedNodeRef) {
		return fgNodesMap.getKey(functionallyGroundedNodeRef);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getTerminalMolecule(org.wymiwyg.rdf.graphs.NamedNode)
	 */
	public TerminalMolecule getTerminalMolecule(NamedNode moleculeRef) {
		return tMoleculesMap.getKey(moleculeRef);
	}
	
	private NamedNode createNewStorageURIRef() {
		return new NamedNodeImpl(Util.createURN5());
		//return new NamedNodeImpl(baseURI + Util.createRandomString(8));
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getNameOfContextualMolecule(org.wymiwyg.rdf.molecules.MaximumContextualMolecule)
	 */
	public NamedNode getNameOfContextualMolecule(MaximumContextualMolecule molecule) {
		return cMoleculesMap.get(molecule);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getNameOfFunctionallyGroundedNode(org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode)
	 */
	public NamedNode getNameOfFunctionallyGroundedNode(FunctionallyGroundedNode functionallyGroundedNode) {
		return fgNodesMap.get(functionallyGroundedNode);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MoleculeStore#getNameOfTerminalMolecule(org.wymiwyg.rdf.molecules.TerminalMolecule)
	 */
	public NamedNode getNameOfTerminalMolecule(TerminalMolecule molecule) {
		return tMoleculesMap.get(molecule);
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

