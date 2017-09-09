/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: MapGraphCache.java,v 1.3 2007/06/06 09:04:19 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.filesystem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.wymiwyg.rdf.graphs.Graph;

/**
 * An implementation of GraphCache based on a HashMap, this is useful when there
 * is enough memory that nothing has ever to be removed from the cache.
 * 
 * @author reto
 * 
 */
public class MapGraphCache implements GraphCache {

	Map<File, Graph> map = new HashMap<File, Graph>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.impl.filesystem.GraphCache#get(java.io.File)
	 */
	public Graph get(File modelFile) {
		return map.get(modelFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.impl.filesystem.GraphCache#put(java.io.File,
	 *      org.wymiwyg.rdf.graphs.Graph)
	 */
	public void put(File modelFile, Graph graph) {
		map.put(modelFile, graph);
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

