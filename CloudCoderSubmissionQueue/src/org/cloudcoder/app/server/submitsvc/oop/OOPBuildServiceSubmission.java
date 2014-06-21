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

import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * A Submission (Problem, TestCases, and program text)
 * waiting to be tested by a remote Builder.
 *  
 * @author David Hovemeyer
 */
public class OOPBuildServiceSubmission implements IFutureSubmissionResult {
	private Object lock = new Object();
	private Submission submission;
	private boolean ready;
	private SubmissionResult submissionResult;
	private Exception error;
	private int numAttempts;
	
	public OOPBuildServiceSubmission(Submission submission) {
		this.submission = submission;
	}
	
	@Override
	public SubmissionResult waitFor(long timeoutMs) throws SubmissionException, InterruptedException {
		synchronized (lock) {
			if (error != null) {
				throw new SubmissionException("Error testing submission", error);
			}
			while (!ready && timeoutMs > 0L) {
				long start = System.currentTimeMillis();
				lock.wait(timeoutMs);
				long end = System.currentTimeMillis();
				timeoutMs -= (end - start);
			}
			return ready ? submissionResult : null;
		}
	}
	
	public Problem getProblem() {
		synchronized (lock) {
			return submission.getProblem();
		}
	}
	
	public List<TestCase> getTestCaseList() {
		synchronized (lock) {
			return submission.getTestCaseList();
		}
	}
	
	public String getProgramText() {
		synchronized (lock) {
			return submission.getProgramText();
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
