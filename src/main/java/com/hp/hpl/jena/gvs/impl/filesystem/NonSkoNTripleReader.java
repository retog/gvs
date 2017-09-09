/*
 *  (c) Copyright 2001, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id: NonSkoNTripleReader.java,v 1.2 2007/05/02 10:52:23 rebach Exp $
 */

package com.hp.hpl.jena.gvs.impl.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.SyntaxError;
import com.hp.hpl.jena.shared.UnknownPropertyException;
import com.hp.hpl.jena.util.FileUtils;

/** N-Triple Reader which preserves the anon-ids
*
* @author  Brian McBride, Jeremy Carroll, Dave Banks, Reto Bachmann-Gmür
* @version  Release=$Name:  $ Date=$Date: 2007/05/02 10:52:23 $
*/

/*NOTE if com.hp.hpl.jena.rdf.model.impl.NTripleReader would have at least a 
* protected constructor this could just override the lookupResource method
* */
public class NonSkoNTripleReader extends Object implements RDFReader {
    static final Log log = LogFactory.getLog(NonSkoNTripleReader.class);

    private Model model = null;
    //private Hashtable anons = new Hashtable();

    private IStream in = null;
    private boolean inErr = false;
    private int errCount = 0;
    private static final int sbLength = 200;

    private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

    /**
     * Already with ": " at end for error messages.
     */
    private String base;

    NonSkoNTripleReader() {
    }
    public void read(Model model, InputStream in, String base)
         {
        // N-Triples must be in ASCII, we permit UTF-8.
        read(model, FileUtils.asUTF8(in), base);
    }
    public void read(Model model, Reader reader, String base)
         {

        if (!(reader instanceof BufferedReader)) {
            reader = new BufferedReader(reader);
        }

        this.model = model;
        this.base = base == null ? "" : (base + ": ");
        in = new IStream(reader);
        readRDF();
        if (errCount != 0) {
            throw new SyntaxError( "unknown" );
        }
    }

    public void read(Model model, String url)  {
        try {
            read(
                model,
                new InputStreamReader(((new URL(url))).openStream()),
                url);
        } catch (Exception e) {
            throw new JenaException(e);
        } finally {
            if (errCount != 0) {
                throw new SyntaxError( "unknown" );
            }
        }
    }

