package org.cloudcoder.webserver;

import java.security.ProtectionDomain;
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
		Properties configProperties = loadProperties("local.properties");
		CloudCoderConfig config = new CloudCoderConfig(configProperties);
		
		// Configure logging
		configureLogging();
		
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
		ClassLoader clsLoader = this.getClass().getClassLoader();
		return Util.loadPropertiesFromResource(clsLoader, propFilePath);
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
