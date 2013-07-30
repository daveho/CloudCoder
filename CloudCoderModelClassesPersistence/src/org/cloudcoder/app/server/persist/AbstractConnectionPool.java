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
 * Abstract base class for {@link IConnectionPool} implementations.
 * All that is required of subclasses is to implement the
 * {@link #createConnection()} and {@link #closeConnection(Connection)}
 * downcall methods.
 * 
 * @author David Hovemeyer
 */
public abstract class AbstractConnectionPool implements IConnectionPool {

	private static class ThreadLocalConnection {
		Connection conn;
		int refCount;
	}

	private ThreadLocal<ThreadLocalConnection> tlc;

	/**
	 * Constructor.
	 */
	public AbstractConnectionPool() {
		tlc = new ThreadLocal<ThreadLocalConnection>();
	}

	@Override
	public Connection getConnection() throws SQLException {
		ThreadLocalConnection c = tlc.get();
		if (c == null) {
			c = new ThreadLocalConnection();
			c.conn = createConnection();
			c.refCount = 0;
			tlc.set(c);
		}
		c.refCount++;
		return c.conn;
	}

	@Override
	public void releaseConnection() throws SQLException {
		ThreadLocalConnection c = tlc.get();
		if (c == null || c.refCount == 0) {
			throw new IllegalStateException("Releasing non-existent connection");
		}
		c.refCount--;
		if (c.refCount == 0) {
			tlc.set(null);
			closeConnection(c.conn);
		}
	}

	/**
	 * Downcall method to create a new {@link Connection} to the database.
	 * 
	 * @return a new {@link Connection} to the database
	 * @throws SQLException
	 */
	protected abstract Connection createConnection() throws SQLException;
	
	/**
	 * Downcall method called when a thread is done with a {@link Connection}.
	 * Should either close the connection or return it to the connection pool.
	 * 
	 * @param conn the {@link Connection} that is no longer needed by the current thread
	 * @throws SQLException
	 */
	protected abstract void closeConnection(Connection conn) throws SQLException;
}
