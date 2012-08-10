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

import java.util.Properties;

import org.cloudcoder.daemon.IDaemon;
import org.cloudcoder.daemon.Util;
import org.cloudcoder.jetty.JettyDaemon;

/**
 * Implementation of {@link IDaemon} to start the CloudCoder web application
 * using an embedded Jetty server, to accept run-time configuration commands,
 * and to handle shutdown.
 * 
 * @author David Hovemeyer
 * @see http://brandontilley.com/2010/03/27/serving-a-gwt-application-with-an-embedded-jetty-server.html
 */
public class CloudCoderDaemon extends JettyDaemon {

	@Override
	protected Config getJettyConfig() {
		final Properties configProperties = loadProperties("cloudcoder.properties");
		final Properties log4jProperties = loadProperties("log4j.properties");
		
		return new Config() {
			@Override
			public int getPort() {
				return Integer.parseInt(configProperties.getProperty("cloudcoder.webserver.port", "8081"));
			}

			@Override
			public boolean isLocalhostOnly() {
				return Boolean.parseBoolean(configProperties.getProperty("cloudcoder.webserver.localhostonly", "true"));
			}
			
			@Override
			public String getContext() {
				return configProperties.getProperty("cloudcoder.webserver.contextpath", "/cloudcoder");
			}
			
			@Override
			public String getWebappResourcePath() {
				return "/war";
			}
			
			@Override
			public Properties getLog4jProperties() {
				return log4jProperties;
			}
			
			@Override
			public Properties getContextParamOverrides() {
				return configProperties;
			}
		};
	}

	@Override
	public void handleCommand(String command) {
		// TODO: implement
	}
	
	/**
	 * Load Properties from a properties file loaded from the classpath.
	 * 
	 * @param fileName name of properties file
	 * @return the Properties contained in the properties file
	 */
	protected Properties loadProperties(String fileName) {
		return Util.loadPropertiesFromResource(this.getClass().getClassLoader(), fileName);
	}
}
