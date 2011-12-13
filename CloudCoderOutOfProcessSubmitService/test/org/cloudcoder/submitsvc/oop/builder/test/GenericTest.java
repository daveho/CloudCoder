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
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.ITester;

public class GenericTest
{
    protected ITester tester;
    protected String programText;
    
    protected Problem problem;
    protected List<TestCase> testCaseList;
    protected List<TestOutcome> testOutcomeList;
    protected int testCaseNum;
    
    protected Problem createGenericProblem() {
        Problem problem=new Problem();
        problem.setProblemId(1);
        problem.setTestName("METHOD NAME GOES HERE");
        problem.setCourseId(1);
        problem.setBriefDescription("Brief Description");
        problem.setDescription("Full Description");
        return problem;
    }
    
    protected void before() {
        testCaseList=new ArrayList<TestCase>();
        testOutcomeList=new ArrayList<TestOutcome>();
        // re-set test case number back to 1
        testCaseNum=1;
    }

    protected TestCase createTestCase(String testName, String input, String output) {
        TestCase testCase=new TestCase();
        testCase.setId(testCaseNum);
        testCase.setTestCaseName(testName);
        testCase.setInput(input);
        testCase.setOutput(output);
        testCase.setProblemId(problem.getProblemId());
        return testCase;
    }
    
    protected void addTestCase(String testName, String input, String output, TestOutcome outcome) {
        TestCase testCase=createTestCase(testName, input, output);
        
        testCaseNum+=1;
        
        testCaseList.add(testCase);
        testOutcomeList.add(outcome);
    }
    
    public void runAllTests() {
        List<TestResult> results=
                tester.testSubmission(problem, testCaseList, programText);
        for (int i=0; i<results.size(); i++) {
            Assert.assertEquals("Test number "+i+" named "+results.get(i).getMessage(),
                    testOutcomeList.get(i),
                    results.get(i).getOutcome());
        }
    }

    public void runOneTest(int testNum) {
        TestResult res=testOneSubmission(problem, testCaseList.get(testNum), programText);
        Assert.assertEquals(testOutcomeList.get(testNum),
                res.getOutcome());
    }
    
    public void runOneTest(String testName) {
        TestCase test=null;
        TestOutcome outcome=null;
        for (int i=0; i<testCaseList.size(); i++){
            TestCase t=testCaseList.get(i);
            if (t.getTestCaseName().equals(testName)) {
                test=t;
                outcome=testOutcomeList.get(i);
                break;
            }
        }
        TestResult res=testOneSubmission(problem, test, programText);
        Assert.assertEquals(outcome,
                res.getOutcome());
    }
    
    protected TestResult testOneSubmission(Problem problem, TestCase testCase, String programText) {
        if (testCaseList==null) {
            testCaseList=new LinkedList<TestCase>();
        }
        testCaseList.clear();
        testCaseList.add(testCase);
        List<TestResult> results=tester.testSubmission(problem, testCaseList, programText);
        return results.get(0);
    }

}
