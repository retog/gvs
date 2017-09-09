/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: GraphOverTime.java,v 1.8 2007/05/15 14:21:41 rebach Exp $
 */
package com.hp.hpl.jena.gvs;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.wymiwyg.rdf.graphs.GroundedNode;

/**
 * This class represents a graph changing over time.
 * 
 * @author reto
 * 
 */
public interface GraphOverTime {

	/**
	 * Two <code>GraphOverTime</code>s are equals if and only if they are the
	 * same instance (i.e. g1 == g2).
	 * 
	 * Implementing classes can simply inherit the implementation of
	 * <code>Object</code
	 *  
	 * @param object
	 * @return
	 */
	public boolean equals(Object object);

	/**
	 * Returns a GraphOverTime limited to the description of the context of the
	 * specified resources. The filtering criteria are the same as for the
	 * filter method in FCAGraph. The previous- and followingChanges method of
	 * the returned GraphOverTimer consider only changes relevant to the subset.
	 * 
	 * @see com.hp.hpl.jena.gvs.FCAGraph.filter(Set<GroundedNode>)
	 * 
	 * @param onlyForGroundedNodes
	 *            ths grounded nodes of which the context is to selected
	 * @return the GraphOverTime for the matching subgraph
	 */
	public GraphOverTime filter(Set<GroundedNode> onlyForGroundedNodes);

	/**
	 * Get the moments of change after the date d. The returned iterator is a
	 * chronological list of dates, the first element t0 is the earliest Date
	 * after d for which getGraph(t0) is not equals getGraph(d).
	 * 
	 * @param d
	 * @return
	 */
	public Iterator<Date> followingChanges(Date d);

	/**
	 * 
	 * @param moment
	 *            the date/time for which the graph is requested
	 * @return the graph at the specified date/time
	 */
	public FCAGraph getGraph(Date moment);

	/**
	 * Get the moments of change previous to the date d. The returned iterator
	 * is an inverse chronological list of dates, the first element t0 is the
	 * latest Date before or equals d for which getGraph(t0) is not equals
	 * getGraph(t0-1), i.e. t0 is the earliest moment for which the graph is
	 * same as for d.
	 * 
	 * @param d
	 * @return an inverse chronological iterator of dates
	 */
	public Iterator<Date> previousChanges(Date d);

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

