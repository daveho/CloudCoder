// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.webserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.cloudcoder.daemon.IDaemon;
import org.cloudcoder.daemon.Util;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Implementation of {@link IDaemon} to start the CloudCoder web application
 * using an embedded Jetty server, to accept run-time configuration commands,
 * and to handle shutdown.
 * 
 * @author David Hovemeyer
 * @see http://brandontilley.com/2010/03/27/serving-a-gwt-application-with-an-embedded-jetty-server.html
 */
public class CloudCoderDaemon implements IDaemon {
	/**
	 * Options for launching the webserver and webapp,
	 * as specified in the CloudCoder configuration properties.
	 */
	private class CloudCoderConfig {
		private Properties configProperties;
		
		public CloudCoderConfig(Properties configProperties) {
			this.configProperties = configProperties;
		}

		public int getPort() {
			return Integer.parseInt(configProperties.getProperty("cloudcoder.webserver.port", "8081"));
		}

		public boolean isLocalhostOnly() {
			return Boolean.parseBoolean(configProperties.getProperty("cloudcoder.webserver.localhostonly", "true"));
		}

		public String getContext() {
			return configProperties.getProperty("cloudcoder.webserver.contextpath", "/cloudcoder");
		}
		
	}

	private Server server;

	@Override
	public void start(String instanceName) {
		// Configure logging
		configureLogging();

		// Load the configuration properties embedded in the executable jarfile
		Properties configProperties = loadProperties("cloudcoder.properties");
		CloudCoderConfig config = new CloudCoderConfig(configProperties);
		
		// Create an override-web.xml to override context parameters specified in the
		// webapp's web.xml.  Its web.xml contains configuration values appropriate
		// for development (which is useful), but we want the configuration values
		// specified for deployment when the user ran the configure.pl script
		// (in cloudcoder.properties).
		String overrideWebXml;
		try {
			overrideWebXml = createOverrideWebXml(configProperties);
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't create override-web.xml", e);
		}
		
		// Create an embedded Jetty server
		this.server = new Server();
		
		// Create a connector
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(config.getPort());
		if (config.isLocalhostOnly()) {
		    //System.out.println("happening?");
			connector.setHost("localhost");
		}
		server.addConnector(connector);

		// Create WebAppContext, running the web application embedded in /war
		// in the classpath.
		WebAppContext handler = new WebAppContext();
		ProtectionDomain domain = getClass().getProtectionDomain();
		String codeBase = domain.getCodeSource().getLocation().toExternalForm();
		if (codeBase.endsWith(".jar")) {
			// Running out of a jarfile: this is the preferred deployment option.
			handler.setWar("jar:" + codeBase + "!/war");
		} else {
			// Running from a directory. Untested.
			boolean endsInDir = codeBase.endsWith("/");
			handler.setWar(codeBase + (endsInDir ? "" : "/") + "war");
		}
		handler.setContextPath(config.getContext());
		
		// Configure the override-web.xml
		handler.setOverrideDescriptors(Arrays.asList(overrideWebXml));

		// Add it to the server
		server.setHandler(handler);

		// Other misc. options
		server.setThreadPool(new QueuedThreadPool(20));

		// And start it up
		System.out.println("Starting up the server...");
		try {
			server.start();
		} catch (Exception e) {
			System.err.println("Could not start server: " + e.getMessage());
		}
	}
	
	private String createOverrideWebXml(Properties configProperties) throws IOException {
		// It is somewhat unfortunate that Jetty doesn't allow overriding
		// context parameters through a programmatic API: that would
		// be much easier than having to create a file.  However,
		// we can at least be thankful that it is possible to override
		// context parameters at all, since it greatly simplifies the
		// process of configuring CloudCoder for deployment.
		
		// Create a temp file
		File f = File.createTempFile("ccws", ".xml");
		f.deleteOnExit();
		
		FileOutputStream fos = new FileOutputStream(f);
		PrintWriter w = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
		try {
			w.println("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>");
			w.println("<web-app>");
			
			for (String prop : configProperties.stringPropertyNames()) {
				if (!prop.startsWith("cloudcoder.")) {
					continue;
				}
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

	private void configureLogging() {
		Properties log4jProperties = loadProperties("log4j.properties");
		PropertyConfigurator.configure(log4jProperties);
	}

	/**
	 * Load Properties from a properties file loaded from the classpath.
	 * 
	 * @param fileName name of properties file
	 * @return the Properties contained in the properties file
	 */
	protected Properties loadProperties(String fileName) {
		ClassLoader clsLoader = this.getClass().getClassLoader();
		return Util.loadPropertiesFromResource(clsLoader, fileName);
	}

	@Override
	public void handleCommand(String command) {
		// TODO: implement
	}

	@Override
	public void shutdown() {
		try {
			System.out.println("Stopping the server...");
			server.stop();
			System.out.println("Waiting for server to finish...");
			server.join();
			System.out.println("Server is finished");
		} catch (Exception e) {
			System.out.println("Exception shutting down Jetty server");
			e.printStackTrace(System.out);
		}
	}
}
