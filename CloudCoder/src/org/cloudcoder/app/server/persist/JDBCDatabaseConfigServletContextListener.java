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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ServletContextListener to initialize the {@link JDBCDatabaseConfig}
 * based on init parameters in the servlet context.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabaseConfigServletContextListener implements ServletContextListener {
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent e) {
		// Initialize the JDBCDatabaseConfig singleton from the
		// init params in the servlet context.
		JDBCDatabaseConfig.create(new JDBCDatabaseConfig.ConfigProperties() {
			@Override
			public String getDbConfigProperty(String name) {
				// As a special case, the cloudcoder.db.portStr property will be
				// returned as ":8889" (for MAMP) if cloudcoder.db.checkmacos is true
				// and the os.name system property contains "OS X".  This allows
				// development mode to work seamlessly on MacOS without any
				// changes to web.xml.
				if (name.equals("cloudcoder.db.portStr")) {
					String checkMacOS = e.getServletContext().getInitParameter("cloudcoder.db.checkmacos");
					if (checkMacOS != null
							&& Boolean.parseBoolean(checkMacOS) == true
							&& System.getProperty("os.name").contains("OS X")) {
						return ":8889";
					}
				}
				
				return e.getServletContext().getInitParameter(name);
			}
		});
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent e) {
		JDBCDatabaseConfig.destroy();
	}
}
