// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;

/**
 * Database transaction to import all {@link Problem}s from an
 * existing course, adding them to given destination course.
 * 
 * @author David Hovemeyer
 */
public class ImportAllProblemsFromCourse extends AbstractDatabaseRunnableNoAuthException<OperationResult> {
	private Course source;
	private Course dest;
	private User instructor;

	/**
	 * Constructor.
	 * 
	 * @param source the course to import {@link Problem}s from
	 * @param dest   the course to add the {@link Problem}s to
	 * @param instructor a {@link User} that is an instructor in both courses
	 */
	public ImportAllProblemsFromCourse(Course source, Course dest, User instructor) {
		this.source = source;
		this.dest = dest;
		this.instructor = instructor;
	}

	@Override
	public OperationResult run(Connection conn) throws SQLException {
		List<Problem> problemList = Queries.doGetProblemsInCourse(instructor, source, conn, this);
		for (Problem problem : problemList) {
			List<TestCase> testCaseList = Queries.doGetTestCasesForProblem(conn, problem.getProblemId(), this);
			
			// Reset problem id and course id (since we will be inserting
			// a new copy of this problem in the destination course)
			problem.setProblemId(0);
			problem.setCourseId(dest.getId());
			
			// Set visisbility to false (on the assumption that the instructor will
			// want to explicitly make problems visible)
			problem.setVisible(false);
			
			// Insert problem
			Queries.doInsertProblem(problem, conn, this);
			
			// Reset test case id and problem id of each test case
			for (TestCase testCase : testCaseList) {
				testCase.setTestCaseId(0);
				testCase.setProblemId(problem.getProblemId());
			}
			
			// Insert test cases
			Queries.doInsertTestCases(problem, testCaseList, conn, this);
		}
		
		return new OperationResult(true, "Successfully imported exercises");
	}

	@Override
	public String getDescription() {
		return " import all exercises from course";
	}
}
