// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.util;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with {@link SubmissionResult}s.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class SubmissionResultUtil {
	private static final Logger logger = LoggerFactory.getLogger(SubmissionResultUtil.class);
	
	public static SubmissionResult createSubmissionResultForUnexpectedBuildError(String message) {
		// FIXME: would be nice to have a more straightforward way to indicate this kind of issue
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
		CompilerDiagnostic compilerDiagnostic = new CompilerDiagnostic(1, 1, 1, 1, message);
		compilationResult.setCompilerDiagnosticList(new CompilerDiagnostic[]{compilerDiagnostic});
		return new SubmissionResult(compilationResult);
	}

	/**
	 * Sanitize a {@link SubmissionResult} to make sure it contains
	 * all required data (and can be saved in the webapp database).
	 * 
	 * @param result the {@link SubmissionResult} to sanitize
	 */
	public static void sanitizeSubmissionResult(SubmissionResult result) {
		if (result.getCompilationResult() == null) {
			logger.warn("Null CompilationResult - should not happen");
			CompilationResult compRes = new CompilationResult(CompilationOutcome.BUILDER_ERROR);
			result.setCompilationResult(compRes);
		}
		
		CompilationResultUtil.sanitizeCompilationResult(result.getCompilationResult());
		
		if (result.getTestResults() == null) {
			logger.warn("Null TestResult - should not happen");
			result.setTestResults(new TestResult[0]);
		} else {
			logger.info("{} results", result.getTestResults().length);
		}
		
		if (ArrayUtil.countNullElements(result.getTestResults()) > 0) {
			logger.warn("Test result array countains {} null values", ArrayUtil.countNullElements(result.getTestResults()));
			TestResult[] fixedTestResults = ArrayUtil.stripNullElements(result.getTestResults());
			result.setTestResults(fixedTestResults);
		}
		
		for (TestResult testResult : result.getTestResults()) {
			TestResultUtil.sanitizeTestResult(testResult);
		}
	}

}
