// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.submitsvc.oop;

import java.util.List;

import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * A Submission (Problem, TestCases, and program text)
 * waiting to be tested by a remote Builder.
 *  
 * @author David Hovemeyer
 */
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
