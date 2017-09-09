/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: ApplicationServerHandler.java,v 1.3 2007/06/05 14:54:53 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.mediatypes.MimeType;
import org.wymiwyg.commons.rdf.mediatypes.MediaTypesUtil;
import org.wymiwyg.commons.util.dirbrowser.PathNameFilter;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

/** A minimalistic wrhapi handler for serving files from the webapp/ source directory for requestsURIs 
 * starting with the specified requestURIPrefix
 * 
 * @author reto
 * 
 */
public class ApplicationServerHandler implements Handler {

	private static final Log log = LogFactory.getLog(ApplicationServerHandler.class);
	
	PathNode rootNode;

	private int requestURIPrefixLength;

	/**
	 * Resolves URL-Paths from a hierarchy of PathNodeS
	 * 
	 * @param rootNode the root of the path-node hierarchy
	 * @param requestURIPrefix a prefix which is removed from requested URL-Paths 
	 * @throws IOException
	 */
	public ApplicationServerHandler(PathNode rootNode, String requestURIPrefix) throws IOException {
		this.rootNode = rootNode;
		this.requestURIPrefixLength = requestURIPrefix.length();
		log.info("ApplicationServerHandler instantiated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wymiwyg.wrhapi.Handler#handle(org.wymiwyg.wrhapi.Request,
	 *      org.wymiwyg.wrhapi.Response)
	 */
	public void handle(Request request, Response response)
			throws HandlerException {
		String path = request.getRequestURI().getPath();
		response.addHeader(HeaderName.CACHE_CONTROL, "max-age=300");
		response.addHeader(HeaderName.CACHE_CONTROL, "public");
		path = path.substring(requestURIPrefixLength, path.length());
		StringTokenizer tokens = new StringTokenizer(path, "/");
		PathNode currentBrowseNode = rootNode;
		while (tokens.hasMoreTokens()) {
			String currentToken = tokens.nextToken();
			if (!tokens.hasMoreTokens()) {
				selectBestChild(currentBrowseNode, currentToken,
						request, response);
				return;
			} else {
				currentBrowseNode = currentBrowseNode.getSubPath(currentToken);
			}
		}
		throw new HandlerException(ResponseStatus.NOT_FOUND, "file "+path+" not found");

	}

	/** Get a non-directory child of a pathnode, given a base-name and a request (for conetnt-negotiation)
	 * 
	 * @param currentBrowseNode
	 * @param currentToken
	 * @param request
	 * @param response 
	 * @return the child-PathNode of pathNode with that baseName and which best
	 *         suits the request
	 * @throws HandlerException 
	 */
	private void selectBestChild(PathNode pathNode, final String baseName,
			Request request, Response response) throws HandlerException {
		//log.info("getting "+baseName+" in "+pathNode);
		String[] childStrings = pathNode.list(new PathNameFilter() {

			public boolean accept(PathNode dir, String name) {
				//log.info("checking "+name+" in "+dir);
				if (!name.startsWith(baseName)) {
					return false;
				}
				if (dir.getSubPath(name).isDirectory()) {
					return false;
				}
				if (name.equals(baseName)) {
					return true;
				}
				if (name.charAt(baseName.length()) == '.') {
					return true;
				}
				// name starts with baseName, but is not followed by a dot
				return false;
			}
		});
		//childStrings is null if the directory doesn't exist
		if ((childStrings == null) || (childStrings.length == 0)) {
			throw new HandlerException(ResponseStatus.NOT_FOUND, "file "+baseName+" not found");
		}
		// TODO content negotiation
		String fileName = childStrings[0];
		MimeType mimeType = MediaTypesUtil.getDefaultInstance().getTypeForExtension(getExtension(fileName));
		if (mimeType != null) {
			response.addHeader(HeaderName.CONTENT_TYPE, mimeType.toString());
		}
		PathNode resultNode = pathNode.getSubPath(fileName);
		final InputStream dataInputStream;
		try {
			//log.info("getting stream from"+fileName+" in "+pathNode+"("+resultNode+")");
			dataInputStream = resultNode.getInputStream();
		} catch (IOException e) {
			throw new HandlerException(e);
		}
		response.setBody(new MessageBody2Read() {

			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(dataInputStream);
			}
			
		});
	}
	
	/** gets the last extension of a filename-like string
	 * @param string
	 * @return
	 */
	private static String getExtension(String string) {
		int lastDotPos = string.lastIndexOf('.');
		if (lastDotPos == -1) {
			return null;
		}
		return string.substring(lastDotPos+1);
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

