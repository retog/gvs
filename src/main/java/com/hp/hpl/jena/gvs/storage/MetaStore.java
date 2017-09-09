/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: MetaStore.java,v 1.5 2007/06/27 12:50:12 rebach Exp $
 */
package com.hp.hpl.jena.gvs.storage;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.wymiwyg.rdf.graphs.NamedNode;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author reto
 * 
 */
public interface MetaStore {

	/**
	 * @param source
	 *            the source asserting the component
	 * @param moleculeRef
	 *            the refrence to the component
	 * @param moment
	 *            the moment of assertion
	 * @param type
	 *            the type of the component using the metamodel ontology
	 * @return false is the component was already asserted, true otherwise
	 */
	public boolean assertComponent(Source source, NamedNode moleculeRef,
			Date moment, Resource type);

	public void revokeComponent(Source source, NamedNode moleculeRef,
			Date moment);

	public Set<NamedNode> getAsserted(Set<Source> sources, Date moment,
			Resource type);

	/**
	 * The moment of assertions is the first moment of a period in which a
	 * component is asserted.
	 * 
	 * @param source
	 * @param moment
	 * @param type
	 * @return the assertions of the specified type made in that exact moment
	 */
	public Set<NamedNode> getAssertions(Source source, Date moment,
			Resource type);

	/**
	 * The moment of a revocation is the first moment at which a component is no
	 * longer asserted after it was asserted.
	 * 
	 * @param source
	 * @param moment
	 * @param type
	 * @return the revocations of the specified type made in that exact moment
	 */
	public Set<NamedNode> getRevocations(Source source, Date moment,
			Resource type);

	/**
	 * @param sources
	 * @param moment
	 * @return
	 */
	public Iterator<Date> getFollowingChanges(Set<Source> sources, Date moment);

	/**
	 * @param sources
	 * @param moment
	 * @return
	 */
	public Iterator<Date> getPreviousChanges(Set<Source> sources, Date moment);

	/**
	 * @return
	 */
	public Set<Source> getSources();

	/*
	 * it should be possible to: - select molecules with a certain resource at
	 * any position but predicate - (select molecules with a certain predicate
	 * and determinabely the object or the subject out of a set) - select
	 * molecules with a certain triple-pattern, where at least the predicate is
	 * set (?)
	 * 
	 */
	// public Set<NamedNode> getAssertedMoleculeWith(Set<Source> sources,
	// ResourceContentCriterion criterion);
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

