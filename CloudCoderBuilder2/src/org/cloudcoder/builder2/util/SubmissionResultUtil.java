package org.cloudcoder.builder2.util;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.SubmissionResult;

public class SubmissionResultUtil {
	public static SubmissionResult createSubmissionResultForUnexpectedBuildError(String message) {
		// FIXME: would be nice to have a more straightforward way to indicate this kind of issue
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
		CompilerDiagnostic compilerDiagnostic = new CompilerDiagnostic(1, 1, 1, 1, message);
		compilationResult.setCompilerDiagnosticList(new CompilerDiagnostic[]{compilerDiagnostic});
		return new SubmissionResult(compilationResult);
	}

}
