/*
 * Created on 1-lug-03
 * 
 * 
 * ====================================================================
 *
 * The WYMIWYG Software License, Version 1.0
 *
 * Copyright (c) 2002-2003 WYMIWYG  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by WYMIWYG."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name "WYMIWYG" or "WYMIWYG.org" must not be used to endorse 
 *    or promote products derived from this software without prior written 
 *    permission. For written permission, please contact wymiwyg@wymiwyg.org.
 *
 * 5. Products derived from this software may not be called  
 *    "WYMIWYG" nor may "WYMIWYG" appear in their names 
 *    without prior written permission of WYMIWYG.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL WYMIWYG OR ITS CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of WYMIWYG.  For more
 * information on WYMIWYG, please see http://www.WYMIWYG.org/.
 *
 * This licensed is based on The Apache Software License, Version 1.1,
 * see http://www.apache.org/.
 */
package com.hp.hpl.jena.gvs.impl.filesystem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.graph.impl.SimpleTransactionHandler;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.ReificationStyle;
/**
 * @author reto
 */
//note: quite a bit copied copied from com.hp.hpl.jena.rdf.model.impl.NTripleWriter
public class NTFileGraph extends GraphMem {

	private PrintWriter fileWriter;

	private static final Log log = LogFactory.getLog(NTFileGraph.class);
	
	private boolean created = false;
	

	public NTFileGraph(File file)
			throws IOException {
		super(ReificationStyle.Standard);
		fileWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), "ascii"));
		if (file.exists()) {
			Model model = ModelFactory.createModelForGraph(this);
			new NonSkoNTripleReader().read(model, file.toURL().toString());
		}	
		created = true;
		log.debug("NTFileGraph created");
	}

	/**
	 * @param statement
	 * @param writer
	 */
	private void writeStatement(Triple triple, PrintWriter writer)
			throws IOException {
		writeNonLiteral(triple.getSubject(), writer);
		writer.write(' ');
		writeNamed(triple.getPredicate(), writer);
		writer.write(' ');
		writeAnyNode(triple.getObject(), writer);
		writer.println(" .");
		/*Graph stmtGraph = new GraphMem();
		stmtGraph.add(triple);
		Model stmtModel = ModelFactory.createModelForGraph(stmtGraph);
		stmtModel.write(writer, "N-TRIPLE");*/
	}

	/**
	 * @param predicate
	 * @param writer
	 */
	private void writeAnyNode(Node node, PrintWriter writer) {
		if (node.isLiteral()) {
			writeLiteral(node, writer);
		} else {
			writeNonLiteral(node, writer);
		}
		
	}

	/**
	 * @param node
	 * @param writer
	 */
	private void writeLiteral(Node node, PrintWriter writer) {
		 String s = node.getLiteral().getLexicalForm();
	        /*
	        if (l.getWellFormed())
	        	writer.print("xml");
	        */
	        writer.print('"');
	        writeString(s, writer);
	        writer.print('"');
	        String lang = node.getLiteralLanguage();
	        if (lang != null && !lang.equals(""))
	            writer.print("@" + lang);
	        String dt = node.getLiteralDatatypeURI();
	        if (dt != null && !dt.equals(""))
	            writer.print("^^<" + dt + ">");
		
	}
	
	private static void writeString(String s, PrintWriter writer) {

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '"') {
                writer.print('\\');
                writer.print(c);
            } else if (c == '\n') {
                writer.print("\\n");
            } else if (c == '\r') {
                writer.print("\\r");
            } else if (c == '\t') {
                writer.print("\\t");
            } else if (c >= 32 && c < 127) {
                writer.print(c);
            } else {
                String hexstr = Integer.toHexString(c).toUpperCase();
                int pad = 4 - hexstr.length();
                writer.print("\\u");
                for (; pad > 0; pad--)
                    writer.print("0");
                writer.print(hexstr);
            }
        }
    }

	/**
	 * @param subject
	 * @param writer
	 */
	private void writeNonLiteral(Node node, PrintWriter writer) {
		if (node instanceof Node_Blank) {
            writer.print(getAnonName(node));
        } else {
        	writeNamed(node, writer);
            
        }
		
	}
	
	/**
	 * @param node
	 * @param writer 
	 */
	private void writeNamed(Node node, PrintWriter writer) {
		writer.print("<");
        writeURIString(node.getURI(), writer);
        writer.print(">");
		
	}

	static private boolean okURIChars[] = new boolean[128];
    static {
        for (int i = 32; i < 127; i++)
            okURIChars[i] = true;
        okURIChars['<'] = false;
        okURIChars['>'] = false;
        okURIChars['\\'] = false;

    }
    private static void writeURIString(String s, PrintWriter writer) {

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < okURIChars.length && okURIChars[c]) {
                writer.print(c);
            } else {
                String hexstr = Integer.toHexString(c).toUpperCase();
                int pad = 4 - hexstr.length();
                writer.print("\\u");
                for (; pad > 0; pad--)
                    writer.print("0");
                writer.print(hexstr);
            }
        }
    }

	/**
	 * @param blank
	 * @return
	 */
	private Object getAnonName(Node blank) {
		String name = "_:A";
        String sid = blank.getBlankNodeId().toString();//getBlankNodeLabel();
        for (int i = 0; i < sid.length(); i++) {
            char c = sid.charAt(i);
            if (c == 'X') {
                name = name + "XX";
            } else if (Character.isLetterOrDigit(c)) {
                name = name + c;
            } else {
                name = name + "X" + Integer.toHexString((int) c) + "X";
            }
        }
        return name;
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#add(com.hp.hpl.jena.graph.Triple)
	 */
	public synchronized void performAdd(Triple triple) {
		super.performAdd(triple);
		if (created) {
			try {
				writeStatement(triple, fileWriter);
				fileWriter.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	/**
	 * @see com.hp.hpl.jena.graph.Graph#delete(com.hp.hpl.jena.graph.Triple)
	 */
	public synchronized void performDelete(Triple triple) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see com.hp.hpl.jena.graph.Graph#getBulkUpdateHandler()
	 */
	public BulkUpdateHandler getBulkUpdateHandler() {
		return new SimpleBulkUpdateHandler(this);
		//return super.getBulkUpdateHandler();
	}
	/**
	 * @see com.hp.hpl.jena.graph.Graph#getTransactionHandler()
	 */
	public TransactionHandler getTransactionHandler() {
		return new SimpleTransactionHandler();
	}
	@Override
	protected void finalize() throws Throwable {
		fileWriter.close();
		super.finalize();
	}
}
