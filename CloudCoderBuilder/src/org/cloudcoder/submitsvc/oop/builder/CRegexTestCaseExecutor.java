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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * Subclass of {@link CTestCaseExecutor} that checks the result
 * of the exited test process by matching output lines against
 * a regular expression.
 * 
 * @author David Hovemeyer
 */
public class CRegexTestCaseExecutor extends CTestCaseExecutor {
	
	private static final Pattern REGEX_OPTIONS = Pattern.compile("\\$([ij]+)$");

	/**
	 * Constructor.
	 * 
	 * @param tempDir    directory in which the test executable will run
	 * @param testCase   the {@link TestCase} to use as test input/expected output
	 */
	public CRegexTestCaseExecutor(File tempDir, TestCase testCase) {
		super(tempDir, testCase);
	}

	@Override
	protected TestResult createTestResult(ProcessRunner processRunner) {
		// Special case: if the stdout is completely empty, it is possible
		// that the expected output of the program is a blank line, and
		// thus empty output is correct.  So, if the stdout is empty,
		// change it to a single empty line.  (These are the kinds of
		// things you learn when you have actual students submitting code.)
		List<String> stdoutAsList = processRunner.getStdoutAsList();
		if (stdoutAsList.isEmpty()) {
			stdoutAsList = new ArrayList<String>();
			stdoutAsList.add("");
		}
		
		// Handle regex options.
		boolean caseInsensitive = false;
		boolean joinOutputLines = false;
		String regex = getTestCase().getOutput();
		Matcher optionsMatcher = REGEX_OPTIONS.matcher(regex);
		if (optionsMatcher.find()) {
			String options = optionsMatcher.group(1);
			if (options.contains("i")) {
				caseInsensitive = true;
			}
			if (options.contains("j")) {
				joinOutputLines = true;
			}
			regex = regex.substring(0, optionsMatcher.start());
		}
		
		// If the "j" regex option was specified, join all of the
		// output lines into a single line (with a single space
		// separating each original line).
		if (joinOutputLines) {
			String oneLine = CUtil.mergeOneLine(stdoutAsList);
			stdoutAsList = new ArrayList<String>();
			stdoutAsList.add(oneLine);
		}
		
		// Scan through its output to see if there is a line
		// matching the test case output regular expression.
		boolean foundMatchingOutput = false;
		Pattern pat = Pattern.compile(regex, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
		for (String line : stdoutAsList) {
			Matcher m = pat.matcher(line);
			if (m.matches()) {
				// Match!
				foundMatchingOutput = true;
				break;
			}
		}
		
		return foundMatchingOutput
				? TestResultUtil.createTestResultForPassedTest(processRunner, getTestCase())
				: TestResultUtil.createTestResultForFailedAssertion(processRunner, getTestCase());
	}
}
