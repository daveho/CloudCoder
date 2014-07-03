// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionResultAnnotation;
import org.cloudcoder.app.shared.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker task to communicate with a remote Builder process.
 * 
 * @author David Hovemeyer
 */
public class WorkerTask implements Runnable {
	/**
	 * Number of milliseconds between attempts to poll the
	 * submission queue.
	 */
	private static final long POLL_INTERVAL_MILLIS = 1000L;

	/**
	 * Maximum number of milliseconds that the task is allowed to
	 * be idle (no submissions available) before a keepalive signal
	 * is sent to the builder.
	 */
	private static final long MAX_IDLE_TIME_MILLIS = 5000L;

	private static Logger logger = LoggerFactory.getLogger(WorkerTask.class);
	
	private volatile boolean shutdownRequested;
	private Socket clientSocket;
	private LinkedBlockingQueue<OOPBuildServiceSubmission> submissionQueue;
	private WorkerTaskSet workerTaskSet;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public WorkerTask(Socket clientSocket, LinkedBlockingQueue<OOPBuildServiceSubmission> submissionQueue, WorkerTaskSet workerTaskSet) throws IOException {
		this.shutdownRequested = false;
		this.clientSocket = clientSocket;
		this.submissionQueue = submissionQueue;
		this.workerTaskSet = workerTaskSet;
		
		this.out = new ObjectOutputStream(clientSocket.getOutputStream());
		this.in = new ObjectInputStream(clientSocket.getInputStream());
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	@Override
	public void run() {
		logger.info("oop buildsvc WorkerTask starting");
		
		OOPBuildServiceSubmission submission = null;
		
		// Keep track of how long it has been since we sent
		// the Builder a Submission.  If it's been too long,
		// we will send a keepalive signal to avoid the TCP
		// connection timing out.
		long idleTimeMillis = 0;
		
		// Testing of submissions by this worker continues until either an
		// explicit shutdown request is made, or an exception is thrown communicating
		// with the remote Builder process.
	submissionTestingLoop:
		while (!shutdownRequested) {

			// Try to get a submission to test
			try {
				submission = submissionQueue.poll(POLL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				if (!shutdownRequested) {
					logger.error("Unexpected interruption", e);
					break submissionTestingLoop;
				}
			}
			
			if (submission == null) {
				idleTimeMillis += POLL_INTERVAL_MILLIS;
				
				if (idleTimeMillis >= MAX_IDLE_TIME_MILLIS) {
					// Send a negative problem id as a keepalive signal.
					// The Builder will ignore this.
					try {
						//logger.debug("Sending keepalive signal to Builder");
						out.writeObject(Integer.valueOf(-1));
						idleTimeMillis = 0L;
					} catch (IOException e) {
						logger.error("Error sending keepalive signal to Builder", e);
						break submissionTestingLoop;
					}
				}
			} else {
				idleTimeMillis = 0L;
			}

			if (submission != null) {
				// Check to make sure there isn't some kind of persistent error
				// affecting the testing of this submission
				if (submission.getNumAttempts() >= 10) {
					// Too many testing failures for this submission!
					submission.setReady();
					continue submissionTestingLoop;
				}
				
				submission.setNumAttempts(submission.getNumAttempts() + 1);
				
				// Attempt to test the submission
				try {
					sendSubmissionForTesting(submission);
					// Submission successfully tested!
					submission = null; // We're done with this Submission
				} catch (IOException e) {
					submission.setError(e);
					logger.error("IOException attempting to send submission for testing", e);
					break submissionTestingLoop;
				} catch (ClassNotFoundException e) {
					submission.setError(e);
					logger.error("ClassNotFoundException testing submission", e);
					break submissionTestingLoop;
				}
			}
		}
		
		// If the testing of a submission was not completed,
		// place it back in the queue so it has an opportunity to be re-tested
		if (submission != null) {
			try {
				submissionQueue.put(submission);
			} catch (InterruptedException e) {
				logger.error("Failed to put submission back in submission queue", e);
				submission.setError(e);
				submission.setReady();
			}
		}

		// End the connection with the Builder
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
		try {
			clientSocket.close();
		} catch (IOException e) {
			logger.warn("Exception closing client socket", e);
		}
		
		workerTaskSet.onWorkerExit(this);
		
		logger.info("oop buildsvc WorkerTask exiting");
	}

	private void sendSubmissionForTesting(OOPBuildServiceSubmission submission) throws IOException, ClassNotFoundException {
		Problem problem = submission.getProblem();
		List<TestCase> testCaseList = submission.getTestCaseList();
		String programText = submission.getProgramText();
		
		// Tell client which Problem to test
		out.writeObject((Integer) problem.getProblemId());
		out.flush();
		
		// Client will send back a boolean indicating whether or not it
		// has this problem already: if not, send it (and its test cases).
		Boolean response = (Boolean) in.readObject();
		if (!response) {
			out.writeObject(problem);
			out.writeObject(testCaseList);
			out.flush();
		}
		
		// Send the program text
		out.writeObject(programText);
		out.flush();
		
		// Read list of TestResults
		SubmissionResult result= (SubmissionResult) in.readObject();
		
//		logger.info("Received submission results");
//		for (SubmissionResultAnnotation annotation : result.getAnnotationList()) {
//			logger.info("key={}, value={}", annotation.getKey(), annotation.getValue());
//		}
		
		submission.setSubmissionResult(result);
		submission.setReady();
	}
}