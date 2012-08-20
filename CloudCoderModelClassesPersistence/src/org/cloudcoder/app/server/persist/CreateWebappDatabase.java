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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
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
	static final boolean DEBUG = false;
	
	public static void main(String[] args) throws Exception {
		try {
			createWebappDatabase();
		} catch (SQLException e) {
			// Handle SQLException by printing an error message:
			// these are likely to be meaningful to the user
			// (for example, can't create the database because
			// it already exists.)
		    e.printStackTrace(System.err);
			System.err.println("Database error: " + e.getMessage());
		}
	}

	private static void createWebappDatabase() throws ClassNotFoundException,
			FileNotFoundException, IOException, SQLException {
		
		System.out.println("Please enter some information needed to configure the Cloudcoder");
		System.out.println("database.  (Hit enter to accept a default value, if there is one.)");
		
		Scanner keyboard = new Scanner(System.in);
		
		String ccUserName = ask(keyboard, "Enter a username for your CloudCoder account: ");
		String ccPassword = ask(keyboard, "Enter a password for your CloudCoder account");
		String ccInstitutionName = ask(keyboard, "What is your institution name (e.g, 'Unseen University')?");
		String ccRepoUrl = ask(keyboard, "Enter the URL of the exercise repository", "https://cloudcoder.org/repo");
		
		Class.forName("com.mysql.jdbc.Driver");

		Properties config = DBUtil.getConfigProperties();
		
		Connection conn = DBUtil.connectToDatabaseServer(config, "cloudcoder.db");
		
		String dbName = config.getProperty("cloudcoder.db.databaseName");
		if (dbName == null) {
			throw new IllegalStateException("configuration properties do not define cloudcoder.db.databaseName");
		}
		
		System.out.println("Creating database");
		DBUtil.createDatabase(conn, dbName);
		
		conn.close();
		
		// Reconnect to the newly-created database
		conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		
		// Create tables and indexes
		createTable(conn, Change.SCHEMA);
		createTable(conn, ConfigurationSetting.SCHEMA);
		createTable(conn, Course.SCHEMA);
		createTable(conn, CourseRegistration.SCHEMA);
		createTable(conn, Event.SCHEMA);
		createTable(conn, Problem.SCHEMA);
		createTable(conn, SubmissionReceipt.SCHEMA);
		createTable(conn, Term.SCHEMA);
		createTable(conn, TestCase.SCHEMA);
		createTable(conn, TestResult.SCHEMA);
		createTable(conn, User.SCHEMA);
		
		// Create initial database contents
		
		// Set institution name (and any other configuration settings)
		System.out.println("Adding configuration settings...");

//		ConfigurationSetting instName = new ConfigurationSetting();
//		instName.setName(ConfigurationSettingName.PUB_TEXT_INSTITUTION);
//		instName.setValue(ccInstitutionName);
//		DBUtil.storeBean(conn, instName, ConfigurationSetting.SCHEMA, JDBCDatabase.CONFIGURATION_SETTINGS);

		DBUtil.storeConfigurationSetting(conn, ConfigurationSettingName.PUB_TEXT_INSTITUTION, ccInstitutionName);
		DBUtil.storeConfigurationSetting(conn, ConfigurationSettingName.PUB_REPOSITORY_URL, ccRepoUrl);

		
		// Terms
		System.out.println("Creating terms...");
		CreateWebappDatabase.storeTerm(conn, "Winter", 0);
		CreateWebappDatabase.storeTerm(conn, "Spring", 1);
		CreateWebappDatabase.storeTerm(conn, "Summer", 2);
		CreateWebappDatabase.storeTerm(conn, "Summer 1", 3);
		CreateWebappDatabase.storeTerm(conn, "Summer 2", 4);
		Term fall = CreateWebappDatabase.storeTerm(conn, "Fall", 5);
		
		// Create an initial demo course
		System.out.println("Creating demo course...");
//<<<<<<< HEAD
//		Course course = new Course();
//		course.setName("CCDemo");
//		course.setTitle("CloudCoder demo course");
//		course.setTermId(fall.getId());
//		//TODO: Get current year
//		course.setTerm(fall);
//		course.setYear(2012);
//		course.setUrl("http://cloudcoder.org/");
//		DBUtil.storeBean(conn, course, Course.SCHEMA, JDBCDatabase.COURSES);
//		
//		// Create an initial user
//		System.out.println("Creating initial user...");
//		User user = new User();
//		user.setUsername(ccUserName);
//		user.setPasswordHash(BCrypt.hashpw(ccPassword, BCrypt.gensalt(12)));
//		DBUtil.storeBean(conn, user, User.SCHEMA, JDBCDatabase.USERS);
//		
//		// Register the user as an instructor in the demo course
//		System.out.println("Registering initial user for demo course...");
//		CourseRegistration courseReg = new CourseRegistration();
//		courseReg.setCourseId(course.getId());
//		courseReg.setUserId(user.getId());
//		courseReg.setRegistrationType(CourseRegistrationType.INSTRUCTOR);
//		courseReg.setSection(101);
//		DBUtil.storeBean(conn, courseReg, CourseRegistration.SCHEMA, JDBCDatabase.COURSE_REGISTRATIONS);
//=======
		int courseId = CreateSampleData.createDemoCourse(conn, fall);
		
		// Create an initial user
		System.out.println("Creating initial user...");
		int userId = CreateSampleData.createInitialUser(conn, ccUserName, ccPassword);
		
		// Register the user as an instructor in the demo course
		System.out.println("Registering initial user for demo course...");
		CreateSampleData.registerUser(conn, userId, courseId, CourseRegistrationType.INSTRUCTOR);
//>>>>>>> 4296caaa5da7c71fc6a038f2b7b12d08b969d1e2
		
		// Create a Problem
		System.out.println("Creating hello, world problem in demo course...");
		Problem problem = new Problem();
//<<<<<<< HEAD
//		problem.setCourseId(course.getId());
//		problem.setWhenAssigned(System.currentTimeMillis());
//		problem.setWhenDue(problem.getWhenAssigned() + (24L*60*60*1000));
//		problem.setVisible(true);
//		problem.setProblemType(ProblemType.C_PROGRAM);
//		problem.setTestname("hello");
//		problem.setBriefDescription("Print hello, world");
//		problem.setDescription(
//				"<p>Print a line with the following text:</p>\n" +
//				"<blockquote><pre>Hello, world</pre></blockquote>\n"
//		);
//
//		problem.setSkeleton(
//				"#include <stdio.h>\n\n" +
//				"int main(void) {\n" +
//				"\t// TODO - add your code here\n\n" +
//				"\treturn 0;\n" +
//				"}\n"
//				);
//		problem.setSchemaVersion(Problem.CURRENT_SCHEMA_VERSION);
//		problem.setAuthorName("A. User");
//		problem.setAuthorEmail("auser@cs.unseen.edu");
//		problem.setAuthorWebsite("http://cs.unseen.edu/~auser");
//		problem.setTimestampUtc(System.currentTimeMillis());
//		problem.setLicense(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0);
//		
//		DBUtil.storeBean(conn, problem, Problem.SCHEMA, JDBCDatabase.PROBLEMS);
//=======
		CreateSampleData.populateSampleProblem(problem, courseId);
		DBUtil.storeModelObject(conn, problem);
		Integer problemId = problem.getProblemId();
//>>>>>>> 4296caaa5da7c71fc6a038f2b7b12d08b969d1e2
		
		// Add a TestCase
		System.out.println("Creating test case for hello, world problem...");
		TestCase testCase = new TestCase();
		CreateSampleData.populateSampleTestCase(testCase, problemId);

		DBUtil.storeModelObject(conn, testCase);

		conn.close();
		
		System.out.println("Success!");
	}

	private static String ask(Scanner keyboard, String prompt) {
		return ask(keyboard, prompt, null);
	}
	
	private static String ask(Scanner keyboard, String prompt, String defval) {
		System.out.println(prompt);
		System.out.print("[default: " + (defval != null ? defval : "") + "] ==> ");
		String value = keyboard.nextLine();
		if (value.trim().equals("") && defval != null) {
			value = defval;
		}
		return value;
	}
	
	private static<E> void createTable(Connection conn, ModelObjectSchema<E> schema) throws SQLException {
		System.out.println("Creating table " + schema.getDbTableName());
		DBUtil.createTable(conn, schema);
	}

	private static Term storeTerm(Connection conn, String name, int seq) throws SQLException {
		Term term = new Term();
		term.setName(name);
		term.setSeq(seq);

//		DBUtil.storeBean(conn, term, Term.SCHEMA, JDBCDatabase.TERMS);

		DBUtil.storeModelObject(conn, term);

		return term;
	}
}
