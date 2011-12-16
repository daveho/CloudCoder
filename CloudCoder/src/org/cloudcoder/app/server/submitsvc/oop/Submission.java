package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.util.List;

import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

class Submission {
	private Object lock = new Object();
	private Problem problem;
	private List<TestCase> testCaseList;
	private String programText;
	private SubmissionResult submissionResult;
	private IOException error;
	
	public Submission(Problem problem, List<TestCase> testCaseList, String programText) {
		this.problem = problem;
		this.testCaseList = testCaseList;
		this.programText = programText;
	}
	
	public Problem getProblem() {
		synchronized (lock) {
			return problem;
		}
	}
	
	public List<TestCase> getTestCaseList() {
		synchronized (lock) {
			return testCaseList;
		}
	}
	
	public String getProgramText() {
		synchronized (lock) {
			return programText;
		}
	}
	
	public SubmissionResult getSubmissionResult() throws SubmissionException {
		synchronized (lock) {
			while (submissionResult == null && error == null) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					throw new SubmissionException("Interrupted while testing submission", e);
				}
			}
			if (error != null) {
				throw new SubmissionException("Error testing submission", error);
			}
			return submissionResult;
		}
	}
	
	public void setSubmissionResult(SubmissionResult result) {
		synchronized (lock) {
			this.submissionResult = result;
			lock.notifyAll();
		}
	}

	public void setError(IOException e) {
		synchronized (lock) {
			this.error = e;
			lock.notifyAll();
		}
	}
}
