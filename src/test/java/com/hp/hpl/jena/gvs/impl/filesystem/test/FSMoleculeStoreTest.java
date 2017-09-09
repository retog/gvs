/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: FSMoleculeStoreTest.java,v 1.3 2007/02/20 12:03:41 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl.filesystem.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.fgnodes.FunctionallyGroundedNode;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.molecules.MaximumContextualMolecule;
import org.wymiwyg.rdf.molecules.TerminalMolecule;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;
import org.wymiwyg.rdf.molecules.functref.impl.ReferenceGroundedDecompositionImpl;
import org.wymiwyg.rdf.molecules.model.modelref.ModelReferencingDecomposition;
import org.wymiwyg.rdf.molecules.model.modelref.implgraph.ModelReferencingDecompositionImpl;

import com.hp.hpl.jena.gvs.impl.filesystem.FSMoleculeStore;
import com.hp.hpl.jena.gvs.impl.filesystem.MapGraphCache;
import com.hp.hpl.jena.gvs.storage.MoleculeStore;
import com.hp.hpl.jena.gvs.test.StoreTest;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 *
 */
public class FSMoleculeStoreTest extends TestCase {
	public void testStoreSize() {
		File dir;
		try {
			dir = File.createTempFile("teststore", "dir");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
		dir.delete();
		dir.mkdirs();
//new File("teststore");
//		if (dir.exists()) {
//			FSStoreTest.delDirContent(dir);
//		} else {
//			dir.mkdirs();
//		}
		MoleculeStore moleculeStore = new FSMoleculeStore(dir, new MapGraphCache());
		addData(moleculeStore, "test12-m1.rdf");
		long size1 = getRecursiveSize(dir);
		System.out.println(size1);
		addData(moleculeStore, "test12-m1.rdf");
		long size2 = getRecursiveSize(dir);
		assertEquals(size1, size2);
		moleculeStore = new FSMoleculeStore(dir, new MapGraphCache());
		addData(moleculeStore, "test12-m1.rdf");
		long size3 = getRecursiveSize(dir);
		assertEquals(size1, size3);
	}

	/**
	 * @param dir
	 * @return
	 */
	private long getRecursiveSize(File dir) {
		long size = dir.length();
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				size += getRecursiveSize(file);
			}
		}
		return size;
	}

	/**
	 * @param moleculeStore 
	 * @param string
	 */
	private void addData(MoleculeStore moleculeStore, String modelName) {
		Model model = ModelFactory.createDefaultModel();
		model.read(StoreTest.class.getResource(modelName).toString());
		Graph graph = JenaUtil.getGraphFromModel(model , true);
		ModelReferencingDecomposition modelReferencingDecomposition = new ModelReferencingDecompositionImpl(graph);
		ReferenceGroundedDecomposition dec = new ReferenceGroundedDecompositionImpl(modelReferencingDecomposition);
		for (FunctionallyGroundedNode fgNode : dec.getFunctionallyGroundedNodes()) {
			moleculeStore.addFunctionallyGroundedNode(fgNode);
		}
		for (TerminalMolecule terminalMolecule : dec.getTerminalMolecules()) {
			moleculeStore.addTerminalMolecule(terminalMolecule);
		}
		for (MaximumContextualMolecule molecule : dec.getContextualMolecules()) {
			moleculeStore.addContextualMolecule(molecule);
		}
		
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

