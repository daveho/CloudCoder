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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

/**
 * Abstract base class for database transactions.
 * Provides methods for creating resources (PreparedStatements, ResultSets)
 * that will be automatically cleaned up by the cleanup() method.
 * 
 * @author David Hovemeyer
 *
 * @param <E>
 */
public abstract class AbstractDatabaseRunnable<E> implements DatabaseRunnable<E> {
	private Stack<Object> cleanupStack = new Stack<Object>();
	
	protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);
		cleanupStack.push(stmt);
		return stmt;
	}
	
	protected PreparedStatement prepareStatement(Connection conn, String sql, int options) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql, options);
		cleanupStack.push(stmt);
		return stmt;
	}
	
	protected ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
		ResultSet resultSet = stmt.executeQuery();
		cleanupStack.push(resultSet);
		return resultSet;
	}

	public ResultSet getGeneratedKeys(PreparedStatement stmt) throws SQLException {
		ResultSet resultSet = stmt.getGeneratedKeys();
		cleanupStack.push(resultSet);
		return resultSet;
	}
	
	@Override
	public void cleanup() {
		while (!cleanupStack.isEmpty()) {
			Object o = cleanupStack.pop();
			if (o instanceof PreparedStatement) {
				DBUtil.closeQuietly((PreparedStatement) o);
			} else if (o instanceof ResultSet) {
				DBUtil.closeQuietly((ResultSet) o);
			}
		}
	}
}
