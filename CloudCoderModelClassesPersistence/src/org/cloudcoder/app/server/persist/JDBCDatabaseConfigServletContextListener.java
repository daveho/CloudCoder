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

package org.cloudcoder.app.server.persist;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ServletContextListener to initialize the {@link JDBCDatabaseConfig}
 * based on init parameters in the servlet context.  Note that this is
 * an abstract class: a concrete subclass should be defined that
 * invokes the constructor with a "property prefix" string specifying
 * what set of configuration properties (e.g, "cloudcoder.db") should
 * be used to configure the database.
 * 
 * @author David Hovemeyer
 */
public abstract class JDBCDatabaseConfigServletContextListener implements ServletContextListener {
	private String propertyPrefix;
	
	/**
	 * Constructor.
	 * 
	 * @param propertyPrefix the prefix for database configuration properties:
	 *                       e.g., "cloudcoder.db" for the webapp,
	 *                       "cloudcoder.repoapp.db" for the repository webapp, etc.
	 */
	public JDBCDatabaseConfigServletContextListener(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent e) {
		// Initialize the JDBCDatabaseConfig singleton from the
		// init params in the servlet context.
		JDBCDatabaseConfig.create(new JDBCDatabaseConfig.ConfigProperties() {
			@Override
			public String getUser() {
				return getParam(e.getServletContext(), propertyPrefix + ".user", "root");
			}

			@Override
			public String getPasswd() {
				return getParam(e.getServletContext(), propertyPrefix + ".passwd", "root");
			}

			@Override
			public String getDatabaseName() {
				return getParam(
						e.getServletContext(),
						propertyPrefix + ".databaseName",
						propertyPrefix.equals("cloudcoder.db") ? "cloudcoder" : "cloudcoderrepodb");
			}

			@Override
			public String getHost() {
				return getParam(e.getServletContext(), propertyPrefix + ".host", "localhost");
			}

			@Override
			public String getPortStr() {
				// As a special case, the port string will be
				// returned as ":8889" (for MAMP) if cloudcoder.db.checkmacos is true
				// and the os.name system property contains "OS X".  This allows
				// development mode to work seamlessly on MacOS without any
				// changes to web.xml.
				String checkMacOS = e.getServletContext().getInitParameter(propertyPrefix + ".checkmacos");
				if (checkMacOS != null
						&& Boolean.parseBoolean(checkMacOS) == true
						&& System.getProperty("os.name").contains("OS X")) {
					return ":8889";
				}
				return getParam(e.getServletContext(), propertyPrefix + ".portStr", "");
			}
		});
	}
	
	private String getParam(ServletContext ctx, String paramName, String defaultValue) {
		String value = ctx.getInitParameter(paramName);
		return (value != null) ? value : defaultValue; 
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent e) {
		JDBCDatabaseConfig.destroy();
	}
}
