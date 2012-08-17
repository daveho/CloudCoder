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
import java.sql.SQLException;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCase;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;

/**
 * Create sample data.
 * 
 * @author David Hovemeyer
 */
public class CreateSampleData {

	/**
	 * Create an initial user.
	 * 
	 * @param conn        database connection
	 * @param ccUserName  user name
	 * @param ccPassword  password (plaintext)
	 * @return the user id of the newly-created user
	 * @throws SQLException
	 */
	public static int createInitialUser(Connection conn, String ccUserName, String ccPassword) throws SQLException {
		User user = new User();
		user.setUsername(ccUserName);
		user.setPasswordHash(BCrypt.hashpw(ccPassword, BCrypt.gensalt(12)));
		DBUtil.storeModelObject(conn, user);
		return user.getId();
	}

	/**
	 * Create a demo course.
	 * 
	 * @param conn  the database connection
	 * @param term  the term in which the course should be created
	 * @return the course id of the newly-created course
	 * @throws SQLException
	 */
	public static int createDemoCourse(Connection conn, Term term) throws SQLException {
		Course course = new Course();
		course.setName("CCDemo");
		course.setTitle("CloudCoder demo course");
		course.setTermId(term.getId());
		course.setTerm(term);
		course.setYear(2012);
		course.setUrl("http://cloudcoder.org/");
		DBUtil.storeModelObject(conn, course);
		return course.getId();
	}

	/**
	 * Register a user for a course.
	 * 
	 * @param conn              the database connection
	 * @param userId            the user id
	 * @param courseId          the course id
	 * @param registrationType  the registration type
	 * @throws SQLException
	 */
	public static void registerUser(Connection conn, int userId, int courseId, CourseRegistrationType registrationType) throws SQLException {
		CourseRegistration courseReg = new CourseRegistration();
		courseReg.setCourseId(courseId);
		courseReg.setUserId(userId);
		courseReg.setRegistrationType(registrationType);
		courseReg.setSection(101);
		DBUtil.storeModelObject(conn, courseReg);
	}

	static void populateSampleTestCase(ITestCase testCase, Integer problemId) {
		testCase.setProblemId(problemId);
		CreateSampleData.populateSampleTestCaseData(testCase);
	}

	static void populateSampleTestCaseData(ITestCaseData testCase) {
		testCase.setTestCaseName("hello");
		testCase.setInput("");
		testCase.setOutput("^\\s*Hello\\s*,\\s*world\\s*$i");
		testCase.setSecret(false);
	}

	static void populateSampleProblem(IProblem problem, int courseId) {
		problem.setCourseId(courseId);
		problem.setWhenAssigned(System.currentTimeMillis());
		problem.setWhenDue(problem.getWhenAssigned() + (24L*60*60*1000));
		problem.setVisible(true);
		CreateSampleData.populateSampleProblemData(problem);
	}

	static void populateSampleProblemData(IProblemData problemData) {
		problemData.setProblemType(ProblemType.C_PROGRAM);
		problemData.setTestname("hello");
		problemData.setBriefDescription("Print hello, world");
		problemData.setDescription(
				"<p>Print a line with the following text:</p>\n" +
				"<blockquote><pre>Hello, world</pre></blockquote>\n"
		);
	
		problemData.setSkeleton(
				"#include <stdio.h>\n\n" +
				"int main(void) {\n" +
				"\t// TODO - add your code here\n\n" +
				"\treturn 0;\n" +
				"}\n"
				);
		problemData.setSchemaVersion(Problem.CURRENT_SCHEMA_VERSION);
		problemData.setAuthorName("A. User");
		problemData.setAuthorEmail("auser@cs.unseen.edu");
		problemData.setAuthorWebsite("http://cs.unseen.edu/~auser");
		problemData.setTimestampUtc(System.currentTimeMillis());
		problemData.setLicense(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0);
	}

}
