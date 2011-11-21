package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.TestResult;


public class ServerTask implements Runnable {
	private static class WorkerThreadAndTaskPair {
		public Thread thread;
		public WorkerTask task;
		
		public WorkerThreadAndTaskPair(Thread thread, WorkerTask task) {
			this.thread = thread;
			this.task = task;
		}
		
		public void shutdown() {
			task.shutdown();
			thread.interrupt();
		}
	}

	private LinkedBlockingQueue<Submission> submissionQueue;
	private List<WorkerThreadAndTaskPair> workerThreadAndTaskPairList;
	private ServerSocket serverSocket;
	private volatile boolean shutdownRequested;
	
	public ServerTask(ServerSocket serverSocket) {
		this.submissionQueue = new LinkedBlockingQueue<Submission>();
		this.workerThreadAndTaskPairList = new ArrayList<WorkerThreadAndTaskPair>();
		this.serverSocket = serverSocket;
		this.shutdownRequested = false;
	}
	
	public List<TestResult> submit(Submission submission) throws SubmissionException {
		// add it to the queue so a worker can grab it	
		submissionQueue.add(submission);
		
		// wait for results
		return submission.getTestResultList();
	}
	
	@Override
	public void run() {
		try {
			while (!shutdownRequested) {
				Socket clientSocket = serverSocket.accept();
				
				// FIXME: we should support whitelisting of client IPs
				
				// create worker task and thread
				WorkerTask workerTask = new WorkerTask(clientSocket, submissionQueue);
				Thread workerThread = new Thread(workerTask);
				WorkerThreadAndTaskPair pair = new WorkerThreadAndTaskPair(workerThread, workerTask);

				// add thread/task to list
				workerThreadAndTaskPairList.add(pair);
				
				// start worker thread
				pair.thread.start();
			}
		
		} catch (IOException e) {
			if (!shutdownRequested) {
				// FIXME: log
			}
		}
	}

	public void shutdown() {
		shutdownRequested = true;
		
		// close server sockets (so no new clients can attach)
		try {
			serverSocket.close();  // a bit rude, but effective
		} catch (IOException e) {
			// ignore (FIXME: log)
		}
		
		// shut down workers
		for (WorkerThreadAndTaskPair pair : workerThreadAndTaskPairList) {
			pair.shutdown();
			try {
				pair.thread.join();
			} catch (InterruptedException e) {
				// FIXME: log
			}
		}
	}
}