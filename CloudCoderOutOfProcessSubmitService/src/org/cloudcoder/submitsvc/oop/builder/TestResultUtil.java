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

package org.cloudcoder.submitsvc.oop.builder;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * Utility methods for creating TestResults.
 * 
 * @author David Hovemeyer
 */
public class TestResultUtil {
	/**
	 * Create a TestResult for an executed test that timed out.
	 * 
	 * @param p         the test output
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForTimeout(ITestOutput p, TestCase testCase) {
		TestResult testResult = new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
		        "timeout",
		        p.getStdout(),
		        p.getStderr());
		return testResult;
	}

	/**
	 * Create a TestResult for an executed test that passed.
	 * 
	 * @param p         the test output
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForPassedTest(ITestOutput p, TestCase testCase) {
		TestResult testResult = new TestResult(TestOutcome.PASSED,
		        p.getStatusMessage(),
		        p.getStdout(),
		        p.getStderr());
		return testResult;
	}

	/**
	 * Create a TestResult for an executed test that caused a core dump.
	 * 
	 * @param p         the test output
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForCoreDump(ITestOutput p) {
		TestResult testResult = new TestResult(TestOutcome.FAILED_WITH_EXCEPTION,
		        p.getStatusMessage(),
		        p.getStdout(),
		        p.getStderr());
		return testResult;
	}

	/**
	 * Create a TestResult for an executed test that failed due to a failed assertion.
	 * 
	 * @param p         the test output
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForFailedAssertion(ITestOutput p, TestCase testCase) {
		String message = "Test failed for input " + testCase.getInput();
		
		TestResult testResult = new TestResult(TestOutcome.FAILED_ASSERTION,
		        message,
		        p.getStdout(),
		        p.getStderr());
		return testResult;
	}
}
