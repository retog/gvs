/*
 * Copyright  2002-2006 WYMIWYG (http://wymiwyg.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: FCAGraph.java,v 1.3 2007/05/02 11:00:23 rebach Exp $
 */
package com.hp.hpl.jena.gvs;

import java.util.Set;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.GroundedNode;
import org.wymiwyg.rdf.molecules.functref.ReferenceGroundedDecomposition;

/**
 * A filterable component-aware graph.
 * <br/>
 * As it extends org.wymiwyg.rdf.graphs.Graph it follows its rules for hashCode
 * and equals
 * 
 * @see com.hp.hpl.jena.gvs.impl.FCAGraphImpl FCAGraphImpl for an implementation.
 * @author reto
 * 
 */
public interface FCAGraph extends Graph, ReferenceGroundedDecomposition,
		 com.hp.hpl.jena.graph.Graph {
	/**
	 * Returns a subgraph of this graph containing only the context of the
	 * specified grounded nodes, respectively only the fg-nodes being equals to
	 * or directly or indirectly containing one of the grounded nodes as well as
	 * all molecules containg one of those fg-nodes or one of the specified
	 * grounded nodes.
	 * 
	 * 
	 * @param onlyForGroundedNodes
	 *            ths grounded nodes of which the context is to selected
	 * @return the FCAGraph for the matching subgraph
	 */
	public FCAGraph filter(Set<GroundedNode> onlyForGroundedNodes);
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
