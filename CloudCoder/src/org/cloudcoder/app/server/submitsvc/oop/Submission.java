package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.util.List;

import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

public class Submission {
	private Object lock = new Object();
	private Problem problem;
	private List<TestCase> testCaseList;
	private String programText;
	private boolean ready;
	private SubmissionResult submissionResult;
	private Exception error;
	private int numAttempts;
	
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
			while (!ready) {
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
		this.submissionResult = result;
	}

	public void setError(Exception e) {
		this.error = e;
	}
	
	/**
	 * Mark this submission as being "ready", meaning that
	 * either testing has completed, or testing could not be
	 * completed due to repeated failures, and we've given up.
	 * Either setSubmissionResult() or setError()
	 * must be called before setting ready to true.
	 */
	public void setReady() {
		synchronized (lock) {
			this.ready = true;
			lock.notifyAll();
		}
	}
	
	/**
	 * @param numAttempts the numAttempts to set
	 */
	public void setNumAttempts(int numAttempts) {
		this.numAttempts = numAttempts;
	}

	/**
	 * @return the numAttempts
	 */
	public int getNumAttempts() {
		return numAttempts;
	}
}
