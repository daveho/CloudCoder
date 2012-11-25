package org.cloudcoder.builder2.ccompiler;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestResult;

public class CUtil {
	public static SubmissionResult createSubmissionResultForUnexpectedBuildError(String message) {
		// FIXME: would be nice to have a more straightforward way to indicate this kind of issue
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
		CompilerDiagnostic compilerDiagnostic = new CompilerDiagnostic(1, 1, 1, 1, message);
		compilationResult.setCompilerDiagnosticList(new CompilerDiagnostic[]{compilerDiagnostic});
		return new SubmissionResult(compilationResult);
	}

	public static SubmissionResult createSubmissionResultFromFailedCompile(Compiler compiler, int prologueLength, int epilogueLength) {
		CompilerDiagnostic[] compilerDiagnosticList = compiler.getCompilerDiagnosticList();
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.FAILURE);
		compilationResult.setCompilerDiagnosticList(compilerDiagnosticList);
		compilationResult.adjustDiagnosticLineNumbers(prologueLength, epilogueLength);
		SubmissionResult submissionResult = new SubmissionResult(compilationResult);
		submissionResult.setTestResults(new TestResult[0]);
		return submissionResult;
	}

}
