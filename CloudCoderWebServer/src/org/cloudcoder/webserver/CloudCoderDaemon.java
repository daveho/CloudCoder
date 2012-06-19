package org.cloudcoder.webserver;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.cloudcoder.daemon.IDaemon;
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
	// TODO: eventually, this will be a resource within the classpath
	private static final String WEB_APP_DIR_NAME = "cloudCoder";
	
	/**
	 * Options for launching the webserver and webapp,
	 * as specified in the CloudCoder configuration properties.
	 */
	private class Options {
		private Properties configProperties;
		
		public Options(Properties configProperties) {
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
		Properties configProperties = loadProperties("local.properties");
		Options options = new Options(configProperties);
		
		// Configure logging
		configureLogging();
		
		// Create an embedded Jetty server
		this.server = new Server();
		
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

		// Make all cloudcoder.* configuration parameters available
		// as context init parameters.
		for (String key : configProperties.stringPropertyNames()) {
			if (key.startsWith("cloudcoder.")) {
				String value = configProperties.getProperty(key);
				handler.setInitParameter(key, value);
			}
		}

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
		String propFilePath = this.getClass().getPackage().getName().replace('.', '/') + "/" + fileName;
		URL propURL = this.getClass().getClassLoader().getResource(propFilePath);
		if (propURL == null) {
			throw new IllegalStateException("Couldn't find properties " + propFilePath);
		}
		Properties properties = new Properties();
		try {
			InputStream in = propURL.openStream();
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't load properties " + propFilePath);
		}
		return properties;
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

	/*
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
 */

}
