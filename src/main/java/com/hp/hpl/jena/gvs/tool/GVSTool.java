/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GVSTool.java,v 1.5 2007/05/12 07:42:30 rebach Exp $
*/
package com.hp.hpl.jena.gvs.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author reto
 *
 */
public class GVSTool {

	private static final Log log = LogFactory.getLog(GVSTool.class);
	
	private static final String invocationString = "java -jar gvs.jar";
	
	private static Map<String, Class<? extends GVSToolCommand>> commandMap = new HashMap<String, Class<? extends GVSToolCommand>>();
	static {
		commandMap.put("services", ServicesCommand.class);
		commandMap.put("update", UpdateCommand.class);
		commandMap.put("get", GetCommand.class);
		commandMap.put("assert", AssertCommand.class);
		commandMap.put("download-remote", DownloadRemoteCommand.class);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			showHelp();
			return;
		}
		final String command = args[0];
		String[] restArgs = new String[args.length -1];
		System.arraycopy(args, 1,restArgs, 0, restArgs.length);
		for (Entry<String, Class<? extends GVSToolCommand>> commandEntry : commandMap.entrySet()) {
			final String currentCommand = commandEntry.getKey();
			if (currentCommand.equals(command)) {
				Class<? extends GVSToolCommand> clazz = commandEntry.getValue();
				log.info("Invoking command class: "+clazz.getName());
				GVSToolCommand instance = clazz.newInstance();
				instance.execute(invocationString+" "+currentCommand, restArgs);
				return;
			}
		}
		
		showHelp();
		

	}
	
	private static void showHelp() {
		System.out.println("Usage:");
		System.out.println(invocationString +" command arguments");
		System.out.println();
		System.out.println("Commands: ");
		for (Entry<String, Class<? extends GVSToolCommand>> commandEntry : commandMap.entrySet()) {
			Class<? extends GVSToolCommand> commandClass = commandEntry.getValue();
			System.out.println(commandEntry.getKey()+": "+commandClass.getAnnotation(CommandDescription.class).description());
		}
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

