/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: FilterableDecomposition.java,v 1.3 2007/05/02 10:52:36 rebach Exp $
*/
package com.hp.hpl.jena.gvs;

import java.util.Set;

import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;

/**
 * Extends ReferenceGroundedDecomposition with methods to filter the components to those containing certain grounded nodes
 * 
 * @author reto
 *
 */
public interface FilterableDecomposition extends ReferenceGroundedDecomposition {
	
	/**
	 * @param onlyForGroundedNodes at least one of these is in each of the returned components 
	 * @return the matching teminal molecules
	 */
	public Set<TerminalMolecule> getTerminalMolecules(Set<GroundedNode> onlyForGroundedNodes);
	/**
	 * @param onlyForGroundedNodes at least one of these is in each of the returned components 
	 * @return the matching fg-nodes
	 */
	public Set<FunctionallyGroundedNode> getFunctionallyGroundedNodes(Set<GroundedNode> onlyForGroundedNodes);
	/**
	 * @param onlyForGroundedNodes at least one of these is in each of the returned components 
	 * @return the matching contextula molecules
	 */
	public Set<MaximumContextualMolecule> getContextualMolecules(Set<GroundedNode> onlyForGroundedNodes);
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

