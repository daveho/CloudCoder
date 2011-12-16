// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.util.List;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Thin abstraction layer for interactions with database.
 * 
 * @author David Hovemeyer
 */
public interface IDatabase {
	public ConfigurationSetting getConfigurationSetting(ConfigurationSettingName name);
	public User authenticateUser(String userName, String password);
	public Problem getProblem(User user, int problemId);
	public Change getMostRecentChange(User user, int problemId);
	public Change getMostRecentFullTextChange(User user, int problemId);
	public List<Change> getAllChangesNewerThan(User user, int problemId, int baseRev);
	public List<? extends Object[]> getCoursesForUser(User user);
	public List<Problem> getProblemsInCourse(User user, Course course);
	public void storeChanges(Change[] changeList);
	public List<TestCase> getTestCasesForProblem(int problemId);
	public void insertSubmissionReceipt(SubmissionReceipt receipt, TestResult[] testResultList);
}
