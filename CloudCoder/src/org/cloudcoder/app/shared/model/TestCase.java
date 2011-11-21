package org.cloudcoder.app.shared.model;

import java.io.Serializable;

public class TestCase implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int problemId;
	private String testCaseName;
	private String input;
	private String output;
	
	public TestCase() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	public int getProblemId() {
		return problemId;
	}
	
	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}
	
	public String getTestCaseName() {
		return testCaseName;
	}
	
	public void setInput(String input) {
		this.input = input;
	}
	
	public String getInput() {
		return input;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getOutput() {
		return output;
	}
}
