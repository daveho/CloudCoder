package org.cloudcoder.submitsvc.oop.builder;

import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

public interface ITester
{
    public List<TestResult> testSubmission(Problem problem, 
            List<TestCase> testCaseList, 
            String programText);
    
    public TestResult testOneSubmission(Problem problem, 
            TestCase testCase, 
            String programText);
}