/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: EhcacheGraphCache.java,v 1.1 2007/02/20 12:03:40 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl.filesystem;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.wymiwyg.rdf.graphs.Graph;

/**
 * @author reto
 *
 */
public class EhcacheGraphCache implements GraphCache {
	Cache cache;
	{
		CacheManager singletonManager = CacheManager.create();
		try {
			singletonManager.removeCache("graphCache");
		} catch (Exception ex) {}
		singletonManager.addCache("graphCache");
		cache = singletonManager.getCache("graphCache");
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.impl.filesystem.GraphCache#get(java.io.File)
	 */
	public Graph get(File modelFile) {
		Element cacheElement = cache.get(modelFile);
		if (cacheElement != null) {
			return (Graph) cacheElement.getObjectValue();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.impl.filesystem.GraphCache#put(java.io.File, org.wymiwyg.rdf.graphs.Graph)
	 */
	public void put(File modelFile, Graph graph) {
		cache.put(new Element(modelFile, graph));
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

