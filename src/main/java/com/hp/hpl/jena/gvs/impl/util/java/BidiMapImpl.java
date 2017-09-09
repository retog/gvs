/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: BidiMapImpl.java,v 1.1 2006/09/22 15:14:25 rebach Exp $
*/
package com.hp.hpl.jena.gvs.impl.util.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author reto
 *
 */
public class BidiMapImpl<K,V> implements BidiMap<K, V> {

	private Map<K,V> forward = new HashMap<K,V>();
	private Map<V,K> backward = new HashMap<V,K>();
	
	/**
	 * 
	 */
	public BidiMapImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.rdf.molecules.store.BidiMap#getKey(U)
	 */
	public K getKey(V value) {
		return backward.get(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		return forward.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return forward.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return forward.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return backward.containsKey(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public V get(Object key) {
		return forward.get(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(K, V)
	 */
	public V put(K key, V value) {
		//remove possible existing with same value
		//throwing exception: GVS specific
		if (backward.containsKey(value)) {
			throw new RuntimeException("value already present");
			/*K oldKey = backward.get(value);
			forward.remove(oldKey);*/
		}
		if (forward.containsKey(key)) {
			throw new RuntimeException("key already present");
			/*V oldValue = forward.get(key);
			backward.remove(oldValue);*/
		}
		backward.put(value, key);
		return forward.put(key, value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public V remove(Object key) {
		V value = forward.remove(key);
		backward.remove(value);
		return value;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}

	}

	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		forward.clear();
		backward.clear();

	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set<K> keySet() {
		return forward.keySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	public Collection<V> values() {
		return backward.keySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	public Set<Entry<K,V>> entrySet() {
		return forward.entrySet();
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
