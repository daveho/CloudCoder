package org.cloudcoder.builder2.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;

public class BuilderTest {
	private static BuilderTest theInstance;
	
	private BuilderTestContext context;

	public boolean createContext() {
		if (context == null) {
			theInstance = this;
			context = new BuilderTestContext(createConfig());
			context.setup();
			return true;
		}
		return false;
	}
	
	protected Properties createConfig() {
		Properties config = new Properties();
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.enable", "true");
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.heapsize", "8388608");
		config.setProperty("cloudcoder.builder2.tmpdir", System.getProperty("java.io.tmpdir"));
		return config;
	}
	
	public BuilderTestContext getContext() {
		return context;
	}
	
	public void destroyContext() {
		if (context != null) {
			context.cleanup();
		}
	}
	
	public static BuilderTest getInstance() {
		return theInstance;
	}

	public void assertSuccessfulCompilation(SubmissionResult result) {
		assertEquals(CompilationOutcome.SUCCESS, result.getCompilationResult().getOutcome());
	}
	
	public void assertAllTestsPassed(SubmissionResult result, ProblemAndTestCaseList exercise) {
		// exercise must have compiled successfully
		assertSuccessfulCompilation(result);
		
		// there must be a test result for each test case
		assertAllTestCasesHaveTestResults(result, exercise);
		
		for (TestResult tr : result.getTestResults()) {
			assertEquals(TestOutcome.PASSED, tr.getOutcome());
		}
	}

	/**
	 * @param result
	 * @param exercise
	 */
	public void assertAllTestCasesHaveTestResults(SubmissionResult result,
			ProblemAndTestCaseList exercise) {
		assertEquals(exercise.getTestCaseData().size(), result.getTestResults().length);
	}

	public void assertCompilerDiagnosticAtLine(SubmissionResult result, int lineNumber) {
		CompilationResult compRes = result.getCompilationResult();
		for (CompilerDiagnostic diag : compRes.getCompilerDiagnosticList()) {
			System.out.println("Diagnostic at line: " + diag.getStartLine());
			if (diag.getStartLine() == (long)lineNumber) {
				return;
			}
		}
		assertTrue("No compiler diagnostic at line " + lineNumber, false);
	}

	public void assertCompilationError(SubmissionResult result) {
		assertEquals(CompilationOutcome.FAILURE, result.getCompilationResult().getOutcome());
	}

	public void assertAllTestsTimedOut(SubmissionResult result, ProblemAndTestCaseList exercise) {
		assertAllTestCasesHaveTestResults(result, exercise);
		for (TestResult tr : result.getTestResults()) {
			assertEquals(TestOutcome.FAILED_FROM_TIMEOUT, tr.getOutcome());
		}
	}
}
