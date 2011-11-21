package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.util.List;

import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestResult;

public class Submission {
	private Object lock = new Object();
	private Problem problem;
	private String programText;
	private List<TestResult> testResultList;
	private IOException error;
	
	public Submission(Problem problem, String programText) {
		this.problem = problem;
		this.programText = programText;
	}
	
	public Problem getProblem() {
		synchronized (lock) {
			return problem;
		}
	}
	
	public String getProgramText() {
		synchronized (lock) {
			return programText;
		}
	}
	
	public List<TestResult> getTestResultList() throws SubmissionException {
		synchronized (lock) {
			while (testResultList == null && error == null) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					throw new SubmissionException("Interrupted while testing submission", e);
				}
			}
			if (error != null) {
				throw new SubmissionException("Error testing submission", error);
			}
			return testResultList;
		}
	}
	
	public void setTestResultList(List<TestResult> testResultList) {
		synchronized (lock) {
			this.testResultList = testResultList;
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
