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
import java.io.IOException;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for C testers.
 * 
 * @author David Hovemeyer
 */
public class CUtil {
	private static final Logger logger = LoggerFactory.getLogger(CUtil.class);
	
	/**
	 * Make a temporary directory.
	 * 
	 * @param baseDir  the parent of the temporary directory to create (e.g., "/tmp")
	 * @return a File representing the newly-created temp directory
	 */
	public static File makeTempDir(String baseDir) {
	    int attempts = 1;
	    File tempDir = null;
	
	    while (tempDir == null && attempts < 10) {
	        try {
	            // start by creating a temporary file
	            File tempFile = File.createTempFile("cmp", "", new File(baseDir));
	
	            // temporary file created successfully - delete it and make an identically-named
	            // directory
	            if (tempFile.delete() && tempFile.mkdir()) {
	                // success!
	                tempDir = tempFile;
	            }
	        } catch (IOException e) {
	            logger.warn("Unable to delete temp file and create directory");
	        }
	        attempts++;
	    }
	    return tempDir;
	}

	public static SubmissionResult createSubmissionResultForUnexpectedBuildError(String message) {
		// FIXME: would be nice to have a more straightforward way to indicate this kind of issue
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
		CompilerDiagnostic compilerDiagnostic = new CompilerDiagnostic(1, 1, 1, 1, message);
		compilationResult.setCompilerDiagnosticList(new CompilerDiagnostic[]{compilerDiagnostic});
		return new SubmissionResult(compilationResult);
	}

	public static SubmissionResult createSubmissionResultFromFailedCompile(Compiler compiler) {
		CompilerDiagnostic[] compilerDiagnosticList = compiler.getCompilerDiagnosticList();
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.FAILURE);
		compilationResult.setCompilerDiagnosticList(compilerDiagnosticList);
		return new SubmissionResult(compilationResult);
	}

	public static String merge(List<String> list){
	    StringBuilder builder=new StringBuilder();
	    for (String s : list) {
	        builder.append(s);
	        builder.append("\n");
	    }
	    return builder.toString();
	}

	public static TestResult createTestResultForTimeout(ProcessRunner p) {
		TestResult testResult = new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
		        "timeout",
		        merge(p.getStdout()),
		        merge(p.getStderr()));
		return testResult;
	}

	public static TestResult createTestResultForPassedTest(ProcessRunner p) {
		TestResult testResult = new TestResult(TestOutcome.PASSED,
		        p.getStatusMessage(),
		        merge(p.getStdout()),
		        merge(p.getStderr()));
		return testResult;
	}

	public static TestResult createTestResultForCoreDump(ProcessRunner p) {
		TestResult testResult = new TestResult(TestOutcome.FAILED_WITH_EXCEPTION,
		        p.getStatusMessage(),
		        merge(p.getStdout()),
		        merge(p.getStderr()));
		return testResult;
	}

	public static TestResult createTestResultForFailedAssertion(ProcessRunner p, String message) {
		TestResult testResult = new TestResult(TestOutcome.FAILED_ASSERTION,
		        message,
		        merge(p.getStdout()),
		        merge(p.getStderr()));
		return testResult;
	}

}
