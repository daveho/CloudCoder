// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.app.server.persist;

/**
 * JDBCDatabase configuration properties singleton.
 * Must be initialized using the create() method before
 * {@link JDBCDatabase} is used.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabaseConfig {
	/**
	 * Interface describing the database configuration properties.
	 */
	public interface ConfigProperties {
		/**
		 * @return the database user
		 */
		public String getUser();
		
		/**
		 * @return the database password
		 */
		public String getPasswd();
		
		/**
		 * @return the database name
		 */
		public String getDatabaseName();
		
		/**
		 * @return the database host
		 */
		public String getHost();
		
		/**
		 * @return the database port string (e.g., ":8889" if using MAMP, empty string if MySQL is listening on its default port)
		 */
		public String getPortStr();
	}
	
	private static JDBCDatabaseConfig instance;
	private static Object instanceLock = new Object();
	
	private ConfigProperties configProperties;
	
	private JDBCDatabaseConfig(ConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	/**
	 * @return the singleton instance of JDBCDatabaseConfig
	 */
	public static JDBCDatabaseConfig getInstance() {
		synchronized (instanceLock) {
			return instance;
		}
	}

	/**
	 * Create the singleton instance of JDBCDatabaseConfig.
	 * 
	 * @param configProperties the configuration properties
	 */
	public static void create(ConfigProperties configProperties) {
		synchronized (instanceLock) {
			if (instance != null) {
				throw new IllegalStateException("JDBCDatabaseConfig already exists");
			}
			instance = new JDBCDatabaseConfig(configProperties);
		}
	}
	
	/**
	 * Destroy the singleton instance of JDBCDatabaseCOonfig.
	 */
	public static void destroy() {
		synchronized (instanceLock) {
			instance = null;
		}
	}
	
	/**
	 * Get the database configuration properties. 
	 * @return the database configuration properties
	 */
	public ConfigProperties getConfigProperties() {
		return configProperties;
	}
}
