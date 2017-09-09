/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: GVSServerLauncher.java,v 1.8 2007/06/27 10:26:49 rebach Exp $
 */
package com.hp.hpl.jena.gvs.services.http;

import java.io.IOException;
import java.net.URL;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.util.dirbrowser.MultiPathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.filter.impl.FilterRunner;

import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.security.DefaultPolicy;
import com.hp.hpl.jena.gvs.security.GVSConfiguration;
import com.hp.hpl.jena.gvs.services.http.accountmanager.AccountManager;
import com.hp.hpl.jena.gvs.services.http.graphserver.GraphHandlerAdaptor;
import com.hp.hpl.jena.gvs.services.http.security.AuthenticatingFilter;

/**
 * @author reto
 * 
 */
public class GVSServerLauncher {

	private static final Log log = LogFactory.getLog(GVSServerLauncher.class);

	public static final WebServer launchGVSServer(final Store store,
			ServerBinding serverBinding, Source identity,
			Set<Source> trustedSources, NamedNode configuration)
			throws IOException {
		if (trustedSources.contains(identity)) {
			Set<Source> newTrustedSources = new HashSet<Source>(trustedSources);
			newTrustedSources.add(identity);
			trustedSources = newTrustedSources;
		}
		GraphOverTime trustedGOT = store.getGraphOverTime(trustedSources);
		Configuration.setConfiguration(new GVSConfiguration(trustedGOT,
				"GVS-Auth"));
		Policy.setPolicy(new DefaultPolicy(trustedGOT));
		Map<String, Handler> prefixHandlerMap = new HashMap<String, Handler>();
		Enumeration<URL> basePathURLEnum = GVSServerLauncher.class
				.getClassLoader().getResources("META-INF/web/application/");
		List<PathNode> nodeList = new ArrayList<PathNode>();
		while (basePathURLEnum.hasMoreElements()) {
			nodeList.add(PathNodeFactory.getPathNode(basePathURLEnum.nextElement()));
		}
		PathNode rootNode = new MultiPathNode(nodeList.toArray(new PathNode[nodeList.size()]));
		prefixHandlerMap.put("/application/", new ApplicationServerHandler(
				rootNode, "/application"));
		prefixHandlerMap.put("/put-infobit", new PutInfoBitHandler(store, identity));
		prefixHandlerMap.put("/meta/sources", new GraphHandlerAdaptor(
				new SourceGraphHandler(store)));
		prefixHandlerMap.put("/meta/account-manager", new GraphHandlerAdaptor(
				new AccountManager(store, identity, trustedSources,
						configuration)));
		prefixHandlerMap.put("/", new GraphHandlerAdaptor(new GVSGraphHandler(
				store, new DiscoBitHandler(trustedGOT))));
		return launchGVSServer(store, serverBinding, prefixHandlerMap, trustedGOT);

	}

	/**
	 * For matching a prefix only the path section before the first question
	 * mark is considered. The prefix must either match the full path, or end
	 * with a slash.
	 * 
	 * @param store
	 * @param serverBinding
	 * @param prefixHandlerMap
	 *            maps path-prefixes to handlers
	 * @param trustedGOT 
	 * @return
	 * @throws IOException
	 */
	private static final WebServer launchGVSServer(final Store store,
			ServerBinding serverBinding,
			final Map<String, Handler> prefixHandlerMap, GraphOverTime trustedGOT) throws IOException {
		WebServerFactory webServerFactory = WebServerFactory.newInstance();

		Handler terminator = new Handler() {

			public void handle(Request request, Response response)
					throws HandlerException {
				String requestURI = request.getRequestURI().getPath();
				String[] userAgents = request.getHeaderValues(HeaderName.USER_AGENT);
				String userAgent;
				if (userAgents.length > 0) {
					userAgent = userAgents[0];
				} else {
					userAgent = "unknown";
				}
				log.info(request.getMethod().getName()+" "+request.getRequestURI().getAbsPath()+" "+userAgent);
				while (true) {
					Handler handler = prefixHandlerMap.get(requestURI);
					if (handler != null) {
						handler.handle(request, response);
						return;
					}
					if (requestURI.equals("/")) {
						return;
					}
					requestURI = requestURI.substring(0,
							requestURI.lastIndexOf('/', requestURI
									.length() - 2) + 1);
				}
			};
		};
		Filter[] filters = new Filter[2];
		filters[0] = new AuthenticatingFilter();
		filters[1] = new InfoDiscoBitFilter(trustedGOT);
		WebServer webServer = webServerFactory.startNewWebServer(	
				new FilterRunner(filters, terminator), serverBinding);
		return webServer;
	}

	/**
	 * launches a gvs server onport 8686 with canned data
	 * 
	 * @param args
	 * @throws Exception
	 */
	/*
	 * public static void main(String[] args) throws Exception { ArgumentHandler
	 * argumentHandler = new ArgumentHandler(args); final
	 * GVSServerLauncherArguments arguments; try { arguments =
	 * AnnotatedInterfaceArguments.getInstance(
	 * GVSServerLauncherArguments.class, argumentHandler).getValueObject(); }
	 * catch (InvalidArgumentsException e) { System.err.println(e.getMessage());
	 * showHelp(); return; } final List<String> unusedArguments = new ArrayList<String>();
	 * argumentHandler.processArguments(new ArgumentProcessor() {
	 * 
	 * public void process(List<String> argumentList) throws
	 * InvalidArgumentsException { unusedArguments.addAll(argumentList);
	 *  }
	 * 
	 * 
	 * }); if (unusedArguments.size() > 0) { for (String unused :
	 * unusedArguments) { System.err.println("Argument not understood "+unused);
	 * showHelp(); return; } } ServerBinding serverBinding = new ServerBinding() {
	 * 
	 * public InetAddress getInetAddress() { try { return
	 * InetAddress.getByName("localhost"); } catch (UnknownHostException e) {
	 * throw new RuntimeException(e); } }
	 * 
	 * public int getPort() { return arguments.getPort(); }
	 *  }; Store store = new MemoryStoreImpl(); File directory = new
	 * File("testdata"); String baseURLString = "http://" +
	 * serverBinding.getInetAddress().getHostName() + ":" +
	 * serverBinding.getPort() + "/"; CannedDataReader.readCannedData(store,
	 * directory, baseURLString); launchGVSServer(store, serverBinding, null,
	 * null, null); AggregatorService aggregator = new AggregatorService(10,
	 * store, store .getGraphOverTime(Collections .singleton((Source) new
	 * SourceImpl(baseURLString + "aggregationmaster"))), new SourceImpl(
	 * baseURLString + "aggregator")); aggregator.start(); log.info("aggregator
	 * started");
	 *  }
	 * 
	 * 
	 * private static void showHelp() { System.out.println("Usage:");
	 * System.out.print("java -jar gvs.jar"); System.out.print(' ');
	 * System.out.println(AnnotatedInterfaceArguments
	 * .getArgumentsSyntax(GVSServerLauncherArguments.class)); PrintWriter out =
	 * new PrintWriter(System.out, true);
	 * AnnotatedInterfaceArguments.printArgumentDescriptions(
	 * GVSServerLauncherArguments.class, out); out.flush(); }
	 */

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

