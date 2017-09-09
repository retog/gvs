/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: SimpleFilterableDecomposition.java,v 1.4 2007/06/28 08:19:14 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl;

import java.util.HashSet;
import java.util.Set;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.impl.GraphUtil;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.NonTerminalMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;

import com.hp.hpl.jena.gvs.FilterableDecomposition;

/**
 * @author reto
 *
 */
public class SimpleFilterableDecomposition implements FilterableDecomposition {

	private ReferenceGroundedDecomposition wrapped;

	/**
	 * 
	 */
	public SimpleFilterableDecomposition(ReferenceGroundedDecomposition wrapped) {
		this.wrapped = wrapped;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.QueryableDecomposition#getContextualMolecules(java.util.Set)
	 */
	public Set<MaximumContextualMolecule> getContextualMolecules(
			Set<GroundedNode> onlyForGroundedNodes) {
		return filter(wrapped.getContextualMolecules(), onlyForGroundedNodes);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.QueryableDecomposition#getFunctionallyGroundedNodes(java.util.Set)
	 */
	public Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes(
			Set<GroundedNode> onlyForGroundedNodes) {
		Set<FunctionallyGroundedNode> result = new HashSet<FunctionallyGroundedNode>();
		FGNODES: for (FunctionallyGroundedNode fgNode : wrapped.getFunctionallyGroundedNodes()) {
			for (NonTerminalMolecule ntMolecule : fgNode.getGroundingMolecules()) {
				for (GroundedNode groundedNode : onlyForGroundedNodes) {
					//TODO move to graph filtering
					//add fg-node if it has a common nt-molecule
					if (groundedNode instanceof FunctionallyGroundedNode) {
						for (NonTerminalMolecule criterionNT : ((FunctionallyGroundedNode)groundedNode).getGroundingMolecules()) {
							if (criterionNT.equals(ntMolecule)) {
								result.add(fgNode);
								continue FGNODES;
							}
						}
					}
					 
					if (GraphUtil.contains(ntMolecule, groundedNode)) {
						result.add(fgNode);
						continue FGNODES;
					}
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.QueryableDecomposition#getTerminalMolecules(java.util.Set)
	 */
	public Set<TerminalMolecule> getTerminalMolecules(
			Set<GroundedNode> onlyForGroundedNodes) {
		return filter(wrapped.getTerminalMolecules(), onlyForGroundedNodes);
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getContextualMolecules()
	 */
	public Set<MaximumContextualMolecule> getContextualMolecules() {
		return wrapped.getContextualMolecules();
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getFunctionallyGroundedNodes()
	 */
	public Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes() {
		return wrapped.getFunctionallyGroundedNodes();
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition#getTerminalMolecules()
	 */
	public Set<TerminalMolecule> getTerminalMolecules() {
		return wrapped.getTerminalMolecules();
	}
	
	private static  <T extends Graph> Set<T> filter(Set<T> moleculeSet, Set<GroundedNode> filter) {
		Set<T> result = new HashSet<T>();
		for (T t : moleculeSet) {
			for (GroundedNode node : filter) {
				if (GraphUtil.contains(t,node)) {
					result.add(t);
					break;
				}
			}
		}
			
		return result;
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

