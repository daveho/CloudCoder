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
import org.cloudcoder.app.shared.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerTask implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(WorkerTask.class);
	
	private volatile boolean shutdownRequested;
	private Socket clientSocket;
	private LinkedBlockingQueue<Submission> submissionQueue;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public WorkerTask(Socket clientSocket, LinkedBlockingQueue<Submission> submissionQueue) throws IOException {
		this.shutdownRequested = false;
		this.clientSocket = clientSocket;
		this.submissionQueue = submissionQueue;
		
		this.out = new ObjectOutputStream(clientSocket.getOutputStream());
		this.in = new ObjectInputStream(clientSocket.getInputStream());
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	@Override
	public void run() {
		Submission submission = null;
		
		// Testing of submissions by this worker continues until either an
		// explicit shutdown request is made, or an exception is thrown communicating
		// with the remote Builder process.
	submissionTestingLoop:
		while (!shutdownRequested) {

			// Try to get a submission to test
			try {
				submission = submissionQueue.poll(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				if (!shutdownRequested) {
					logger.error("Unexpected interruption", e);
					break submissionTestingLoop;
				}
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
	}

	private void sendSubmissionForTesting(Submission submission) throws IOException, ClassNotFoundException {
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
		submission.setSubmissionResult(result);
		submission.setReady();
	}
}