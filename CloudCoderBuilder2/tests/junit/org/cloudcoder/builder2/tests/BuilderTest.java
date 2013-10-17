package org.cloudcoder.builder2.tests;

import static org.junit.Assert.*;

import org.cloudcoder.app.shared.model.CompilationOutcome;
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
			context = new BuilderTestContext();
			context.setup();
			return true;
		}
		return false;
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
}
