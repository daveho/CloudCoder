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

package org.cloudcoder.app.shared.model;

/**
 * Interface to be implemented by objects representing a test case
 * associated with a specific problem.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public interface ITestCase extends ITestCaseData {

	/**
	 * Set the test case id.
	 * @param id the test case id
	 */
	public abstract void setTestCaseId(int id);

	/**
	 * Get the test case id.
	 * @return the test case id
	 */
	public abstract int getTestCaseId();

	/**
	 * Set the problem id.
	 * @param problemId the problem id to set
	 */
	public abstract void setProblemId(int problemId);

	/**
	 * Get the problem id.
	 * @return the problem id
	 */
	public abstract int getProblemId();

}