// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Properties;

import org.cloudcoder.daemon.IDaemon;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JettyDaemon is an implementation of {@link IDaemon} specialized for
 * running a web application embedded as a resource.  This is meant to
 * make it easy to produce a single executable jarfile which contains
 * a webapp and the Jetty server required to run it. 
 * 
 * Note that the {@link #handleCommand(String)} method is not implemented;
 * subclasses must provide their own implementation (which can be a no-op).
 * 
 * @author David Hovemeyer
 * @see http://brandontilley.com/2010/03/27/serving-a-gwt-application-with-an-embedded-jetty-server.html
 */
public abstract class JettyWebappDaemon extends JettyDaemon<JettyWebappDaemonConfig> {
	private static Logger logger = LoggerFactory.getLogger(JettyWebappDaemon.class);
	
	private String webappUrl;
	private String extraClasspath;
	
	/**
	 * Specify an explicit URL for the webapp to be executed.
	 * Normally the webapp URL is constructed from the codebase of the
	 * executable jarfile, which is the desired behavior for
	 * deployment.  For development, however, it is useful to be able
	 * to hard-code the location of the webapp.
	 * 
	 * @param webappUrl the URL of the webapp
	 */
	public void setWebappUrl(String webappUrl) {
		this.webappUrl = webappUrl;
	}
	
	/**
	 * Set extra classpath entries to be used by the webapp.
	 * This is useful for running in development mode, where some classes
	 * needed by the webapp are being provided by other projects.
	 * Entries should be comma- or semicolon-separated.
	 * 
	 * @param extraClasspath the extra classpath entries to set
	 */
	public void setExtraClasspath(String extraClasspath) {
		this.extraClasspath = extraClasspath;
	}
	
	@Override
	protected void onCreateServer(Server server) {
		JettyWebappDaemonConfig jettyConfig = getJettyConfig();
		
		// Create an override-web.xml to override context parameters specified in the
		// webapp's web.xml.  Its web.xml contains configuration values appropriate
		// for development (which is useful), but we want the configuration values
		// specified for deployment when the user ran the configure.pl script
		// (in cloudcoder.properties).
		String overrideWebXml = null;
		Properties contextParamOverrides = jettyConfig.getContextParamOverrides();
		if (contextParamOverrides != null) {
			try {
				overrideWebXml = createOverrideWebXml(contextParamOverrides);
			} catch (IOException e) {
				throw new IllegalStateException("Couldn't create override-web.xml", e);
			}
		}
	
		// Create WebAppContext, running the web application embedded in the classpath
		// (unless a webapp URL is specified explicitly).
		WebAppContext handler = new WebAppContext();
		ProtectionDomain domain = getClass().getProtectionDomain();
		if (webappUrl == null) {
			// Attempt to determine the location of the webapp embedded in the classpath.
			String codeBase = domain.getCodeSource().getLocation().toExternalForm();
			if (codeBase.endsWith(".jar")) {
				// Running out of a jarfile: this is the preferred deployment option.
				webappUrl = "jar:" + codeBase + "!" + jettyConfig.getWebappResourcePath();
			} else {
				// Running from a directory. Untested.
				boolean endsInDir = codeBase.endsWith("/");
				if (endsInDir) {
					codeBase = codeBase.substring(0, codeBase.length() - 1);
				}
				webappUrl = codeBase + jettyConfig.getWebappResourcePath();
			}
		}
		handler.setWar(webappUrl);
		handler.setContextPath(jettyConfig.getContext());
		if (extraClasspath != null) {
			handler.setExtraClasspath(extraClasspath);
			logger.info("Extra classpath entries: {}", extraClasspath);
		}
		
		if (overrideWebXml != null) {
			// Configure the override-web.xml
			handler.setOverrideDescriptors(Arrays.asList(overrideWebXml));
		}
		
		// Don't allow directory listings
		handler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		
		// Allow the welcome file to be a servlet
		handler.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
	
		// Add it to the server
		server.setHandler(handler);
	}

	private String createOverrideWebXml(Properties configProperties) throws IOException {
		// It is somewhat unfortunate that Jetty doesn't allow overriding
		// context parameters through a programmatic API: that would
		// be much easier than having to create a file.  However,
		// we can at least be thankful that it is possible to override
		// context parameters at all, since it greatly simplifies the
		// process of configuring webapps for deployment.
		
		// Create a temp file
		File f = File.createTempFile("ccws", ".xml");
		f.deleteOnExit();
		
		FileOutputStream fos = new FileOutputStream(f);
		PrintWriter w = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
		try {
			w.println("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>");
			w.println("<web-app>");
			
			for (String prop : configProperties.stringPropertyNames()) {
				w.println("  <context-param>");
				w.println("    <param-name>" + prop + "</param-name>");
				w.println("    <param-value>" + configProperties.getProperty(prop) + "</param-value>");
				w.println("  </context-param>");
			}
			
			w.println("</web-app>");
		} finally {
			w.close();
		}
		
		return f.getAbsolutePath();
	}

}
