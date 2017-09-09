/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: CachingMetaStore.java,v 1.3 2007/06/27 12:50:16 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.wymiwyg.rdf.graphs.NamedNode;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.impl.memory.NativeMemoryMetaStore;
import com.hp.hpl.jena.gvs.storage.MetaStore;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A <code>MetaStore</code> caching the information in and answering reads
 * from a <code>NativeMemoryMetaStore</code> while writing everyting to a
 * grounding (usually persistent) store.
 * 
 * @author reto
 * 
 */
public class CachingMetaStore implements MetaStore {

	private MetaStore baseSore;
	private NativeMemoryMetaStore cacheStore = new NativeMemoryMetaStore();

	/**
	 * creates a CachingMetaStore based on a metaModel.
	 * @param metaModel
	 */
	public CachingMetaStore(Model metaModel) {
		this.baseSore = new ModelMetaStore(metaModel);
		cacheStore.loadFromModel(metaModel);
	}
	
	/**
	 * creates a CachingMetaStore based on baseStore
	 * @param baseSore
	 */
	public CachingMetaStore(MetaStore baseSore) {
		this.baseSore = baseSore;
		copyFromBaseToCache();
	}

	/**
	 * copies the content of base to the cache
	 */
	private void copyFromBaseToCache() {
		for (Source source : baseSore.getSources()) {
			Set<Source> singleSourceSet = Collections.singleton(source);
			Iterator<Date> changeDates = baseSore.getFollowingChanges(singleSourceSet,
					new Date(0));
			while (changeDates.hasNext()) {
				Date date = changeDates.next();
				copyComponents(source, date, METAMODEL.FunctionallyGroundedNode);
				copyComponents(source, date, METAMODEL.TerminalMolecule);
				copyComponents(source, date, METAMODEL.ContextualMolecule);
			}
		}

	}

	/**
	 * @param source
	 * @param date
	 * @param functionallyGroundedNode
	 */
	private void copyComponents(Source source, Date date,
			Resource type) {
		Set<NamedNode> fgNodeRefs = baseSore.getRevocations(source, date, type);
		for (NamedNode namedNode : fgNodeRefs) {
			cacheStore.revokeComponent(source, namedNode, date);
		}
		fgNodeRefs = baseSore.getAssertions(source, date, type);
		for (NamedNode namedNode : fgNodeRefs) {
			cacheStore.assertComponent(source, namedNode, date, type);
		}
		
	}

	public boolean assertComponent(Source source, NamedNode moleculeRef,
			Date moment, Resource type) {
		if (cacheStore.assertComponent(source, moleculeRef, moment, type)) {
			baseSore.assertComponent(source, moleculeRef, moment, type);
			return true;
		}
		return false;
	}

	public Set<NamedNode> getAsserted(Set<Source> sources, Date moment,
			Resource type) {
		return cacheStore.getAsserted(sources, moment, type);
	}

	public Iterator<Date> getFollowingChanges(Set<Source> sources, Date moment) {
		return cacheStore.getFollowingChanges(sources, moment);
	}

	public Iterator<Date> getPreviousChanges(Set<Source> sources, Date moment) {
		return cacheStore.getPreviousChanges(sources, moment);
	}

	public Set<Source> getSources() {
		return cacheStore.getSources();
	}

	public void revokeComponent(Source source, NamedNode moleculeRef,
			Date moment) {
		cacheStore.revokeComponent(source, moleculeRef, moment);
		baseSore.revokeComponent(source, moleculeRef, moment);

	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getAssertions(com.hp.hpl.jena.gvs.Source, java.util.Date, com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Set<NamedNode> getAssertions(Source source, Date moment,
			Resource type) {
		return cacheStore.getAssertions(source, moment, type);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getRevocations(com.hp.hpl.jena.gvs.Source, java.util.Date, com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Set<NamedNode> getRevocations(Source source, Date moment,
			Resource type) {
		return cacheStore.getRevocations(source, moment, type);
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

