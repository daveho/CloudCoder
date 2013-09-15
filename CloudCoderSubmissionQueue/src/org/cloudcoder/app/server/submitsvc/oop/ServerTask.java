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
import java.util.concurrent.LinkedBlockingQueue;

import org.cloudcoder.app.server.model.HealthData;
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
    
    // Update HealthData every 5 seconds.
	public static final long UPDATE_SUBMISSION_QUEUE_SIZE_INTERVAL = 5000L;
    
    private class HealthMonitorTask implements Runnable {
    	@Override
    	public void run() {
    		while (!shutdownRequested) {
    			try {
    				int size = submissionQueue.size();
    				HealthData.getInstance().updateSubmissionQueueSize(size);
    				
    				Thread.sleep(UPDATE_SUBMISSION_QUEUE_SIZE_INTERVAL);
    			} catch (InterruptedException e) {
    				// Shutting down
    				logger.info("Health monitor thread interrupted...");
    			}
    		}
    	}
    }

	private LinkedBlockingQueue<OOPBuildServiceSubmission> submissionQueue;
	private ServerSocket serverSocket;
	private WorkerTaskSet workerTaskSet;
	private volatile boolean shutdownRequested;
	private Thread healthMonitorThread;
	
	public ServerTask(ServerSocket serverSocket) {
		this.submissionQueue = new LinkedBlockingQueue<OOPBuildServiceSubmission>();
		this.serverSocket = serverSocket;
		this.workerTaskSet = new WorkerTaskSet();
		this.shutdownRequested = false;
	}
	
	public int getNumWorkerTasks() {
		return workerTaskSet.getNumWorkerTasks();
	}
	
	public void submit(OOPBuildServiceSubmission submission) throws SubmissionException {
		// add it to the queue so a worker can grab it	
	    submissionQueue.add(submission);
	}
	
	@Override
	public void run() {
		healthMonitorThread = new Thread(new HealthMonitorTask());
		healthMonitorThread.start();
		
		try {
			while (!shutdownRequested) {
				Socket clientSocket = serverSocket.accept();
				
				// FIXME: we should support whitelisting of client IPs
				
				// create worker task and thread
				workerTaskSet.createWorker(clientSocket, submissionQueue);
			}
		
		} catch (IOException e) {
			if (!shutdownRequested) {
			    logger.error("IOException waiting for connections", e);
			}
		}
	}

	public void shutdown() {
		shutdownRequested = true;
		
		// Shut down the submission queue size monitor thread
		healthMonitorThread.interrupt();
		
		// close server sockets (so no new clients can attach)
		try {
			serverSocket.close();  // a bit rude, but effective
		} catch (IOException e) {
		    logger.error("Exception closing ServerTask's socket", e);
		}
		
		// shut down worker tasks and wait for them to exit
		workerTaskSet.shutdownAll();
		workerTaskSet.waitForAll();
	}
}