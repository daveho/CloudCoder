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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Create the webapp database, using the metadata information
 * specified by the model classes.
 * 
 * @author David Hovemeyer
 */
public class CreateWebappDatabase {
	private static final boolean DEBUG = true;
	
	private static class ConfigProperties {
		private Properties properties;
		
		public ConfigProperties() throws FileNotFoundException, IOException {
			properties = new Properties();
			properties.load(new FileReader("../local.properties"));
		}
		
		public String get(String propName) {
			String value = properties.getProperty("cloudcoder.db." + propName);
			if (value == null) {
				throw new IllegalArgumentException("Unknown property: " + propName);
			}
			return value;
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		Class.forName("com.mysql.jdbc.Driver");

		ConfigProperties config = new ConfigProperties();
		
		String user = config.get("user");
		String passwd = config.get("passwd");
		String dbname = config.get("databaseName");
		String host = config.get("host");
		
		// Connect to the database server, but don't speCBcify a database name 
		Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + "/?user=" + user + "&password=" + passwd);
		
		System.out.println("Creating database");
		DBUtil.execSql(conn, "create database " + dbname);
		
		conn.close();
		
		// Reconnect to the newly-created database
		conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + dbname + "?user=" + user + "&password=" + passwd);
		
		// Create tables and indexes
		createTable(conn, JDBCDatabase.CHANGES, Change.SCHEMA);
		createTable(conn, JDBCDatabase.CONFIGURATION_SETTINGS, ConfigurationSetting.SCHEMA);
		createTable(conn, JDBCDatabase.COURSES, Course.SCHEMA);
		createTable(conn, JDBCDatabase.COURSE_REGISTRATIONS, CourseRegistration.SCHEMA);
		createTable(conn, JDBCDatabase.EVENTS, Event.SCHEMA);
		createTable(conn, JDBCDatabase.PROBLEMS, Problem.SCHEMA);
		createTable(conn, JDBCDatabase.SUBMISSION_RECEIPTS, SubmissionReceipt.SCHEMA);
		createTable(conn, JDBCDatabase.TERMS, Term.SCHEMA);
		createTable(conn, JDBCDatabase.TEST_CASES, TestCase.SCHEMA);
		createTable(conn, JDBCDatabase.TEST_RESULTS, TestResult.SCHEMA);
		createTable(conn, JDBCDatabase.USERS, User.SCHEMA);
		
		conn.close();
		
		System.out.println("Success!");
	}

	private static void createTable(Connection conn, String tableName, ModelObjectSchema schema) throws SQLException {
		System.out.println("Creating table " + tableName);
		String sql = DBUtil.getCreateTableStatement(schema, tableName);
		if (DEBUG) {
			System.out.println(sql);
		}
		DBUtil.execSql(conn, sql);
	}
}
