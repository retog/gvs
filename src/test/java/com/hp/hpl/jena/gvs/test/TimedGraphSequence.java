/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: TimedGraphSequence.java,v 1.5 2007/05/01 09:57:06 rebach Exp $
 */
package com.hp.hpl.jena.gvs.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.wymiwyg.commons.jena.ModelCreationUtil;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;
import org.wymiwyg.rdf.leanifier.MoleculeBasedLeanifier;

import com.hp.hpl.jena.gvs.FCAGraph;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;


/**
 * @author reto
 * 
 */
public class TimedGraphSequence {

	private int size;

	private FCAGraph[] graphs;

	private Date[] dates;

	TimedGraphSequence(FCAGraph[] graphs, Date[] dates) {
		if (graphs.length != dates.length) {
			throw new RuntimeException("need equal amount of graphs and dates");
		}
		this.graphs = graphs;
		this.dates = dates;
		size = dates.length;
	}


	public int getSize() {
		return size;
	}

	public FCAGraph getGraph(int number) {
		return graphs[number];
	}

	public Date getDate(int number) {
		return dates[number];
	}
	
	public void write(OutputStream out) throws IOException {
		for (int i = 0; i < graphs.length; i++) {
			Graph graph = graphs[i];
			Date date = dates[i];
			try {
				out.write('\n');
				out.write(("graph nr:"+i+" asserted at").getBytes());
				out.write(date.toString().getBytes());
				out.write('\n');
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			JenaUtil.getModelFromGraph(graph).write(out);
			out.flush();
		}
	}

	public static TimedGraphSequence getRandomSequence(int size, int modelSize) {
		FCAGraph[] graphs = new FCAGraph[size];
		Date[] dates = new Date[graphs.length];
		Date date = new Date(System.currentTimeMillis() - 1000000);
		for (int i = 0; i < graphs.length; i++) {
			// TODO at this level a graph should probably ignore ontology
			Graph currentGraph = JenaUtil.getGraphFromModel(ModelCreationUtil
					.createRandomModel(modelSize), true);
			currentGraph = MoleculeBasedLeanifier
					.getLeanVersionOf(currentGraph);
			graphs[i] = new FCAGraphImpl(currentGraph);
			dates[i] = date;
			date = new Date((long) (date.getTime() + (Math.random() * 100000)));

		}
		return new TimedGraphSequence(graphs, dates);
	}
	
	public static TimedGraphSequence getSequenceWithRandomDates(FCAGraph[] graphs) {
		Date[] dates = new Date[graphs.length];
		Date date = new Date(System.currentTimeMillis() - 1000000);
		for (int i = 0; i < graphs.length; i++) {
			dates[i] = date;
			date = new Date((long) (date.getTime() + (Math.random() * 100000)));

		}
		return new TimedGraphSequence(graphs, dates);
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

