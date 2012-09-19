// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012 Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012 David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute a C test case.
 * This is an abstract base class: subclasses must implement the
 * {@link #createTestResult(ProcessRunner)} method to check the
 * exited test process to determine the {@link TestResult}..
 * 
 * @author David Hovemeyer
 */
public abstract class CTestCaseExecutor implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(CTestCaseExecutor.class);
	
	private static final int MAX_TEST_EXECUTOR_JOIN_ATTEMPTS = 10;

	private File tempDir;
	private TestCase testCase;
	private TestResult testResult;
	private Thread thread;
	
	protected List<String> arguments=new LinkedList<String>();

	public static final String PROGRAM_NAME = "prog";
	
	public void addArgument(String arg) {
	    arguments.add(arg);
	}

	/**
	 * Number of milliseconds between polls to see if a test case
	 * process has completed.
	 */
	public static final int POLL_INTERVAL_IN_MILLIS = 500;

	/**
	 * Maximum number of seconds to allow a test case process to run.
	 * Note that the test process will be limited by the OS to at most
	 * 5 seconds of CPU time; this limit is to avoid a test process
	 * hanging around for a long time by, for example, sleeping or
	 * blocking on I/O.
	 */
	public static final int MAX_TIME_IN_SECONDS = 8;

	/**
	 * Constructor.
	 * 
	 * @param tempDir    directory in which the test executable will run
	 * @param testCase   the {@link TestCase} to use as test input/expected output
	 */
	public CTestCaseExecutor(File tempDir, TestCase testCase) {
	    arguments.add("./"+PROGRAM_NAME);
		this.tempDir = tempDir;
		this.testCase = testCase;
	}
	
	/**
	 * @return the testCase
	 */
	public TestCase getTestCase() {
		return testCase;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Create a process runner that will read only a limited amount of
		// output.  (We don't want the tested process to generate huge amounts
		// output that could overwhelm the Builder and/or database.)
		ProcessRunner processRunner = createProcessRunner();
		
		processRunner.setStdin(testCase.getInput());
		
		// FIXME this is #!@!$! dangerous for many, many reasons
		// - should chroot
		// - should deny access to network
		String[] cmd=arguments.toArray(new String[arguments.size()]);
		processRunner.runAsynchronous(tempDir, cmd);
        //processRunner.runAsynchronous(tempDir, new String[]{"./" + CTestCaseExecutor.PROGRAM_NAME});            
		
		int elapsed = 0;
		while (processRunner.isRunning() && elapsed < CTestCaseExecutor.MAX_TIME_IN_SECONDS * 1000) {
			try {
				Thread.sleep(CTestCaseExecutor.POLL_INTERVAL_IN_MILLIS);
			} catch (InterruptedException e) {
				// can't happen
			}
			
			elapsed += CTestCaseExecutor.POLL_INTERVAL_IN_MILLIS;
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
			// Process completed.
			this.testResult = createTestResult(processRunner);
		}
	}

	/**
	 * Create a {@link ProcessRunner} for executing the test program.
	 * Subclasses may override this to customize the ProcessRunner.
	 * 
	 * @return a {@link ProcessRunner} to use to execute the test program
	 */
	protected ProcessRunner createProcessRunner() {
		return CUtil.createProcessRunner();
	}

	/**
	 * Check the output and/or exit code of the process to determine
	 * whether or not the test succeeded.
	 * 
	 * @param processRunner the ProcessRunner of the exited test process
	 * @return a TestResult
	 */
	protected abstract TestResult createTestResult(ProcessRunner processRunner);

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