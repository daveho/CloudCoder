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
	private static final boolean KEEP_TEMP_FILES = Boolean.getBoolean("cProgramTester.keepTempFiles");
	
	private static final Logger logger = LoggerFactory.getLogger(CProgramTester.class);
	
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
			if (!KEEP_TEMP_FILES) {
				new DeleteDirectoryRecursively(tempDir).delete();
			}
		}
	}

	private SubmissionResult doTestSubmission(Submission submission, File tempDir) {
		if (tempDir == null) {
			logger.warn("Failed to make temp directory for C program submission");
			return CUtil.createSubmissionResultForUnexpectedBuildError("Builder could not create temporary directory");
		}
		
		Compiler compiler = new Compiler(submission.getProgramText(), tempDir, CTestCaseExecutor.PROGRAM_NAME);
		compiler.setCompilerExe("g++"); // C++ is a better C than C
		if (!compiler.compile()) {
			return CUtil.createSubmissionResultFromFailedCompile(compiler, 0, 0);
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
		List<CTestCaseExecutor> testCaseExecutors = new ArrayList<CTestCaseExecutor>();
		for (TestCase testCase : submission.getTestCaseList()) {
			CTestCaseExecutor executor = createTestExecutor(tempDir, testCase);
			executor.start();
			testCaseExecutors.add(executor);
		}
		
		// Wait for all TestCaseExecutors to finish,
		// collect TestResults
		List<TestResult> testResultList = new ArrayList<TestResult>();
		for (CTestCaseExecutor executor : testCaseExecutors) {
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

	/**
	 * Create a {@link CTestCaseExecutor} to execute given test case.
	 * By default, creates a {@link CRegexTestCaseExecutor}.
	 * Subclasses may override to use a different test case executor.
	 * 
	 * @param tempDir   the temp directory containing the compiled test exe
	 * @param testCase  the test case
	 * @return the CTestCaseExecutor with which to execute the test
	 */
	protected CTestCaseExecutor createTestExecutor(File tempDir, TestCase testCase) {
		return new CRegexTestCaseExecutor(tempDir, testCase);
//		return new CMultiExecRegexTestCaseExecutor(tempDir, testCase, 5);
	}
}
