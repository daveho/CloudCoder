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
import org.cloudcoder.app.shared.model.TestOutcome;
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
	 * Note that the test process will be limited by the OS to at most
	 * 5 seconds of CPU time; this limit is to avoid a test process
	 * hanging around for a long time by, for example, sleeping or
	 * blocking on I/O.
	 */
	private static final int MAX_TIME_IN_SECONDS = 8;
	
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
		compiler.setCompilerExe("g++"); // C++ is a better C than C
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

		// Create a TestExecutor for each test case, and start it asynchronously.
		// All TestExecutors will work in parallel.
		List<TestCaseExecutor> testCaseExecutors = new ArrayList<CProgramTester.TestCaseExecutor>();
		for (TestCase testCase : submission.getTestCaseList()) {
			TestCaseExecutor executor = new TestCaseExecutor(tempDir, testCase);
			executor.start();
			testCaseExecutors.add(executor);
		}
		
		// Wait for all TestCaseExecutors to finish,
		// collect TestResults
		List<TestResult> testResultList = new ArrayList<TestResult>();
		for (TestCaseExecutor executor : testCaseExecutors) {
			executor.join();
			testResultList.add(executor.getTestResult());
		}
		
		// OK, we have all of our TestResults.
		// Package them up in a SubmissionResult (along with the CompilationResult)
		// and we're done.
		SubmissionResult submissionResult = new SubmissionResult();
		submissionResult.setCompilationResult(compilationResult);
		submissionResult.setTestResults(testResultList.toArray(new TestResult[testResultList.size()]));
		
		return submissionResult;
	}
	
	private static class TestCaseExecutor implements Runnable {
		private static final int MAX_TEST_EXECUTOR_JOIN_ATTEMPTS = 10;

		private File tempDir;
		private TestCase testCase;
		private TestResult testResult;
		private Thread thread;

		public TestCaseExecutor(File tempDir, TestCase testCase) {
			this.tempDir = tempDir;
			this.testCase = testCase;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			// Create a process runner that will read only a limited amount of
			// output.  (We don't want the tested process to generate huge amounts
			// output that could overwhelm the Builder and/or database.)
			ProcessRunner processRunner = CUtil.createProcessRunner();
			
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
				testResult = TestResultUtil.createTestResultForTimeout(processRunner, testCase);
			} else if (!processRunner.isExitStatusKnown()) {
				testResult = TestResultUtil.createTestResultForInternalError(processRunner, testCase);
			} else if (processRunner.isCoreDump()) {
				if (processRunner.getExitCode() == 9 || processRunner.getExitCode() == 24) {
					// Special case: signals 9 (KILL) and 24 (XCPU) indicate that the
					// process exceeded its CPU limit, so treat them as a timeout.
					testResult = TestResultUtil.createTestResultForTimeout(processRunner, testCase);
					
					// The process stderr does not seem to be particularly
					// useful in this case.
					testResult.setStderr("");
				} else {
					// Some other fatal signal (most likely SEGV).
					testResult = TestResultUtil.createTestResultForCoreDump(processRunner, testCase);
				}
			} else {
				// Process completed.  Scan through its output to see if there is a line
				// matching the test case output regular expression.
				boolean foundMatchingOutput = false;
				Pattern pat = Pattern.compile(testCase.getOutput());
				for (String line : processRunner.getStdoutAsList()) {
					Matcher m = pat.matcher(line);
					if (m.matches()) {
						// Match!
						foundMatchingOutput = true;
						break;
					}
				}
				testResult = foundMatchingOutput
						? TestResultUtil.createTestResultForPassedTest(processRunner, testCase)
						: TestResultUtil.createTestResultForFailedAssertion(processRunner, testCase);
			}
		}

		public void start() {
			thread = new Thread(this);
			thread.start();
		}
		
		public void join() {
			boolean done = false;
			int numAttempts = 0;
			while (!done && numAttempts < MAX_TEST_EXECUTOR_JOIN_ATTEMPTS) {
				try {
					thread.join();
					done = true;
				} catch (InterruptedException e) {
					logger.error("test executor interrupted unexpectedly");
					numAttempts++;
				}
			}
			if (!done) {
				logger.error(
						"could not join test executor after {} attempts - giving up",
						MAX_TEST_EXECUTOR_JOIN_ATTEMPTS);
				testResult = new TestResult(TestOutcome.INTERNAL_ERROR, "Test executor failed to complete");
			}
		}

		public TestResult getTestResult() {
			return testResult;
		}
	}
}
