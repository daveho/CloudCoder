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

package org.cloudcoder.repoapp.webserver;

import java.util.Properties;

import org.cloudcoder.daemon.Util;
import org.cloudcoder.jetty.JettyDaemon;

/**
 * Implementation of {@link IDaemon} for launching, handling commands,
 * and shutting down for the CloudCoder Repository webapp.
 * 
 * @author David Hovemeyer
 */
public class CloudCoderRepositoryDaemon extends JettyDaemon {
	private Properties configProperties;

	/**
	 * Set the configuration properties to use.
	 * If this method is not called, then the daemon will attempt to read
	 * properties from the "cloudcoder.properties" embedded resource.
	 * (Generally, calling this method is only needed when running
	 * in development, where we don't want to read an actual
	 * configuration properties file.)
	 * 
	 * @param properties configuration properties to use
	 */
	public void setConfigProperties(Properties properties) {
		this.configProperties = properties;
	}

	@Override
	protected Config getJettyConfig() {
		if (this.configProperties == null) {
			this.configProperties = loadProperties("cloudcoder.properties");
		}
		final Properties log4jProperties = loadProperties("log4j.properties");
		
		return new Config() {
			@Override
			public boolean isLocalhostOnly() {
				return Boolean.parseBoolean(configProperties.getProperty("cloudcoder.repoapp.webserver.localhostonly", "true"));
			}
			
			@Override
			public String getWebappResourcePath() {
				return "/war";
			}
			
			@Override
			public int getPort() {
				return Integer.parseInt(configProperties.getProperty("cloudcoder.repoapp.webserver.port", "8082"));
			}
			
			@Override
			public Properties getLog4jProperties() {
				return log4jProperties;
			}
			
			@Override
			public Properties getContextParamOverrides() {
				return configProperties;
			}
			
			@Override
			public String getContext() {
				return configProperties.getProperty("cloudcoder.repoapp.webserver.contextpath", "/repo");
			}
		};
	}

	private Properties loadProperties(String propFile) {
		return Util.loadPropertiesFromResource(this.getClass().getClassLoader(), propFile);
	}

	@Override
	public void handleCommand(String command) {
		// TODO: implement
	}
}
