/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: MultiReader.java,v 1.2 2007/05/15 09:11:26 rebach Exp $
*/
package com.hp.hpl.jena.gvs.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 *
 */
public class MultiReader {

	/**
	 * @author reto
	 *
	 */
	private static final class PipedProcessor implements Processor {
		PipedInputStream in = new PipedInputStream();

		PipedOutputStream out;

		PipedProcessor() throws IOException {
			out = new PipedOutputStream(in);
		}

		public OutputStream getOutputStream() {
			return out;
		}
	}

	/**
	 * @author reto
	 *
	 */
	public interface Processor {
		OutputStream getOutputStream();

	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//Reader fileReader = new InputStreamReader(MultiReader.class.getResourceAsStream("failing-case-20061108.txt"));
		Reader fileReader =  new FileReader("failing-case.txt");
		BufferedReader in = new BufferedReader(fileReader);
		int modelNumber = 0;
		PrintWriter currentOut = null;
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.equals("<rdf:RDF")) {
				currentOut = new PrintWriter(new OutputStreamWriter(getProcessor(modelNumber++).getOutputStream()));
			}
			if (line.equals("</rdf:RDF>")) {
				currentOut.println(line);
				currentOut.close();
				currentOut = null;
			}
			if (currentOut != null) {
				currentOut.println(line);
			}
		}

	}

	/**
	 * @param i
	 * @return
	 * @throws IOException 
	 */
	private static Processor getProcessor(final int modelOrdinal) throws IOException {
		final PipedProcessor result =  new PipedProcessor();
		new Thread() {
			public void run() {		
				try {
					Model model = ModelFactory.createDefaultModel();
					model.read(result.in,"");
					OutputStream out = new FileOutputStream("test18-m"+modelOrdinal+".rdf");
					model.write(out, "");
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}.start();
		return result;
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

