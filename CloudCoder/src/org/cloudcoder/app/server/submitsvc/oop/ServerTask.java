package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerTask implements Runnable {
    private static final Logger logger=LoggerFactory.getLogger(ServerTask.class);
    
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
	
	public SubmissionResult submit(Submission submission) throws SubmissionException {
		// add it to the queue so a worker can grab it	
	    submissionQueue.add(submission);
		
		// wait for results
		return submission.getSubmissionResult();
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
			    
			}
		}
	}

	public void shutdown() {
		shutdownRequested = true;
		
		// close server sockets (so no new clients can attach)
		try {
			serverSocket.close();  // a bit rude, but effective
		} catch (IOException e) {
		    logger.error("Exception in ServerTask", e);
		}
		
		// shut down workers
		for (WorkerThreadAndTaskPair pair : workerThreadAndTaskPairList) {
			pair.shutdown();
			try {
				pair.thread.join();
			} catch (InterruptedException e) {
			    logger.error("Unable to join", e);
			}
		}
	}
}