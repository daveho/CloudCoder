package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

public class WorkerTask implements Runnable {
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
		
		while (!shutdownRequested) {
			Submission submission = null;
			try {
				submission = submissionQueue.poll(1000, TimeUnit.MILLISECONDS);
				
				if (submission != null) {
					sendSubmissionForTesting(submission);
				}
			} catch (InterruptedException e) {
				if (!shutdownRequested) {
					// FIXME: log unexpected interruption
				}
			} catch (IOException e) {
				// FIXME: log
				submission.setError(e);
				break;
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("ClassNotFound while deserializing message", e);
			}
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
		List<TestResult> testResultList = (List<TestResult>) in.readObject();
		submission.setTestResultList(testResultList);
	}
}