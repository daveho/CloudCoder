package org.cloudcoder.app.shared.model;

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
}
