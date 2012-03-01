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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The set of {@link WorkerTask}s that are communicating with
 * remote Builder threads.
 * 
 * @author David Hovemeyer
 */
public class WorkerTaskSet {
	private static final Logger logger = LoggerFactory.getLogger(WorkerTaskSet.class);
    
	private static class WorkerThreadAndTaskPair {
		public final Thread thread;
		public final WorkerTask task;
		
		public WorkerThreadAndTaskPair(Thread thread, WorkerTask task) {
			this.thread = thread;
			this.task = task;
		}
		
		public void shutdown() {
			task.shutdown();
			thread.interrupt();
		}
	}

	private Object lock;
	private List<WorkerThreadAndTaskPair> workerThreadAndTaskPairList;
	
	/**
	 * Constructor.
	 */
	public WorkerTaskSet() {
		lock = new Object();
		this.workerThreadAndTaskPairList = new ArrayList<WorkerThreadAndTaskPair>();

	}
	
	/**
	 * Get the number of worker tasks that are currently active.
	 * 
	 * @return number of worker tasks that are currently active
	 */
	public int getNumWorkerTasks() {
		synchronized (lock) {
			return workerThreadAndTaskPairList.size();
		}
	}

	/**
	 * Create a {@link WorkerTask} to communicate with a remote
	 * Builder thread.
	 * 
	 * @param clientSocket     Socket with which to communicate with remote Builder thread
	 * @param submissionQueue  queue of submissions requiring compilation and testing
	 * @throws IOException
	 */
	public void createWorker(Socket clientSocket, LinkedBlockingQueue<OOPBuildServiceSubmission> submissionQueue) throws IOException {
		WorkerTask workerTask = new WorkerTask(clientSocket, submissionQueue, this);
		Thread workerThread = new Thread(workerTask);
		WorkerThreadAndTaskPair pair = new WorkerThreadAndTaskPair(workerThread, workerTask);
		
		// start worker thread
		pair.thread.start();

		// add thread/task to list
		synchronized (lock) {
			workerThreadAndTaskPairList.add(pair);
		}
	}

	/**
	 * Tell all worker tasks to shut down.
	 */
	public void shutdownAll() {
		synchronized (lock) {
			for (WorkerThreadAndTaskPair pair : workerThreadAndTaskPairList) {
				pair.shutdown();
			}
		}
	}

	/**
	 * Wait for all worker tasks to complete.
	 */
	public void waitForAll() {
		synchronized (lock) {
			while (workerThreadAndTaskPairList.size() > 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					logger.error("WorkerTaskSet interrupted while waiting for workers to finish");
				}
			}
		}
	}

	/**
	 * Called by a {@link WorkerTask} when the task exits.
	 * 
	 * @param workerTask a WorkerTask
	 */
	public void onWorkerExit(WorkerTask workerTask) {
		synchronized (lock) {
			for (Iterator<WorkerThreadAndTaskPair> i = workerThreadAndTaskPairList.iterator(); i.hasNext(); ) {
				WorkerThreadAndTaskPair pair = i.next();
				if (pair.task == workerTask) {
					i.remove();
					break;
				}
			}
			lock.notifyAll();
		}
	}

}
