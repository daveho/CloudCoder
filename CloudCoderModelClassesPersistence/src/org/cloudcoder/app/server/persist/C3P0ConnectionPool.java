package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * Implementation of {@link IConnectionPool} using the C3P0 library. 
 * 
 * @author David Hovemeyer
 * @see http://www.mchange.com/projects/c3p0/index.html
 */
public class C3P0ConnectionPool implements IConnectionPool {
	
	private static class ThreadLocalConnection {
		Connection conn;
		int refCount;
	}

	private ThreadLocal<ThreadLocalConnection> tlc;
	private ComboPooledDataSource cpds;
	
	/**
	 * Constructor.
	 * 
	 * @param config the database configuration properties
	 * @throws SQLException
	 */
	public C3P0ConnectionPool(JDBCDatabaseConfig.ConfigProperties config) throws SQLException {
		tlc = new ThreadLocal<ThreadLocalConnection>();
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
	public Connection getConnection() throws SQLException {
		ThreadLocalConnection c = tlc.get();
		if (c == null) {
			c = new ThreadLocalConnection();
			tlc.set(c);
			c.conn = cpds.getConnection();
			c.refCount = 0;
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
		}
		c.conn.close(); // will return the underlying JDBC connection to the connection pool
	}

	@Override
	public void destroy() throws SQLException {
		DataSources.destroy(cpds);
	}
}