    public Object setProperty(String propName, Object propValue)
         {
        errorHandler.error(new UnknownPropertyException( propName ));
        return null;
    }

    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = this.errorHandler;
        this.errorHandler = errHandler;
        return old;
    }

    protected void readRDF()  {
        try {
            model.notifyEvent( GraphEvents.startRead );
            unwrappedReadRDF();
        } finally {
            model.notifyEvent( GraphEvents.finishRead );
        }
    }
    
    protected final void unwrappedReadRDF() {
        Resource subject;
        Property predicate = null;
        RDFNode object;

        while (!in.eof()) {
            while (!in.eof()) {
                inErr = false;

                skipWhiteSpace();
                if (in.eof()) {
                    return;
                }

                subject = readResource();
                if (inErr)
                    break;

                skipWhiteSpace();
                try {
                    predicate = model.createProperty(readResource().getURI());
                } catch (Exception e1) {
                    errorHandler.fatalError(e1);
                }
                if (inErr)
                    break;

                skipWhiteSpace();
                object = readNode();
                if (inErr)
                    break;

                skipWhiteSpace();
                if (badEOF())
                    break;

                if (!expect("."))
                    break;

                try {
                    model.add(subject, predicate, object);
                } catch (Exception e2) {
                    errorHandler.fatalError(e2);
                }
            }
            if (inErr) {
                errCount++;
                while (!in.eof() && in.readChar() != '\n') {
                }
            }
        }
    }

    public Resource readResource()  {
        char inChar = in.readChar();
        if (badEOF())
            return null;

        if (inChar == '_') { // anon resource
            if (!expect(":"))
                return null;
            String name = readName();
            if (name == null) {
                syntaxError("expected bNode label");
                return null;
            }
            return lookupResource(name);
        } else if (inChar == '<') { // uri
            String uri = readURI();
            if (uri == null) {
                inErr = true;
                return null;
            }
            inChar = in.readChar();
            if (inChar != '>') {
                syntaxError("expected '>'");
                return null;
            }
            return model.createResource(uri);
        } else {
            syntaxError("unexpected input");
            return null;
        }
    }

    public RDFNode readNode()  {
        skipWhiteSpace();
        switch (in.nextChar()) {
            case '"' :
                return readLiteral(false);
            case 'x' :
                return readLiteral(true);
            case '<' :
            case '_' :
                return readResource();
            default :
                syntaxError("unexpected input");
                return null;
        }
    }

    protected Literal readLiteral(boolean wellFormed)  {

        StringBuffer lit = new StringBuffer(sbLength);

        if (wellFormed) {
            deprecated("Use ^^rdf:XMLLiteral not xml\"literals\", .");

            if (!expect("xml"))
                return null;
        }

        if (!expect("\""))
            return null;

        while (true) {
            char inChar = in.readChar();
            if (badEOF())
                return null;
            if (inChar == '\\') {
                char c = in.readChar();
                if (in.eof()) {
                    inErr = true;
                    return null;
                }
                if (c == 'n') {
                    inChar = '\n';
                } else if (c == 'r') {
                    inChar = '\r';
                } else if (c == 't') {
                    inChar = '\t';
                } else if (c == '\\' || c == '"') {
                    inChar = c;
                } else if (c == 'u') {
                    inChar = readUnicode4Escape();
                    if (inErr)
                        return null;
                } else {
                    syntaxError("illegal escape sequence '" + c + "'");
                    return null;
                }
            } else if (inChar == '"') {
                String lang;
                if ('@' == in.nextChar()) {
                    expect("@");
                   lang = readLang();
                } else if ('-' == in.nextChar()) {
                    expect("-");
                    deprecated("Language tags should be introduced with @ not -.");
                    lang = readLang();
                } else {
                    lang = "";
                }
                if (wellFormed) {
                    return model.createLiteral(
                        lit.toString(),
//                        "",
                        wellFormed);
                } else if ('^' == in.nextChar()) {
                    String datatypeURI = null;
                    if (!expect("^^<")) {
                        syntaxError("ill-formed datatype");
                        return null;
                    }
                    datatypeURI = readURI();
                    if (datatypeURI == null || !expect(">"))
                        return null;
					if ( lang.length() > 0 )
					   deprecated("Language tags are not permitted on typed literals.");
                    
                    return model.createTypedLiteral(
                        lit.toString(),
                        datatypeURI);
                } else {
                    return model.createLiteral(lit.toString(), lang);
                }
            }
            lit = lit.append(inChar);
        }
    }

    private char readUnicode4Escape() {
        char buf[] =
            new char[] {
                in.readChar(),
                in.readChar(),
                in.readChar(),
                in.readChar()};
        if (badEOF()) {
            return 0;
        }
        try {
            return (char) Integer.parseInt(new String(buf), 16);
        } catch (NumberFormatException e) {
            syntaxError("bad unicode escape sequence");
            return 0;
        }
    }
    private void deprecated(String s) {
        errorHandler.warning(
            new SyntaxError(
                syntaxErrorMessage(
                    "Deprecation warning",
                    s,
                    in.getLinepos(),
                    in.getCharpos())));
    }

    private void syntaxError(String s) {
        errorHandler.error(
            new SyntaxError(
                syntaxErrorMessage(
                    "Syntax error",
                    s,
                    in.getLinepos(),
                    in.getCharpos())));
        inErr = true;
    }
    private String readLang() {
        StringBuffer lang = new StringBuffer(15);


        while (true) {
            char inChar = in.nextChar();
            if (Character.isWhitespace(inChar) || inChar == '.' || inChar == '^')
                return lang.toString();
            lang = lang.append(in.readChar());
        }
    }
    private boolean badEOF() {
        if (in.eof()) {
            syntaxError("premature end of file");
        }
        return inErr;
    }
    protected String readURI() {
        StringBuffer uri = new StringBuffer(sbLength);

        while (in.nextChar() != '>') {
            char inChar = in.readChar();

            if (inChar == '\\') {
                expect("u");
                inChar = readUnicode4Escape();
            }
            if (badEOF()) {
                return null;
            }
            uri = uri.append(inChar);
        }
        return uri.toString();
    }

    protected String readName() {
        StringBuffer name = new StringBuffer(sbLength);

        while (!Character.isWhitespace(in.nextChar())) {
            name = name.append(in.readChar());
            if (badEOF())
                return null;
        }
        return name.toString();
    }
    private boolean expect(String str) {
        for (int i = 0; i < str.length(); i++) {
            char want = str.charAt(i);

            if (badEOF())
                return false;

            char inChar = in.readChar();

            if (inChar != want) {
                //System.err.println("N-triple reader error");
                syntaxError("expected \"" + str + "\"");
                return false;
            }
        }
        return true;
    }
    protected void skipWhiteSpace() {
        while (Character.isWhitespace(in.nextChar()) || in.nextChar() == '#') {
            char inChar = in.readChar();
            if (in.eof()) {
                return;
            }
            if (inChar == '#') {
                while (inChar != '\n') {
                    inChar = in.readChar();
                    if (in.eof()) {
                        return;
                    }
                }
            }
        }
    }

    protected Resource lookupResource(String name)  {
        Resource r;
        r = model.createResource(getAnonId(name));
        return r;
    }

    /**
	 * @param name
	 * @return
	 */
	private AnonId getAnonId(String name) {
		
		StringWriter result = new StringWriter();
		StringReader in = new StringReader(name);
		try {
			//Skip "A";
			in.read(new char[1]);
			boolean inX = false;
			StringWriter inXValueWriter = new StringWriter();
			for (int ch = in.read(); ch != -1; ch = in.read()) {
				if (inX) {
					if (ch == 'X') {
						inX = false;
						String inXValue = inXValueWriter.toString();
						inXValueWriter = new StringWriter();
						if (inXValue.equals("")) {
							result.write('X');
						} else {
							result.write(Integer.parseInt(inXValue, 16));
						}
					} else {
						inXValueWriter.write(ch);
					}
				} else {
					if (ch == 'X') {
						inX = true;
					} else {
						result.write(ch);
					}
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
        return new AnonId(result.toString());
	}
	protected String syntaxErrorMessage(
        String sort,
        String msg,
        int linepos,
        int charpos) {
        return base
            + sort
            + " at line "
            + linepos
            + " position "
            + charpos
            + ": "
            + msg;
    }
    
}

class IStream {

    // simple input stream handler

    Reader in;
    char[] thisChar = new char[1];
    boolean eof;
    int charpos = 1;
    int linepos = 1;

    protected IStream(Reader in) {
        try {
            this.in = in;
            eof = (in.read(thisChar, 0, 1) == -1);
        } catch (IOException e) {
            throw new JenaException(e);
        }
    }

    protected char readChar() {
        try {
            if (eof)
                return '\000';
            char rv = thisChar[0];
            eof = (in.read(thisChar, 0, 1) == -1);
            if (rv == '\n') {
                linepos++;
                charpos = 0;
            } else {
                charpos++;
            }
            return rv;
        } catch (java.io.IOException e) {
            throw new JenaException(e);
        }
    }

    protected char nextChar() {
        return eof ? '\000' : thisChar[0];
    }

    protected boolean eof() {
        return eof;
    }

    protected int getLinepos() {
        return linepos;
    }

    protected int getCharpos() {
        return charpos;
    }
    
}