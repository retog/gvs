/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FCAGraphImplTest.java,v 1.2 2007/05/02 10:53:03 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.test;

import java.util.Collections;

import junit.framework.TestCase;

import org.wymiwyg.commons.jena.ModelCreationUtil;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.leanifier.MoleculeBasedLeanifier;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class FCAGraphImplTest extends TestCase {
	/**
	 * Test if a model on fca is isomorphic with model converted using JenaUtil
	 * 
	 */
	public void testComapringWithConverter() {
		Model origModel = ModelCreationUtil.createRandomModel(1000);
		Graph origGraph = JenaUtil.getGraphFromModel(origModel, true);
		Graph leanGraph = MoleculeBasedLeanifier.getLeanVersionOf(origGraph);
		Model leanModel = JenaUtil.getModelFromGraph(leanGraph);
		FCAGraph fcaGraph = new FCAGraphImpl(leanGraph);
		Model modelOnFCA = ModelFactory.createModelForGraph(fcaGraph);
		assertTrue(leanModel.isIsomorphicWith(modelOnFCA));
	}

	/**
	 * test the filter-method using the model in filter-test.n3
	 * 
	 */
	public void testFilter() {
		Model model = ModelFactory.createDefaultModel();
		model.read(getClass().getResource("filter-test.n3").toString(),
				"N3");
		FCAGraph fcaGraph = new FCAGraphImpl(model);
		FCAGraph subGraph = fcaGraph.filter(Collections.singleton((GroundedNode)new NamedNodeImpl("mailto:test@example.org")));
		assertEquals(3, subGraph.size());
		FCAGraph subGraph2 = fcaGraph.filter(Collections.singleton((GroundedNode)new NamedNodeImpl("http://example.org/foo")));
		assertEquals(3, subGraph2.size());
		//ModelFactory.createModelForGraph(subGraph2).write(System.out);
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

