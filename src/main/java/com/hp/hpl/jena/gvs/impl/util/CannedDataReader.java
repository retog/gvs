/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: CannedDataReader.java,v 1.5 2007/05/02 10:52:50 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.util.dirbrowser.FilePathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;

import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.impl.memory.MemoryStoreImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 * 
 */
public class CannedDataReader {

	private static final Log log = LogFactory.getLog(CannedDataReader.class);

	/**
	 * @author reto
	 * 
	 */
	public static class Assertion implements Comparable<Assertion> {

		private Source source;

		private FCAGraph graph;

		private Date date;

		/**
		 * @param impl
		 * @param graph
		 * @param date
		 */
		public Assertion(Source source, FCAGraph graph, Date date) {
			this.source = source;
			this.graph = graph;
			this.date = date;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Assertion o) {
			if (o.equals(this)) {
				return 0;
			}
			if (this.date.equals(o.date)) {
				return source.toString().compareTo(o.source.toString());
			}
			return date.compareTo(o.date);
		}

		/**
		 * @param store
		 * @param clock
		 */
		public void storeIn(Store store) {
			log.info(source + " asserted something at " + date);
			Clock oldClock = store.getClock();
			store.setClock(new Clock() {

				public Date getTime() {
					return date;
				}
				
			});
			store.updateGraph(source, graph);
			store.setClock(oldClock);

		}

	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {;
//		MemoryStoreImpl store = new MemoryStoreImpl();
//		readCannedData(store, new File("testdata"), "http://localhost:8181/");
//		// store.getGraphOverTime(sources)
//	}

	public static SortedSet<Assertion> getAssertions(PathNode directory,
			String baseURLString) {
		SortedSet<Assertion> assertions = new TreeSet<Assertion>();
		String[] childNames = directory.list();
		for (String childName : childNames) {
			if (!childName.endsWith(".rdf")) {
				continue;
			}
			String baseName = childName.substring(0, childName.length() - 4);
			StringTokenizer tokens = new StringTokenizer(baseName, "-");
			String sourceString = baseURLString + tokens.nextToken();
			Date date;
			try {
				String dateString = tokens.nextToken();
				date = new SimpleDateFormat("yyyyMMddHHmmssSSS")
						.parse(dateString);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			Model model = ModelFactory.createDefaultModel();
			try {
				model.read(directory.getSubPath(childName).getInputStream(), baseURLString);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			FCAGraph graph = new FCAGraphImpl(model);
			assertions.add(new Assertion(new SourceImpl(sourceString), graph,
					date));
		}
		return assertions;
	}

	public static void addToStrore(Store store,
			SortedSet<Assertion> assertions) {
		for (Assertion assertion : assertions) {
			assertion.storeIn(store);
		}
	}

//	public static void readCannedData(Store store, 
//			File directory, String baseURLString) {
//		readCannedData(store, new FilePathNode(directory), baseURLString);
//
//	}
	
	public static void readCannedData(Store store, 
			URL directory, String baseURLString) {
		try {
			readCannedData(store, PathNodeFactory.getPathNode(directory), baseURLString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	public static void readCannedData(Store store, 
			PathNode directory, String baseURLString) {
		SortedSet<Assertion> assertions = getAssertions(directory,
				baseURLString);
		addToStrore(store, assertions);

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

