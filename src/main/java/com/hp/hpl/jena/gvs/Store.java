/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: Store.java,v 1.7 2007/05/23 12:57:26 rebach Exp $
 */
package com.hp.hpl.jena.gvs;

import java.util.Set;

/**
 * A <code>Store</code> generates <code>GraphOverTimes</code> by keeping
 * track of assertions and revocations of graphs from different sources.
 * 
 * @author reto
 * 
 */
public interface Store {

	/**
	 * Get a graph-over time to access the union of the graphs for the specified
	 * sources at any point in time.
	 * 
	 * @param sources
	 *            the sources to be considered
	 * @return a GraphOverTime for the union of the graphs for the specified
	 *         sources
	 */
	public GraphOverTime getGraphOverTime(Set<Source> sources);

	/**
	 * Asserts a graph as a source, i.e. the graph to the graph of that source
	 * 
	 * @param source
	 *            the source the graph should be asserted as
	 * @param graph
	 *            the asserted graph
	 */
	public void assertGraph(Source source, FCAGraph graph);

	/**
	 * Revokes a graph previously asserted by a source.
	 * 
	 * Statements with functional or inverse-functional are removed only if the
	 * node they ground is not part of any other statement. More precisely:
	 * revoking a graph doesn't revoke the fgnodes contained therein except if
	 * they are not part of any statement, in which case the nt-molecules are
	 * revoked. If all nt-molecules of an fgnodes are revoked or if all its
	 * remaining nt-molecules reference back to the fg-node, the molecule
	 * containing that fgnode are merged preserving the identity of the node
	 * creating a new contextual molecule. If the fgnode is the grounding object
	 * of an nt-molecule of another fg-node that molecule is integrate in the
	 * contextual molecule, recursively if this is the only molecule of the
	 * fg-node.
	 * 
	 * @param source
	 *            the source for which the graph is to be revoked
	 * @param graph
	 */
	public void revokeGraph(Source source, FCAGraph graph);

	/**
	 * Asserts a graph revoking everything previously asserted that is not in
	 * the new graph.
	 * 
	 * @param source
	 * @param graph
	 */
	public void updateGraph(Source source, FCAGraph graph);

	/**
	 * This revokes a specified graph simultaneously asserting another graph.
	 * 
	 * @param source
	 *            the source for which the graph is to be changed
	 * @param revokedGraph
	 *            the graph to be revoked
	 * @param assertedGraph
	 *            the graph to be asserted
	 */
	public void changeGraph(Source source, FCAGraph revokedGraph,
			FCAGraph assertedGraph);

	/**
	 * performs the specified transaction as the specified source.
	 * 
	 * @param source
	 *            the source that perfoms the transaction
	 * @param transaction
	 *            the transaction to be performed
	 */
	public void perform(Source source, StoreTransaction transaction);

	/**
	 * Sets the Clock which is used by GVS to get the current time.
	 * 
	 * @param clock
	 */
	public void setClock(Clock clock);

	/**
	 * Returns the Clock which is used by GVS to get the current time.
	 * 
	 * @return
	 */
	public Clock getClock();

	/**
	 * get all sources known to the store
	 * 
	 * @return all sources
	 */
	public Set<Source> getSources();

	/**
	 * Closes the store. This allows the implementation to release resources and
	 * locks. After calling this method no other method of the Store nor of its
	 * GraphOverTimeS should be called.
	 */
	public void close();

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

