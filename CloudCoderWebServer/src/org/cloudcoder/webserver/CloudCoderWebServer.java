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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

// See: http://brandontilley.com/2010/03/27/serving-a-gwt-application-with-an-embedded-jetty-server.html
public class CloudCoderWebServer {
	private static final String WEB_APP_DIR_NAME = "cloudCoder";
	
	// Configuration options
	private static class Options {
		private int port;
		private String context;
		private boolean localhostOnly;
		
		public Options() {
			// Default options: listen on port 8001, context is /cloudcoder,
			// and only accept connections originating from localhost (which
			// is what we expect if the webapp is being reverse-proxied
			// (which is what I (DHH) am doing).
			this.port = 8081;
			this.context = "/cloudcoder";
			this.localhostOnly = true;
		}
		
		public int getPort() {
			return port;
		}
		
		public String getContext() {
			return context;
		}
		
		public boolean isLocalhostOnly() {
			return localhostOnly;
		}
		
		public void parse(String[] args) {
			for (String arg : args) {
				if (arg.startsWith("--port=")) {
					port = Integer.parseInt(arg.substring("--port=".length()));
				} else if (arg.startsWith("--context=")) {
					context = arg.substring("--context=".length());
				} else if (arg.startsWith("--localhostOnly=")) {
					localhostOnly = Boolean.parseBoolean(arg.substring("--localhostOnly=".length()));
					System.out.println("localhost only is: "+localhostOnly);
				} else {
					throw new IllegalArgumentException("Unknown argument: " + arg);
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.parse(args);
		
		// Configure logging
		configureLogging();
		
		// Create an embedded Jetty server
		Server server = new Server();
		
		// Create a connector
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(options.getPort());
		if (options.isLocalhostOnly()) {
		    //System.out.println("happening?");
			connector.setHost("localhost");
		}
		server.addConnector(connector);

		// Create WebAppContext
		WebAppContext handler = new WebAppContext();
		handler.setResourceBase("./apps/" + WEB_APP_DIR_NAME);
		handler.setDescriptor("./apps/" + WEB_APP_DIR_NAME + "/WEB-INF/web.xml");
		handler.setContextPath(options.getContext());
		handler.setParentLoaderPriority(true);

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
		
		// Wait until "quit" is written to the FIFO
		try {
			final String appName = "cloudCoder";
			BufferedReader reader = new BufferedReader(System.getProperty(appName + ".fifo") != null
					? new FileReader(System.getProperty(appName + ".fifo"))
					: new InputStreamReader(System.in));
			
			try {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						System.err.println("Reached EOF on FIFO?");
						break;
					}
					if (line.trim().toLowerCase().equals("quit")) {
						System.out.println("Quit command read from FIFO");
						break;
					}
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			System.err.println("IOException reading from FIFO: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		
		try {
			System.out.println("Stopping the server...");
			server.stop();
			System.out.println("Waiting for server to finish...");
			server.join();
			System.out.println("Server is finished");
		} catch (Exception e) {
			// Should not happen
			System.err.println("Exception shutting down the server");
			e.printStackTrace(System.err);
		}
	}

	private static void configureLogging() throws IOException {
		Properties log4jProperties = new Properties();
		InputStream in = new FileInputStream("./apps/cloudCoder/WEB-INF/classes/log4j.properties"); 
		try {
			log4jProperties.load(in);
		} finally {
			in.close();
		}
		
		// Configure log4j
		PropertyConfigurator.configure(log4jProperties);
	}
}
