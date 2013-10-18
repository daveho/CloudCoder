package org.cloudcoder.builder2.tests.c;

import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.tests.BuilderTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class CProgramTest extends BuilderTest {
	private ProblemAndTestCaseList skip3;
	
	@Before
	public void start() {
		if (createContext()) {
			skip3 = getContext().getExercise("skip3");
		}
	}
	
	@Test
	public void testSkip3GoodSubmission() {
		String source = getContext().getSourceText("skip3_pass.c");
		SubmissionResult result = getContext().testSubmission(source, skip3);
		super.assertAllTestsPassed(result, skip3);
	}
	
	@Test
	public void testSkip3TimeoutInfiniteLoop() {
		String source = getContext().getSourceText("skip3_timeout_infinite_loop.c");
		SubmissionResult result = getContext().testSubmission(source, skip3);
		super.assertAllTestsTimedOut(result, skip3);
	}
	
	@AfterClass
	public static void whenDone() {
		BuilderTest.getInstance().destroyContext();
	}
}
