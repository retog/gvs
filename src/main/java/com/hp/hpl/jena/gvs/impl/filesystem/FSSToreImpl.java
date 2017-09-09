/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FSSToreImpl.java,v 1.9 2007/06/20 19:16:51 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.filesystem;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.StoreException;
import com.hp.hpl.jena.gvs.impl.BaseStoreImpl;
import com.hp.hpl.jena.gvs.impl.CachingMetaStore;
import com.hp.hpl.jena.gvs.impl.ModelMetaStore;
import com.hp.hpl.jena.gvs.storage.MetaStore;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class FSSToreImpl extends BaseStoreImpl {

	private FSMoleculeStore fsMoleculeStore;

	private File lockFile;

	/**
	 * Creates an FSStoreImpl using the current working directory (".").
	 * 
	 * @throws IOException
	 */
	public FSSToreImpl() throws IOException {
		this(new File("."));
	}

	/**
	 * Creates an FSStoreImpl using the specified directory
	 * 
	 * @param storeDir
	 *            the directory into which the contents are stored
	 * @throws IOException
	 */
	public FSSToreImpl(File storeDir) throws IOException {
		this(storeDir, new EhcacheGraphCache());
	}

	/**
	 * Creates an FSStoreImpl using the specified directory and GraphCache
	 * 
	 * @param storeDir
	 *            the directory into which the contents are stored
	 * @param cache
	 *            the cache to be used
	 * @throws IOException
	 */
	public FSSToreImpl(File storeDir, GraphCache cache) throws IOException {
		storeDir.mkdirs();
		lockFile = new File(storeDir, ".running-gvs-id");
		if (!lockFile.createNewFile()) {
			throw new StoreException(
					"Another instance seems to be still running, if not delete "
							+ lockFile);
		}
		lockFile.deleteOnExit();
		File moleculeStoreDir = new File(storeDir, "moleculestore");
		moleculeStoreDir.mkdirs();
		fsMoleculeStore = new FSMoleculeStore(moleculeStoreDir, cache);
		setMoleculeStore(fsMoleculeStore);
		File metaStoreFile = new File(storeDir, "meta-store.nt");
		Model metaModel = ModelFactory.createModelForGraph(new NTFileGraph(
				metaStoreFile));
		MetaStore metaStore;// = new ModelMetaStore(metaModel);
		metaStore = new CachingMetaStore(metaModel);
		setMetaStore(metaStore);
	}

	/**
	 * Creates an FSStoreImpl using the specified directory and Clock
	 * 
	 * @param storeDir
	 *            the directory into which the contents are stored
	 * @param clock
	 *            the clock to be used
	 * @throws IOException
	 */
	public FSSToreImpl(File storeDir, Clock clock) throws IOException {
		this(storeDir);
		setClock(clock);
	}

	/**
	 * Detects and fixes incosistencies in the model such as duplicated
	 * components
	 * 
	 * @param fix
	 *            whether it should change molecule and metastore to fix
	 *            inconsistencies
	 */
	public void checkAndFixConsistency(final boolean fix) {
		fsMoleculeStore.checkAndFixConsistency(fix);
		// TODO fix in metamodel
	}

	@Override
	public void close() {
		super.close();
		lockFile.delete();
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

