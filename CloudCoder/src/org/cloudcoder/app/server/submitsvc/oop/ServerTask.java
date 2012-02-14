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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.cloudcoder.app.shared.model.SubmissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server to listen for connections from remote Builder processes
 * and start WorkerTask threads to communicate with them.
 * 
 * @author David Hovemeyer
 */
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

	private LinkedBlockingQueue<OOPBuildServiceSubmission> submissionQueue;
	private List<WorkerThreadAndTaskPair> workerThreadAndTaskPairList;
	private ServerSocket serverSocket;
	private volatile boolean shutdownRequested;
	
	public ServerTask(ServerSocket serverSocket) {
		this.submissionQueue = new LinkedBlockingQueue<OOPBuildServiceSubmission>();
		this.workerThreadAndTaskPairList = new ArrayList<WorkerThreadAndTaskPair>();
		this.serverSocket = serverSocket;
		this.shutdownRequested = false;
	}
	
	public void submit(OOPBuildServiceSubmission submission) throws SubmissionException {
		// add it to the queue so a worker can grab it	
	    submissionQueue.add(submission);
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
			    logger.error("IOException waiting for connections", e);
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