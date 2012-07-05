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
 * This class represents a {@link Problem} and {@link TestCase}s for the problem,
 * as assigned in a specific {@link Course}.  An instance of ProblemAndTestCaseList
 * can be converted to a {@link ProblemAndTestCaseData}, which is the
 * "exportable" form of a problem and its test cases.
 * 
 * @author David Hovemeyer
 */
public class ProblemAndTestCaseList implements ActivityObject {
	private static final long serialVersionUID = 1L;
	
	private Problem problem;
	private TestCase[] testCaseList;
	
	/**
	 * Constructor.
	 */
	public ProblemAndTestCaseList() {
		
	}
	
	/**
	 * Set the Problem.
	 * @param problem the Problem to set
	 */
	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	
	/**
	 * @return the Problem
	 */
	public Problem getProblem() {
		return problem;
	}
	
	/**
	 * Set the list of TestCases.
	 * @param testCaseList the list of TestCases
	 */
	public void setTestCaseList(TestCase[] testCaseList) {
		this.testCaseList = testCaseList;
	}
	
	/**
	 * @return the list of TestCases
	 */
	public TestCase[] getTestCaseList() {
		return testCaseList;
	}
	
	/**
	 * Convert to {@link ProblemAndTestCaseData}, which is the
	 * "exportable" form of a problem and its test cases.
	 * 
	 * @return the ProblemAndTestCaseData
	 */
	public ProblemAndTestCaseData toProblemAndTestCaseData() {
		ProblemAndTestCaseData result = new ProblemAndTestCaseData();

		result.setProblemData(problem.duplicateProblemData());

		for (TestCase testCase : testCaseList) {
			result.addTestCase(testCase.duplicateTestCaseData());
		}
		
		return result;
	}

	/**
	 * Add a TestCase.
	 * 
	 * @param testCase the TestCase to add
	 */
	public void addTestCase(TestCase testCase) {
		// FIXME: probably we should just use a List to store the TestCases
		TestCase[] larger = new TestCase[testCaseList.length + 1];
		System.arraycopy(testCaseList, 0, larger, 0, testCaseList.length);
		larger[testCaseList.length] = testCase;
		testCaseList = larger;
	}

	/**
	 * Remove a TestCase.
	 * 
	 * @param testCase the TestCase to remove
	 */
	public void removeTestCase(TestCase testCase) {
		// FIXME: probably we should just use a List to store the TestCases
		TestCase[] smaller = new TestCase[testCaseList.length - 1];
		int index = 0;
		for (TestCase tc : testCaseList) {
			if (tc != testCase) {
				smaller[index++] = tc;
			}
		}
		testCaseList = smaller;
	}
}
