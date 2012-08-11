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

/**
 * Interface defining the names of the database tables
 * used by {@link JDBCDatabase}.
 * 
 * @author David Hovemeyer
 */
public interface JDBCTableNames {

	// Constants for table names
	public static final String TEST_RESULTS = "cc_test_results";
	public static final String TEST_CASES = "cc_test_cases";
	public static final String SUBMISSION_RECEIPTS = "cc_submission_receipts";
	public static final String TERMS = "cc_terms";
	public static final String EVENTS = "cc_events";
	public static final String CHANGES = "cc_changes";
	public static final String COURSE_REGISTRATIONS = "cc_course_registrations";
	public static final String COURSES = "cc_courses";
	public static final String PROBLEMS = "cc_problems";
	public static final String USERS = "cc_users";
	public static final String CONFIGURATION_SETTINGS = "cc_configuration_settings";
	public static final String REPO_PROBLEMS = "cc_repo_problems";
	public static final String REPO_TEST_CASES = "cc_repo_test_cases";

}