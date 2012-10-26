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

import java.io.File;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * Variant of {@link CRegexTestCaseExecutor} which executes the test
 * case multiple times.  The test is only considered to be passed if all
 * executions are passing.  This attempts to handle the case where a bug
 * in the submission makes the executable behave nondeterministically,
 * and may succeed on some runs but fail on others.
 * 
 * @author David Hovemeyer
 */
public class CMultiExecRegexTestCaseExecutor extends CRegexTestCaseExecutor {
	private int numTimes;
	
	/**
	 * Constructor.
	 * 
	 * @param tempDir   the temp directory containing the test executable
	 * @param testCase  the test case
	 * @param numTimes  the maximum number of times to execute the test case
	 */
	public CMultiExecRegexTestCaseExecutor(File tempDir, TestCase testCase, int numTimes) {
		super(tempDir, testCase);
		this.numTimes = numTimes;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.CTestCaseExecutor#run()
	 */
	@Override
	public void run() {
		for (int i = 0; i < numTimes; i++) {
			super.run();
			TestResult result = getTestResult();
			if (result.getOutcome() != TestOutcome.PASSED) {
				break;
			}
		}
	}
}
