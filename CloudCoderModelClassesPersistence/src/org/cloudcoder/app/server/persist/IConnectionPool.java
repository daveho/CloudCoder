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

/**
 * Interface for database connection pool operations.
 * Connections are thread-local, so any thread that calls
 * {@link #getConnection()} should always use try/finally
 * to ensure that {@link #releaseConnection()} will be called
 * when the connection is no longer needed.
 * 
 * @author David Hovemeyer
 */
public interface IConnectionPool {
	/**
	 * Get the thread-local connection, creating a new connection
	 * (or reusing an unused connection) if necessary.
	 * 
	 * @return the thread-local connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException;
	
	/**
	 * Release the thread-local connection.
	 * 
	 * @throws SQLException
	 */
	public void releaseConnection() throws SQLException;
	
	/**
	 * Clean up all resources associated with this connection pool.
	 * @throws SQLException 
	 */
	public void destroy() throws SQLException;
}
