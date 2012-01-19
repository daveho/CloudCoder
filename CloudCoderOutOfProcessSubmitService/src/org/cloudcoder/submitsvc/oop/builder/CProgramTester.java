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

package org.cloudcoder.submitsvc.oop.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tester to build and test complete C programs.
 * 
 * @author David Hovemeyer
 */
public class CProgramTester implements ITester {
	/**
	 * Maximum number of seconds to allow a test case process to run.
	 */
	private static final int MAX_TIME_IN_SECONDS = 5;
	
	/**
	 * Number of milliseconds between polls to see if a test case
	 * process has completed.
	 */
	private static final int POLL_INTERVAL_IN_MILLIS = 500;
	
	private static final Logger logger = LoggerFactory.getLogger(CProgramTester.class);
	private static final String PROGRAM_NAME = "prog";
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.ITester#testSubmission(org.cloudcoder.app.shared.model.Submission)
	 */
	@Override
	public SubmissionResult testSubmission(Submission submission) {
		File tempDir = CUtil.makeTempDir("/tmp");
		try {
			return doTestSubmission(submission, tempDir);
		} finally {
			// Clean up
			new DeleteDirectoryRecursively(tempDir).delete();
		}
	}

	private SubmissionResult doTestSubmission(Submission submission, File tempDir) {
		if (tempDir == null) {
			logger.warn("Failed to make temp directory for C program submission");
			return CUtil.createSubmissionResultForUnexpectedBuildError("Builder could not create temporary directory");
		}
		
		Compiler compiler = new Compiler(submission.getProgramText(), tempDir, PROGRAM_NAME);
		if (!compiler.compile()) {
			return CUtil.createSubmissionResultFromFailedCompile(compiler);
		}
		
		// Gnarly - we have a compiled program.
		CompilationResult compilationResult = new CompilationResult();
		compilationResult.setOutcome(CompilationOutcome.SUCCESS);
		compilationResult.setCompilerDiagnosticList(compiler.getCompilerDiagnosticList());
		
		// Run it for each test case,
		// sending the test case's input as stdin to the process, and scanning
		// the process's stdout for a line matching the test case's output,
		// which is interpreted as a regular expression.
		
		List<TestResult> testResultList = new ArrayList<TestResult>();
		
		// FIXME: consider executing tests in parallel, like CTester
		for (TestCase testCase : submission.getTestCaseList()) {
			executeTestCase(tempDir, testCase, testResultList);
		}
		
		// OK, we have all of our TestResults.
		// Package them up in a SubmissionResult (along with the CompilationResult)
		// and we're done.
		SubmissionResult submissionResult = new SubmissionResult();
		submissionResult.setCompilationResult(compilationResult);
		submissionResult.setTestResults(testResultList.toArray(new TestResult[testResultList.size()]));
		
		return submissionResult;
	}

	private void executeTestCase(File tempDir, TestCase testCase,
			List<TestResult> testResultList) {
		ProcessRunner processRunner = new ProcessRunner();
		
		processRunner.setStdin(testCase.getInput());
		
		// FIXME this is #!@!$! dangerous for many, many reasons
		// - should chroot
		// - should deny access to network
		processRunner.runAsynchronous(tempDir, new String[]{"./" + PROGRAM_NAME});
		
		int elapsed = 0;
		while (processRunner.isRunning() && elapsed < MAX_TIME_IN_SECONDS * 1000) {
			try {
				Thread.sleep(POLL_INTERVAL_IN_MILLIS);
			} catch (InterruptedException e) {
				// can't happen
			}
			
			elapsed += POLL_INTERVAL_IN_MILLIS;
		}
		
		if (processRunner.isRunning()) {
			// timed out!
			processRunner.killProcess();
			testResultList.add(CUtil.createTestResultForTimeout(processRunner));
		} else if (processRunner.getExitCode() == 6) {
			// indicates core dump?
			testResultList.add(CUtil.createTestResultForCoreDump(processRunner));
		} else {
			// Process completed.  Scan through its output to see if there is a line
			// matching the test case output regular expression.
			boolean foundMatchingOutput = false;
			Pattern pat = Pattern.compile(testCase.getOutput());
			for (String line : processRunner.getStdout()) {
				Matcher m = pat.matcher(line);
				if (m.matches()) {
					// Match!
					foundMatchingOutput = true;
					break;
				}
			}
			testResultList.add(foundMatchingOutput
					? CUtil.createTestResultForPassedTest(processRunner)
					: CUtil.createTestResultForFailedAssertion(processRunner, "Test failed for input " + testCase.getInput()));
		}
	}
}
