/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: SimpleFilterableDecompositionTest.java,v 1.1 2007/03/07 15:24:04 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl.test;

import java.util.HashSet;
import java.util.Set;

import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;
import org.wymiwyg.rdf.molecules.functref.impl.ReferenceGroundedDecompositionImpl;
import org.wymiwyg.rdf.molecules.model.modelref.implgraph.ModelReferencingDecompositionImpl;

import com.hp.hpl.jena.gvs.impl.SimpleFilterableDecomposition;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
 * @author reto
 *
 */
public class SimpleFilterableDecompositionTest extends ModelTestBase {

	/**
	 * @param name
	 */
	public SimpleFilterableDecompositionTest(String name) {
		super(name);
	}

	SimpleFilterableDecomposition simpleFilterableDecomposition;
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Model model = modelWithStatements("http://example.org/a dc:title 'Hello';" +
				"_:a http://xmlns.com/foaf/0.1/mbox http://example.org/b;" +
				"_:a rdfs:comment _:b");
		ReferenceGroundedDecomposition baseDec = new ReferenceGroundedDecompositionImpl(
				new ModelReferencingDecompositionImpl(JenaUtil.getGraphFromModel(model, true)));
		simpleFilterableDecomposition = new SimpleFilterableDecomposition(baseDec);
	}

	/**
	 * Test method for {@link com.hp.hpl.jena.gvs.impl.SimpleFilterableDecomposition#getContextualMolecules(java.util.Set)}.
	 */
	public void testGetContextualMoleculesSetOfGroundedNode() {
		Set<GroundedNode> requiredGroundedNodes = new HashSet<GroundedNode>();
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/b"));
		assertEquals(0, simpleFilterableDecomposition.getContextualMolecules(requiredGroundedNodes).size());
		GroundedNode groundedNode = simpleFilterableDecomposition.getFunctionallyGroundedNodes().iterator().next();
		requiredGroundedNodes.add(groundedNode);
		assertEquals(1, simpleFilterableDecomposition.getContextualMolecules(requiredGroundedNodes).size());

	}

	/**
	 * Test method for {@link com.hp.hpl.jena.gvs.impl.SimpleFilterableDecomposition#getFunctionallyGroundedNodes(java.util.Set)}.
	 */
	public void testGetFunctionallyGroundedNodesSetOfGroundedNode() {
		Set<GroundedNode> requiredGroundedNodes = new HashSet<GroundedNode>();
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/a#"));
		assertEquals(0, simpleFilterableDecomposition.getFunctionallyGroundedNodes(requiredGroundedNodes).size());
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/a"));
		assertEquals(0, simpleFilterableDecomposition.getFunctionallyGroundedNodes(requiredGroundedNodes).size());
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/b"));
		assertEquals(1, simpleFilterableDecomposition.getFunctionallyGroundedNodes(requiredGroundedNodes).size());
	}

	/**
	 * Test method for {@link com.hp.hpl.jena.gvs.impl.SimpleFilterableDecomposition#getTerminalMolecules(java.util.Set)}.
	 */
	public void testGetTerminalMoleculesSetOfGroundedNode() {
		Set<GroundedNode> requiredGroundedNodes = new HashSet<GroundedNode>();
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/a#"));
		assertEquals(0, simpleFilterableDecomposition.getTerminalMolecules(requiredGroundedNodes).size());
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/b"));
		assertEquals(0, simpleFilterableDecomposition.getTerminalMolecules(requiredGroundedNodes).size());
		requiredGroundedNodes.add(new NamedNodeImpl("http://example.org/a"));
		assertEquals(1, simpleFilterableDecomposition.getTerminalMolecules(requiredGroundedNodes).size());
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

