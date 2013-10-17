package org.cloudcoder.builder2.tests.python;

import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.tests.BuilderTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class PythonFunctionTest extends BuilderTest {
	private ProblemAndTestCaseList computeSum;
	private ProblemAndTestCaseList gravity;

	@Before
	public void start() {
		if (createContext()) {
			computeSum = getContext().getExercise("compute_sum");
			gravity = getContext().getExercise("gravity");
		}
	}
	
	@Test
	public void testComputeSum() {
		String source = getContext().getSourceText("compute_sum_pass.py");
		SubmissionResult result = getContext().testSubmission(source, computeSum);
		super.assertAllTestsPassed(result, computeSum);
	}
	
	@Test
	public void testComputeSumWrongFunctionName() {
		String source = getContext().getSourceText("compute_sum_wrong_function_name.py");
		SubmissionResult result = getContext().testSubmission(source, computeSum);
		
		// This should be caught as a "compilation" error
		super.assertCompilationError(result);
	}
	
	@Test
	public void testComputeSumCompilationFailureDueToUnknownImport() {
		String source = getContext().getSourceText("compute_sum_missing_import.py");
		SubmissionResult result = getContext().testSubmission(source, computeSum);
		super.assertCompilationError(result);
	}
	
	@Test
	public void testGravityNameErrorDueToMisspelledParam() {
		String source = getContext().getSourceText("gravity_nameerror_due_to_misspelled_param.py");
		SubmissionResult result = getContext().testSubmission(source, gravity);
		super.assertCompilerDiagnosticAtLine(result, 8);
	}
	
	@AfterClass
	public static void whenDone() {
		BuilderTest.getInstance().destroyContext();
	}
}
