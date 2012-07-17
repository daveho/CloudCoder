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

	public AbstractDatabaseRunnable() {
		super();
	}

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