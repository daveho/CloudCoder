// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2017, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2017, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.JDBCDatabaseConfig.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IConnectionPool} for MySQL.
 * This class replicates the original behavior, where connections
 * and other JDBC objects were never reused, except connections
 * being reused if multiple requests were made by the same
 * thread.
 * 
 * @author David Hovemeyer
 */
public class MysqlConnectionPool extends AbstractConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(MysqlConnectionPool.class);

	static {
		try {
			Class.forName(JDBCDatabase.JDBC_DRIVER_CLASS);
		} catch (Exception e) {
			throw new IllegalStateException("Could not load mysql jdbc driver", e);
		}
	}
	
	private String jdbcUrl;
	
	/**
	 * Constructor.
	 * 
	 * @param config the database configuration properties
	 */
	public MysqlConnectionPool(ConfigProperties config) {
		this.jdbcUrl =
				"jdbc:mysql://" +
				config.getHost() + config.getPortStr() +
				"/" +
				config.getDatabaseName() +
				"?user=" +
				config.getUser() +
				"&password=" + config.getPasswd();
		logger.debug("Database URL: "+jdbcUrl);
	}
	
	@Override
	protected Connection createConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl);
	}
	
	@Override
	protected void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}
	
	@Override
	public void destroy() {
		// We don't actually do anything here
	}
}
