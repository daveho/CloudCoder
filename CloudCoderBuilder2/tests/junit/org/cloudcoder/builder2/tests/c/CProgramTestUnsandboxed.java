package org.cloudcoder.builder2.tests.c;

import java.util.Properties;

import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.tests.BuilderTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

// C_PROGRAM tests that are executed without sandboxing.
// We need this specifically to test the case that a test process
// fails by taking too long to execute without reaching the CPU time limit.
// This is generally only possible by sleeping or doing another
// blocking operation, which isn't actually possible when using
// SECCOMP.
public class CProgramTestUnsandboxed extends BuilderTest {
	private ProblemAndTestCaseList skip3;
	
	@Override
	protected Properties createConfig() {
		// Disable sandboxing!
		Properties config = super.createConfig();
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.enable", "false");
		return config;
	}
	
	@Before
	public void start() {
		if (createContext()) {
			skip3 = getContext().getExercise("skip3");
		}
	}
	
	@Test
	public void testSkip3TimeoutWithoutReachingCPULimit() {
		String source = getContext().getSourceText("skip3_timeout_by_sleeping.c");
		SubmissionResult result = getContext().testSubmission(source, skip3);
		super.assertAllTestsTimedOut(result, skip3);
	}
	
	@AfterClass
	public static void whenDone() {
		BuilderTest.getInstance().destroyContext();
	}

}
