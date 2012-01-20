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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A ServletContextListener to initialize configuration
 * parameters needed by JDBCDatabase.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabaseConfig implements ServletContextListener {
	private static JDBCDatabaseConfig instance;
	
	private String dbUser;
	private String dbPasswd;
	private String dbHost;
	private String dbPortStr;
	
	private static Object instanceLock = new Object();
	
	public JDBCDatabaseConfig() {
		// The app server should create the object automatically,
		// so we'll save a reference to it so JDBCDatabase can
		// use it.
		synchronized (instanceLock) {
			instance = this;
		}
	}
	
	public static JDBCDatabaseConfig getInstance() {
		synchronized (instanceLock) {
			return instance;
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
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent e) {
		synchronized (instanceLock) {
			dbUser = e.getServletContext().getInitParameter("cloudcoder.db.user");
			dbPasswd = e.getServletContext().getInitParameter("cloudcoder.db.passwd");
			dbHost = e.getServletContext().getInitParameter("cloudcoder.db.host");
			dbPortStr = e.getServletContext().getInitParameter("cloudcoder.db.portstr");
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent e) {
		instance = null; // allow garbage collection
	}
}
