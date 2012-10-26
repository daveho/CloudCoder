// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * Tester to compile and run complete Java programs
 * (consisting of a top level class with a main method).
 * Output is judged for correctness by testing each line
 * against a regexp; the same technique as {@link CProgramTester}.
 * 
 * @author David Hovemeyer
 */
public class JavaProgramTester implements ITester {

	@Override
	public SubmissionResult testSubmission(Submission submission) {
		// Create a temp directory where we can write the class files
		File tempDir = CUtil.makeTempDir();
		System.out.println("workDir is " + tempDir.getPath());

		try {
			// Attempt to compile and test the submission
			return doTestSubmission(submission, tempDir);
		} finally {
			new DeleteDirectoryRecursively(tempDir).delete();
		}
	}

	protected SubmissionResult doTestSubmission(Submission submission, File workDir) {
		//Problem problem = submission.getProblem();
		
		String programText = submission.getProgramText();

		// Determine the package name and top-level class name
		FindJavaPackageAndClassNames packageAndClassNames = new FindJavaPackageAndClassNames();
		packageAndClassNames.determinePackageAndClassNames(programText);
		if (packageAndClassNames.getClassName() == null) {
			return CUtil.createSubmissionResultForUnexpectedBuildError("Could not determine top-level class name");
		}

		// Attempt to compile the program
		InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();
		compiler.addClassFile(packageAndClassNames.getFullyQualifiedClassName(), programText);
		if (!compiler.compileWithoutLoadingClasses()) {
			return new SubmissionResult(compiler.getCompileResult());
		}
		
		// Write the class files
		Map<String, byte[]> compiledClasses = compiler.getFileManager().getClasses();
		for (Map.Entry<String, byte[]> entry : compiledClasses.entrySet()) {
			String clsName = entry.getKey();
			byte[] bytes = entry.getValue();
			
			String fileName = clsName.replace('.', '/') + ".class";
			File out = new File(workDir, fileName);
			out.getParentFile().mkdirs();
			
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(out);
				IOUtils.copy(new ByteArrayInputStream(bytes), os);
			} catch (IOException e) {
				return CUtil.createSubmissionResultForUnexpectedBuildError("Error writing Java class file: " + e.getMessage());
			} finally {
				IOUtils.closeQuietly(os);
			}
		}

		// Execute the test cases
		List<JavaRegexTestCaseExecutor> executorList = new ArrayList<JavaRegexTestCaseExecutor>();
		for (TestCase testCase : submission.getTestCaseList()) {
			JavaRegexTestCaseExecutor executor =
					new JavaRegexTestCaseExecutor(workDir, testCase, packageAndClassNames.getFullyQualifiedClassName());
			executorList.add(executor);
			
			executor.start();
		}
		
		// Wait for test case executors to complete and accumulate TestResults
		List<TestResult> testResultList = new ArrayList<TestResult>();
		for (JavaRegexTestCaseExecutor executor : executorList) {
			executor.join();
			testResultList.add(executor.getTestResult());
		}
		
		// Create SubmissionResult
		SubmissionResult submissionResult = new SubmissionResult();
		submissionResult.setCompilationResult(compiler.getCompileResult());
		submissionResult.setTestResults(testResultList.toArray(new TestResult[testResultList.size()]));
		
		return submissionResult;
	}
}
