package org.cloudcoder.analysis.features.java;

public class Submission {

	private int id;
	private int problemId;
	private int testsAttempted;
	private int testsPassed;
	private int userId;
	private String source;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getProblemId() {
		return problemId;
	}
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	public int getTestsAttempted() {
		return testsAttempted;
	}
	public void setTestsAttempted(int testsAttempted) {
		this.testsAttempted = testsAttempted;
	}
	public int getTestsPassed() {
		return testsPassed;
	}
	public void setTestsPassed(int testsPassed) {
		this.testsPassed = testsPassed;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
}
