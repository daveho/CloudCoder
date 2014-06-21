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

package org.cloudcoder.builderwebservice;

import java.util.Properties;

import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitServiceServletContextListener;
import org.cloudcoder.builderwebservice.servlets.Submit;
import org.cloudcoder.daemon.Util;
import org.cloudcoder.jetty.JettyDaemon;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Daemon to expose one or more builders as a web service.
 * 
 * @author David Hovemeyer
 */
public class CloudCoderBuilderWebServiceDaemon extends JettyDaemon<BuilderWebServiceJettyDaemonConfig> {
	private static final Logger logger = LoggerFactory.getLogger(CloudCoderBuilderWebServiceDaemon.class);
	
	private Properties cloudcoderProperties;
	
	public void setCloudcoderProperties(Properties cloudcoderProperties) {
		this.cloudcoderProperties = cloudcoderProperties;
	}

	@Override
	protected BuilderWebServiceJettyDaemonConfig getJettyConfig() {
		final Properties log4jProperties =
				Util.loadPropertiesFromResource(this.getClass().getClassLoader(), "log4j.properties");
		if (cloudcoderProperties == null) {
			cloudcoderProperties = Util.loadPropertiesFromResource(this.getClass().getClassLoader(), "cloudcoder.properties");
		}
		
		return new BuilderWebServiceJettyDaemonConfig() {
			@Override
			public boolean isLocalhostOnly() {
				return Boolean.parseBoolean(cloudcoderProperties.getProperty("cloudcoder.builderwebservice.localhostonly", "true"));
			}
			
			@Override
			public int getPort() {
				return Integer.parseInt(cloudcoderProperties.getProperty("cloudcoder.builderwebservice.port", "8083"));
			}
			
			@Override
			public int getNumThreads() {
				return Integer.parseInt(cloudcoderProperties.getProperty("cloudcoder.builderwebservice.numThreads", "20"));
			}
			
			@Override
			public Properties getLog4jProperties() {
				return log4jProperties;
			}
			
			@Override
			public String getContextPath() {
				return cloudcoderProperties.getProperty("cloudcoder.builderwebservice.contextpath", "/bws");
			}
		};
	}

	@Override
	protected void onCreateServer(Server server, BuilderWebServiceJettyDaemonConfig config) {
		// Create the ServletContextHandler
		ServletContextHandler ctxHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		ctxHandler.setContextPath(config.getContextPath());
		server.setHandler(ctxHandler);
		
		// Register the OutOfProcessSubmitServiceServletContextListener,
		// which will manage connections from the builders
		ctxHandler.addEventListener(new OutOfProcessSubmitServiceServletContextListener());

		// Add the Submit servlet
		ctxHandler.addServlet(new ServletHolder(new Submit()), "/submit/*");
		
		// Set all cloudcoder properties as servlet context init params
		for (Object key_ : cloudcoderProperties.keySet()) {
			String key = (String) key_;
			String value = cloudcoderProperties.getProperty(key);
			System.out.println("Setting init parameter: " + key + "=>" + value);
			ctxHandler.setInitParameter(key, value);
		}
	}

	@Override
	public void handleCommand(String command) {
		// No commands are supported at this point
		logger.warn("Received unknown command: {}", command);
	}

}
