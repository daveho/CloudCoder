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
import java.util.List;

import junit.framework.Assert;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.ITester;

public class GenericTest
{
    protected static ITester tester;
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

    protected void addTestCase(String input, String output, TestOutcome outcome) {
        TestCase testCase=new TestCase();
        testCase.setId(testCaseNum);
        testCase.setTestCaseName("test"+testCaseNum);
        testCase.setInput(input);
        testCase.setOutput(output);
        testCase.setProblemId(problem.getProblemId());
        testCaseNum+=1;
        
        testCaseList.add(testCase);
        testOutcomeList.add(outcome);
    }
    
    public void runTests(String programText) {
        List<TestResult> results=
                tester.testSubmission(problem, testCaseList, programText);
        for (int i=0; i<results.size(); i++) {
            Assert.assertEquals(results.get(i).getMessage(),
                    testOutcomeList.get(i),
                    results.get(i).getOutcome());
        }
        for (TestResult t : results) {
            System.out.println(t);
        }
    }

}
