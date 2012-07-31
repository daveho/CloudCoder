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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
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
	private static final boolean DEBUG = false;
	
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
		configureLog4j();
		
		Scanner keyboard = new Scanner(System.in);
		
		System.out.print("Enter a username for your CloudCoder account: ");
		String ccUserName = keyboard.nextLine();
		
		System.out.print("Enter a password for your CloudCoder account: ");
		String ccPassword = keyboard.nextLine();
		
		System.out.print("What is your institution name (e.g, 'Unseen University')? ");
		String ccInstitutionName = keyboard.nextLine();
		
		Class.forName("com.mysql.jdbc.Driver");

		ConfigProperties config = new ConfigProperties();
		
		String dbUser = config.get("user");
		String dbPasswd = config.get("passwd");
		String dbName = config.get("databaseName");
		String dbHost = config.get("host");
		
		// Connect to the database server, but don't specify a database name 
		Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/?user=" + dbUser + "&password=" + dbPasswd);
		
		System.out.println("Creating database");
		DBUtil.execSql(conn, "create database " + dbName);
		
		conn.close();
		
		// Reconnect to the newly-created database
		conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/" + dbName + "?user=" + dbUser + "&password=" + dbPasswd);
		
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
		
		// Create initial database contents
		
		// Set institution name
		ConfigurationSetting instName = new ConfigurationSetting();
		instName.setName(ConfigurationSettingName.PUB_TEXT_INSTITUTION);
		instName.setValue(ccInstitutionName);
		storeBean(conn, instName, ConfigurationSetting.SCHEMA, JDBCDatabase.CONFIGURATION_SETTINGS);
		
		// Terms
		System.out.println("Creating terms...");
		storeTerm(conn, "Winter", 0);
		storeTerm(conn, "Spring", 1);
		storeTerm(conn, "Summer", 2);
		storeTerm(conn, "Summer 1", 3);
		storeTerm(conn, "Summer 2", 4);
		Term fall = storeTerm(conn, "Fall", 5);
		
		// Create an initial demo course
		System.out.println("Creating demo course...");
		Course course = new Course();
		course.setName("CCDemo");
		course.setTitle("CloudCoder demo course");
		course.setTermId(fall.getId());
		course.setTerm(fall);
		course.setYear(2012);
		course.setUrl("http://cloudcoder.org/");
		storeBean(conn, course, Course.SCHEMA, JDBCDatabase.COURSES);
		
		// Create an initial user
		System.out.println("Creating initial user...");
		User user = new User();
		user.setUsername(ccUserName);
		user.setPasswordHash(BCrypt.hashpw(ccPassword, BCrypt.gensalt(12)));
		storeBean(conn, user, User.SCHEMA, JDBCDatabase.USERS);
		
		// Register the user as an instructor in the demo course
		System.out.println("Registering initial user for demo course...");
		CourseRegistration courseReg = new CourseRegistration();
		courseReg.setCourseId(course.getId());
		courseReg.setUserId(user.getId());
		courseReg.setRegistrationType(CourseRegistrationType.INSTRUCTOR);
		courseReg.setSection(101);
		storeBean(conn, courseReg, CourseRegistration.SCHEMA, JDBCDatabase.COURSE_REGISTRATIONS);
		
		// Create a Problem
		System.out.println("Creating hello, world problem in demo course...");
		Problem problem = new Problem();
		problem.setCourseId(course.getId());
		problem.setWhenAssigned(System.currentTimeMillis());
		problem.setWhenDue(problem.getWhenAssigned() + (24L*60*60*1000));
		problem.setVisible(true);
		problem.setProblemType(ProblemType.C_PROGRAM);
		problem.setTestname("hello");
		problem.setBriefDescription("Print hello, world");
		problem.setDescription(
				"<p>Print a line with the following text:</p>\n" +
				"<blockquote><pre>Hello, world</pre></blockquote>\n"
		);			// At the moment, we don't need to allow NULL field values.

		problem.setSkeleton(
				"#include <stdio.h>\n\n" +
				"int main(void) {\n" +
				"\t// TODO - add your code here\n\n" +
				"\treturn 0;\n" +
				"}\n"
				);
		problem.setSchemaVersion(Problem.CURRENT_SCHEMA_VERSION);
		problem.setAuthorName("A. User");
		problem.setAuthorEmail("auser@cs.unseen.edu");
		problem.setAuthorWebsite("http://cs.unseen.edu/~auser");
		problem.setTimestampUtc(System.currentTimeMillis());
		problem.setLicense(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0);
		
		storeBean(conn, problem, Problem.SCHEMA, JDBCDatabase.PROBLEMS);
		
		// Add a TestCase
		System.out.println("Creating test case for hello, world problem...");
		TestCase testCase = new TestCase();
		testCase.setProblemId(problem.getProblemId());
		testCase.setTestCaseName("hello");
		testCase.setInput("");
		testCase.setOutput("^\\s*Hello\\s*,\\s*world\\s*$i");
		testCase.setSecret(false);
		
		storeBean(conn, testCase, TestCase.SCHEMA, JDBCDatabase.TEST_CASES);
		
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

	private static Term storeTerm(Connection conn, String name, int seq) throws SQLException {
		Term term = new Term();
		term.setName(name);
		term.setSeq(seq);
		storeBean(conn, term, Term.SCHEMA, JDBCDatabase.TERMS);
		return term;
	}

	// Use introspection to store an arbitrary bean in the database.
	// Eventually we could use this sort of approach to replace much
	// of our hand-written JDBC code, although I don't know how great
	// and idea that would be (for example, it might not yield adequate
	// performance.)  For just creating the database, it should be
	// fine.
	private static void storeBean(Connection conn, Object bean, ModelObjectSchema schema, String tableName) throws SQLException {
		StringBuilder buf = new StringBuilder();
		
		buf.append("insert into " + tableName);
		buf.append(" values (");
		buf.append(DBUtil.getInsertPlaceholdersNoId(schema));
		buf.append(")");
		
		PreparedStatement stmt = null;
		ResultSet genKeys = null;
		
		try {
			stmt = conn.prepareStatement(buf.toString(), schema.hasUniqueId() ? PreparedStatement.RETURN_GENERATED_KEYS : 0);
			
			// Now for the magic: iterate through the schema fields
			// and bind the query parameters based on the bean properties.
			int index = 1;
			for (ModelObjectField field : schema.getFieldList()) {
				if (field.isUniqueId()) {
					continue;
				}
				try {
					Object value = PropertyUtils.getProperty(bean, field.getPropertyName());
					if (value instanceof Enum) {
						// Enum values are converted to integers
						value = Integer.valueOf(((Enum<?>)value).ordinal());
					}
					stmt.setObject(index++, value);
				} catch (Exception e) {
					throw new SQLException(
							"Couldn't get property " + field.getPropertyName() +
							" of " + bean.getClass().getName() + " object");
				}
			}
			
			// Execute the insert
			stmt.executeUpdate();
			
			if (schema.hasUniqueId()) {
				genKeys = stmt.getGeneratedKeys();
				if (!genKeys.next()) {
					throw new SQLException("Couldn't get generated id for " + bean.getClass().getName()); 
				}
				int id = genKeys.getInt(1);
				
				// Set the unique id value in the bean
				try {
					BeanUtils.setProperty(bean, schema.getUniqueIdField().getPropertyName(), id);
				} catch (Exception e) {
					throw new SQLException("Couldn't set generated unique id for " + bean.getClass().getName());
				}
			}
		} finally {
			DBUtil.closeQuietly(genKeys);
			DBUtil.closeQuietly(stmt);
		}
	}
	
	private static void configureLog4j() {
		// See: http://robertmaldon.blogspot.com/2007/09/programmatically-configuring-log4j-and.html
		Logger rootLogger = Logger.getRootLogger();
		if (!rootLogger.getAllAppenders().hasMoreElements()) {
			rootLogger.setLevel(Level.INFO);
			rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
		}
	}
}
