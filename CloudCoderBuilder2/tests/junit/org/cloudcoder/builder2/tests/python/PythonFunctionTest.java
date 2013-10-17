package org.cloudcoder.builder2.tests.python;

import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.tests.BuilderTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class PythonFunctionTest extends BuilderTest {
	private ProblemAndTestCaseList computeSum;

	@Before
	public void start() {
		if (createContext()) {
			computeSum = getContext().getExercise("compute_sum");
		}
	}
	
	public void finish() {
		destroyContext();
	}
	
	
	@Test
	public void testComputeSum() {
		String source = getContext().getSourceText("compute_sum_pass.py");
		SubmissionResult result = getContext().testSubmission(source, computeSum);
		super.assertAllTestsPassed(result, computeSum);
	}
	
	@AfterClass
	public static void whenDone() {
		BuilderTest.getInstance().destroyContext();
	}
}
