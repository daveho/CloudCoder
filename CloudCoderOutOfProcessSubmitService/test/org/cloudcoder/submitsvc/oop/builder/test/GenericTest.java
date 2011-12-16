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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.ITester;

public class GenericTest
{
    protected ITester tester;
    protected List<TestOutcome> testOutcomeList;
    protected Submission submission;
    
    protected void createProblem(String testName, ProblemType problemType) {
        Problem problem=new Problem();
        problem.setTestName(testName);
        problem.setProblemType(problemType);
        problem.setBriefDescription("Brief Description");
        problem.setDescription("Full Description");
        
        submission=new Submission(problem, new LinkedList<TestCase>(), null);

        testOutcomeList=new ArrayList<TestOutcome>();
    }
    
    protected void setProgramText(String programText) {
        submission.setProgramText(programText);
    }

    protected TestCase createTestCase(String testName, String input, String output) {
        TestCase testCase=new TestCase();
        testCase.setTestCaseName(testName);
        testCase.setInput(input);
        testCase.setOutput(output);
        return testCase;
    }
    
    protected void addTestCase(String testName, String input, String output) {
        TestCase testCase=createTestCase(testName, input, output);
        submission.addTestCase(testCase);
    }
    
    protected void addTestCaseAndOutcome(String testName, String input, String output, TestOutcome outcome) {
        addTestCase(testName, input, output);
        testOutcomeList.add(outcome);
    }
    
    protected void runOneTestCase(String testName, String input, String output,
            TestOutcome outcome)
    {
        addTestCase(testName, input, output);
        SubmissionResult result=tester.testSubmission(submission);
        if (!result.isCompiled()) {
            Assert.fail("Code should have compiled");
        }
        TestResult testResult=result.getTestResults()[0];
        
        Assert.assertEquals("Test named "+testResult.getMessage(),
                outcome,
                testResult.getOutcome());
    }
    
    public void runAllTests() {
        SubmissionResult result=tester.testSubmission(submission);
        if (!result.isCompiled()) {
            Assert.fail("Code should have compiled");
        }
        TestResult[] results=result.getTestResults();
        for (int i=0; i<results.length; i++) {
            TestResult testResult=results[i];
            TestOutcome outcome=testOutcomeList.get(i);
            Assert.assertEquals(outcome, testResult.getOutcome());
            
        }
    }
}
