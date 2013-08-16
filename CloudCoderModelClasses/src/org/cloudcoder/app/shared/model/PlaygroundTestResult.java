package org.cloudcoder.app.shared.model;

import java.util.List;

public class PlaygroundTestResult
{
    private Integer testNumber=1;
    private String input="";
    private String stdout="";
    private String stderr="";
    
    public PlaygroundTestResult() {}
    
    public PlaygroundTestResult(TestResult testResult) {
        //TODO parse out exceptions using TestResult
        // and its exception class
        this.testNumber=testResult.getId();
        this.input=testResult.getInput();
        this.stdout=testResult.getStdout();
        this.stderr=testResult.getStderr();
    }
    
    public Integer getTestNumber() {
        return testNumber;
    }
    public void setTestNumber(Integer testNumber) {
        this.testNumber = testNumber;
    }
    public String getInput() {
        return input;
    }
    public void setInput(String input) {
        this.input = input;
    }
    public String getStdout() {
        return stdout;
    }
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
    public String getStderr() {
        return stderr;
    }
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public static PlaygroundTestResult[] convertTestResult(TestResult[] testResults) {
        PlaygroundTestResult[] playgroundTestResults=new PlaygroundTestResult[testResults.length];
        for (int i=0; i<testResults.length; i++) {
            playgroundTestResults[i]=new PlaygroundTestResult(testResults[i]);
        }
        return playgroundTestResults;
    }

    public static PlaygroundTestResult[] convertTestCase(TestCase[] testCases) {
        PlaygroundTestResult[] playgroundTestResults=new PlaygroundTestResult[testCases.length];
        for (int i=0; i<testCases.length; i++) {
            PlaygroundTestResult r=new PlaygroundTestResult();
            TestCase t=testCases[i];
            r.setTestNumber(t.getTestCaseId());
            r.setInput(t.getInput());
            r.setStdout("");
            r.setStderr("");
            playgroundTestResults[i]=r;
        }
        return playgroundTestResults;
    }
    
    public static PlaygroundTestResult[] convertTestCase(List<TestCase> testCaseList) {
        PlaygroundTestResult[] playgroundTestResults=new PlaygroundTestResult[testCaseList.size()];
        for (int i=0; i<testCaseList.size(); i++) {
            PlaygroundTestResult r=new PlaygroundTestResult();
            TestCase t=testCaseList.get(i);
            r.setTestNumber(t.getTestCaseId());
            r.setInput(t.getInput());
            r.setStdout("");
            r.setStderr("");
            playgroundTestResults[i]=r;
        }
        return playgroundTestResults;
    }
}
