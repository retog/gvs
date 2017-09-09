/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: ReconstructedReferenceGroundedDecompositionImpl.java,v 1.2 2007/05/15 14:24:42 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.impl.GraphUtil;
import org.wymiwyg.rdf.graphs.impl.SourceNodeNotFoundException;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;
import org.wymiwyg.rdf.molecules.functref.impl.FgNodeMerger;
import org.wymiwyg.rdf.molecules.impl.SimpleContextualMolecule;
import org.wymiwyg.rdf.molecules.impl.SimpleTerminalMolecule;

/**
 * An implementation of
 * <code>org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition</code>
 * where the elements of its sets of components are filled in accessing the
 * editable sets provided by its methods of default visibility.
 * 
 * This class does not do any consistency checking, i.e. the code intsnaciating
 * it and filling its values must take care of providing every
 * <code>FunctionallyGroundedNode</code> referenced in another component.
 * 
 * @author reto
 */
public class ReconstructedReferenceGroundedDecompositionImpl implements
		ReferenceGroundedDecomposition {

	private Set<TerminalMolecule> editableTerminalMolecules = new HashSet<TerminalMolecule>();

	private Set<FunctionallyGroundedNode> editableFunctionallyGroundedNodes = new HashSet<FunctionallyGroundedNode>();

	private Set<MaximumContextualMolecule> editableContextualMolecules = new HashSet<MaximumContextualMolecule>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getTerminalMolecules()
	 */
	public Set<TerminalMolecule> getTerminalMolecules() {
		return Collections.unmodifiableSet(editableTerminalMolecules);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getFunctionallyGroundedNodes()
	 */
	public Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes() {
		return Collections.unmodifiableSet(editableFunctionallyGroundedNodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getContextualMolecules()
	 */
	public Set<MaximumContextualMolecule> getContextualMolecules() {
		return Collections.unmodifiableSet(editableContextualMolecules);
	}

	Set<MaximumContextualMolecule> getEditableContextualMolecules() {
		return editableContextualMolecules;
	}

	Set<FunctionallyGroundedNode> getEditableFunctionallyGroundedNodes() {
		return editableFunctionallyGroundedNodes;
	}

	Set<TerminalMolecule> getEditableTerminalMolecules() {
		return editableTerminalMolecules;
	}

	/**
	 * 
	 */
	

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
