// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.submitsvc.oop.builder.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.CProgramTester;
import org.cloudcoder.submitsvc.oop.builder.ITester;
import org.junit.Test;

/**
 * Tests for CProgramTester.
 */
public class TestCProgramTester {
	private static final String GOOD_HELLO_WORLD =
			"#include <stdio.h>\n" +
			"\n" +
			"int main(void){\n" +
			"  printf(\"  Hello,   world   \\n\");\n" +
			"  return 0;\n" +
			"}\n" +
			"";

	private Problem createProblem(String desc, String testName) {
		Problem problem = new Problem();
		problem.setBriefDescription(desc);
		problem.setDescription(desc);
		problem.setCourseId(1);
		problem.setProblemId(1);
		problem.setProblemType(ProblemType.C_PROGRAM);
		problem.setTestname(testName);
		long now = System.currentTimeMillis();
		problem.setWhenAssigned(now);
		problem.setWhenDue(now+1L);
		return problem;
	}

	private TestCase createTestCase(String input, String output, String testCaseName) {
		TestCase testCase = new TestCase();
		testCase.setId(1);
		testCase.setInput(input);
		testCase.setOutput(output);
		testCase.setProblemId(1);
		testCase.setSecret(false);
		testCase.setTestCaseName(testCaseName);
		return testCase;
	}
	
	@Test
	public void testHelloWorld() {
		Problem helloWorldProblem = createProblem("Print hello world", "hello");
		
		String input = "";
		String output = "^\\s*Hello,\\s+world(\\s.*)?$i";
		System.out.println(output);
		String testCaseName = "testHello";
		List<TestCase> testCaseList = Arrays.asList(new TestCase[]{
				createTestCase(input, output, testCaseName)
		});
		
		ITester cProgramTester = new CProgramTester();
		
		//System.out.println(GOOD_HELLO_WORLD);
		Submission submission = new Submission(helloWorldProblem, testCaseList, GOOD_HELLO_WORLD);
		
		SubmissionResult submissionResult = cProgramTester.testSubmission(submission);
		assertEquals(CompilationOutcome.SUCCESS, submissionResult.getCompilationResult().getOutcome());

		TestResult[] testResultList = submissionResult.getTestResults();
		assertEquals(TestOutcome.PASSED, testResultList[0].getOutcome());
	}
}
