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

import java.util.List;

/**
 * Interface to be implemented by objects that represent a
 * {@link IProblemData} object and its associated {@link ITestCaseData}
 * objects.  It may be used by code that imports and exports problems
 * and their test cases using a neutral intermediate form.
 * 
 * @author David Hovemeyer
 *
 * @param <ProblemDataType>   the actual {@link IProblemData} subtype contained in the object
 * @param <TestCaseDataType>  the actual {@link ITestCaseData} subtype contained in the object
 */
public interface IProblemAndTestCaseData<
	ProblemDataType extends IProblemData,
	TestCaseDataType extends ITestCaseData> {
	
	/**
	 * Set the problem data.
	 * @param problem the problem data to set
	 */
	public void setProblem(ProblemDataType problem);
	
	/**
	 * Get the problem data.
	 * @return the problem data
	 */
	public ProblemDataType getProblem();
	
	/**
	 * Get a list of test cases.
	 * @return the list of test cases
	 */
	public List<TestCaseDataType> getTestCaseData();
	
	/**
	 * Add a test case.
	 * @param testCase the test case to add
	 */
	public void addTestCase(TestCaseDataType testCaseData);
}
