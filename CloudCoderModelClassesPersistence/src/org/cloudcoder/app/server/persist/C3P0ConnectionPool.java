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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Implementation of {@link IConnectionPool} using the C3P0 library. 
 * 
 * @author David Hovemeyer
 * @see http://www.mchange.com/projects/c3p0/index.html
 */
public class C3P0ConnectionPool extends AbstractConnectionPool {
	private ComboPooledDataSource cpds;
	
	/**
	 * Constructor.
	 * 
	 * @param config the database configuration properties
	 * @throws SQLException
	 */
	public C3P0ConnectionPool(JDBCDatabaseConfig.ConfigProperties config) throws SQLException {
		cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw new SQLException("Could not set JDBC driver class", e);
		}
		cpds.setJdbcUrl("jdbc:mysql://" +
				config.getHost() + config.getPortStr() +
				"/" +
				config.getDatabaseName());
		cpds.setUser(config.getUser());
		cpds.setPassword(config.getPasswd());
		
		// Turn on connection pooling
		// TODO: these should be tunable based on CloudCoder config properties
		cpds.setMinPoolSize(10);
		cpds.setMaxPoolSize(150);
		
		// Turn on prepared statement pooling
		cpds.setMaxStatements(180);
	}
	
	@Override
	public void destroy() throws SQLException {
		cpds.close();
	}
	
	@Override
	protected Connection createConnection() throws SQLException {
		return cpds.getConnection();
	}
	
	@Override
	protected void closeConnection(Connection conn) throws SQLException {
		// Because this is a C3P0 connection, closing it returns it
		// to the connection pool
		conn.close();
	}
}
