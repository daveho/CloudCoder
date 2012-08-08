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

import org.slf4j.LoggerFactory;

/**
 * JDBCDatabase configuration properties singleton.
 * Must be initialized using the create() method before
 * {@link JDBCDatabase} is used.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabaseConfig {
	/**
	 * A source of database configuration properties.
	 */
	public interface ConfigProperties {
		/**
		 * Get the value of a configuration property.
		 * 
		 * @param name the name of the configuration property to get
		 * @return the value of the configuration property
		 */
		public String getDbConfigProperty(String name);
	}
	
	private static JDBCDatabaseConfig instance;
	private static Object instanceLock = new Object();
	
	private String dbUser;
	private String dbPasswd;
	private String dbDatabaseName;
	private String dbHost;
	private String dbPortStr;
	
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
			instance = new JDBCDatabaseConfig();
			instance.readDatabaseConfigProperties(configProperties);
		}
	}

	private void readDatabaseConfigProperties(ConfigProperties dbProperties) {
		dbUser = getDatabaseProperty(dbProperties, "cloudcoder.db.user");
		dbPasswd = getDatabaseProperty(dbProperties, "cloudcoder.db.passwd");
		dbDatabaseName = getDatabaseProperty(dbProperties, "cloudcoder.db.databaseName");
		dbHost = getDatabaseProperty(dbProperties, "cloudcoder.db.host");
		dbPortStr = getDatabaseProperty(dbProperties, "cloudcoder.db.portStr");
	}

	private String getDatabaseProperty(ConfigProperties dbProperties, String propertyName) {
		String value = dbProperties.getDbConfigProperty(propertyName);
		if (value == null) {
			throw new IllegalArgumentException("Database property " + propertyName + " is undefined");
		}
		LoggerFactory.getLogger(this.getClass()).info("Database property " + propertyName + "=" + value);
		return value;
	}
	
	/**
	 * Destroy the singleton instance of JDBCDatabaseCOonfig.
	 */
	public static void destroy() {
		synchronized (instanceLock) {
			instance = null;
		}
	}
	
	private static void check(String s) {
		if (s == null) {
			throw new IllegalStateException("database config param not set");
		}
	}
	
	/**
	 * @return the database username
	 */
	public String getDbUser() {
		check(dbUser);
		return dbUser;
	}
	
	/**
	 * @return the database password
	 */
	public String getDbPasswd() {
		check(dbPasswd);
		return dbPasswd;
	}
	
	/**
	 * @return the database name (defaults to "cloudcoder" if not explicitly set
	 *         in servlet context init params)
	 */
	public String getDbDatabaseName() {
		return dbDatabaseName != null ? dbDatabaseName : "cloudcoder";
	}
	
	/**
	 * @return the host on which the MySQL database is running
	 *         (returns "localhost" if no db host is set) 
	 */
	public String getDbHost() {
		return dbHost != null ? dbHost : "localhost";
	}
	
	/**
	 * @return the database port string (if the MySQL server is running
	 *         on a nonstandard port, then this should be of the form
	 *         :YYYY, e.g., :8889 for MAMP)
	 */
	public String getDbPortStr() {
		return dbPortStr != null ? dbPortStr : "";
	}
}
