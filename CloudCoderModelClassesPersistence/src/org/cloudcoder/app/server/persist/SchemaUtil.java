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
import java.sql.Statement;
import java.util.Properties;

import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Problem;

/**
 * Utilities for working with table schema versions and migrations.
 * 
 * @author David Hovemeyer
 */
public class SchemaUtil {
	/**
	 * Create the schema version table if it does not already exist.
	 * 
	 * @param conn  the Connection to the database (which must have the
	 *              CloudCoder webapp or repository database set as the current
	 *              database)
	 * @param tables the list of tables that the database contains
	 * @throws SQLException
	 */
	public static void createSchemaVersionTableIfNeeded(Connection conn, ModelObjectSchema<?>[] tables) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Statement create = null;
		PreparedStatement insert = null;
		
		try {
			stmt = conn.prepareStatement(
					"select count(*) from information_schema.tables " +
					" where table_schema = DATABASE() " +
					"   and table_name = 'cc_schema_version'"
			);
			
			resultSet = stmt.executeQuery();
			
			if (!resultSet.next()) {
				throw new SQLException("Couldn't retrieve information from information_schema.tables");
			}
			
			int count = resultSet.getInt(1);
			if (count == 0) {
				// schema version table doesn't exist yet, so create it
				create = conn.createStatement();
				create.execute(
						"create table cc_schema_version (" +
						"  table_name varchar(50) PRIMARY KEY, " +
						"  schema_version MEDIUMINT " +
						")"
				);
				
				insert = conn.prepareStatement("insert into cc_schema_version values (?, ?)");
				for (ModelObjectSchema<?> table : tables) {
					insert.setString(1, table.getDbTableName());
					insert.setInt(2, 0);
					insert.addBatch();
				}
				
				insert.executeBatch();
			}
			
		} finally {
			DBUtil.closeQuietly(insert);
			DBUtil.closeQuietly(create);
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
		}
	}
	
	/**
	 * Get the schema version number for given table.
	 * 
	 * @param conn
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public static int getDbSchemaVersion(Connection conn, ModelObjectSchema<?> table)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = conn.prepareStatement(
					"select schema_version from cc_schema_version " +
					" where table_name = ?"
			);
			stmt.setString(1, table.getDbTableName());
			
			resultSet = stmt.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("No entry in cc_schema_version for table " + table.getDbTableName());
			}
			
			return resultSet.getInt(1);
		} finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
		}
	}
	
	public static void main(String[] args) throws Exception {
		// Just for testing
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		createSchemaVersionTableIfNeeded(conn, CreateWebappDatabase.TABLES);
		System.out.println("Created schema version table");
		
		int problemsVersion = getDbSchemaVersion(conn, Problem.SCHEMA);
		System.out.println("cc_problems is at schema version " + problemsVersion);
	}
}
