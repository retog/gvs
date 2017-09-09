/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GVSToolCommand.java,v 1.5 2007/10/01 19:29:55 rebach Exp $
*/
package com.hp.hpl.jena.gvs.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.wymiwyg.commons.util.arguments.AnnotatedInterfaceArguments;
import org.wymiwyg.commons.util.arguments.ArgumentHandler;
import org.wymiwyg.commons.util.arguments.ArgumentProcessor;
import org.wymiwyg.commons.util.arguments.InvalidArgumentsException;

import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.filesystem.EhcacheGraphCache;
import com.hp.hpl.jena.gvs.impl.filesystem.FSSToreImpl;
import com.hp.hpl.jena.gvs.impl.filesystem.GraphCache;
import com.hp.hpl.jena.gvs.impl.filesystem.MapGraphCache;

/**
 * @author reto
 *
 */
public abstract class GVSToolCommand<T extends GVSToolArguments> {

	protected abstract Class<T> getArgumentsClass();
	
	/** Execute the command
	 * 
	 * @param invocationString invoation of the command as present in help-texts
	 * @param args
	 */
	public void execute(String invocationString, String[] args){
		ArgumentHandler argumentHandler = new ArgumentHandler(args);
		final T arguments;
		try {
			arguments = AnnotatedInterfaceArguments.getInstance(
					getArgumentsClass(), argumentHandler).getValueObject();
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			showHelp(invocationString);
			return;
		}
		final List<String> unusedArguments = new ArrayList<String>();
		try {
			argumentHandler.processArguments(new ArgumentProcessor() {
				public void process(List<String> argumentList) throws InvalidArgumentsException {
					unusedArguments.addAll(argumentList);
					
				}
			});
		} catch (InvalidArgumentsException e1) {
			throw new RuntimeException(e1);
		}
		if (unusedArguments.size() > 0) {
			for (String unused : unusedArguments) {
				System.err.println("Argument not understood "+unused);
				showHelp(invocationString);
				return;
			}
		}
		Store store;
		try {
			GraphCache cache;
			if (arguments.getUseEhcahe()) {
				cache = new EhcacheGraphCache();
			} else {
				cache = new MapGraphCache();
			}
			store = new FSSToreImpl(arguments.getStorageDir(), cache);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		execute(store, arguments);
	}
	


	/**
	 * @param arguments
	 */
	protected abstract void execute(Store store, T arguments);

	protected void showHelp(String invocationString) {
		System.out.println("Usage:");
		System.out.print(invocationString);
		System.out.print(' ');
		System.out.println(AnnotatedInterfaceArguments
				.getArgumentsSyntax(getArgumentsClass()));
		PrintWriter out = new PrintWriter(System.out, true);
		AnnotatedInterfaceArguments.printArgumentDescriptions(
				getArgumentsClass(), out);
		out.flush();
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

