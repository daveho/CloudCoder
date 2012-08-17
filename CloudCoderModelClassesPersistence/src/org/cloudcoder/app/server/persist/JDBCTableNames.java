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

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoTestCase;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Interface defining the names of the database tables
 * used by {@link JDBCDatabase}.
 * 
 * <em>Deprecated:</em> Database table names should be retrieved using {@link ModelObjectSchema#getDbTableName()}.
 * This interface is here as a placeholder.
 * 
 * @author David Hovemeyer
 */
@Deprecated
public interface JDBCTableNames {

	// Constants for table names
	public static final String TEST_RESULTS = TestResult.SCHEMA.getDbTableName();
	public static final String TEST_CASES = TestCase.SCHEMA.getDbTableName();
	public static final String SUBMISSION_RECEIPTS = SubmissionReceipt.SCHEMA.getDbTableName();
	public static final String TERMS = Term.SCHEMA.getDbTableName();
	public static final String EVENTS = Event.SCHEMA.getDbTableName();
	public static final String CHANGES = Change.SCHEMA.getDbTableName();
	public static final String COURSE_REGISTRATIONS = CourseRegistration.SCHEMA.getDbTableName();
	public static final String COURSES = Course.SCHEMA.getDbTableName();
	public static final String PROBLEMS = Problem.SCHEMA.getDbTableName();
	public static final String USERS = User.SCHEMA.getDbTableName();
	public static final String CONFIGURATION_SETTINGS = ConfigurationSetting.SCHEMA.getDbTableName();
	public static final String REPO_PROBLEMS = RepoProblem.SCHEMA.getDbTableName();
	public static final String REPO_TEST_CASES = RepoTestCase.SCHEMA.getDbTableName();

}