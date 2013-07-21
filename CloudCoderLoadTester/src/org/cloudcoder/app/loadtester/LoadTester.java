// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.loadtester;

import java.net.CookieHandler;

/**
 * Load tester: creates {@link LoadTesterTask}s and runs them
 * in as many threads as necessary to achieve the desired degree
 * of concurrency.
 * 
 * @author David Hovemeyer
 */
public class LoadTester {
	
	private HostConfig hostConfig;
	private Mix mix;
	private int numThreads;
	private int repeatCount;
	private long maxPause;

	/**
	 * Constructor.
	 */
	public LoadTester() {
		
	}
	
	/**
	 * Set the {@link HostConfig} specifying how to connect to the host webapp.
	 * 
	 * @param hostConfig the {@link HostConfig}
	 */
	public void setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
	}
	
	/**
	 * Set the {@link Mix} containing the {@link EditSequence}s that will be played.
	 * 
	 * @param mix the {@link Mix}
	 */
	public void setMix(Mix mix) {
		// Make a clone of the Mix, since we may modify the EditSequences
		// (e.g., to set a minimim pause time)
		this.mix = mix.clone();
	}
	
	/**
	 * Set how many concurrent threads should be used.
	 * 
	 * @param numThreads number of concurrent threads
	 */
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	/**
	 * Set how many times each thread should repeat playing its edit sequence.
	 * 
	 * @param repeatCount how many times each thread should repeat playing its edit sequence
	 */
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	/**
	 * Set the maximum pause between events, in milliseconds.
	 * If the max pause time is set to 0, then there will be no maximum
	 * pause time (and the {@link EditSequence}s will be played back
	 * using their original timing).
	 * 
	 * @param maxPause the max pause between events, in milliseconds
	 */
	public void setMaxPause(long maxPause) {
		this.maxPause = maxPause;
	}
	
	/**
	 * Execute the tasks and wait for them to complete.
	 */
	public void execute() {
		// Ensure that the activity reporter's monitor thread is running
		LoadTesterActivityReporter.getInstance().start();
		
		// Set LoadTesterCookieHandler singleton instance as the global
		// default cookie handler.  This will create per-thread
		// CookieManagers, so that each load tester task thread will
		// have its own cookie store.
		CookieHandler.setDefault(LoadTesterCookieHandler.getInstance());
		
		// If a max pause time was set, compress the edit sequences
		if (maxPause > 0) {
			for (EditSequence seq : mix.getEditSequenceList()) {
				CompressEditSequence c = new CompressEditSequence();
				c.setMaxPauseTime(maxPause);
				c.compress(seq);
			}
		}
		
		// Create tasks
		LoadTesterTask[] tasks = new LoadTesterTask[numThreads];
		
		// EditSequences are assigned to tasks in round-robin order
		int seqIndex = 0;
		
		for (int i = 0; i < numThreads; i++) {
			tasks[i] = new LoadTesterTask();
			
			// We assume the test user accounts are "user1", "user2", etc.,
			// with passwords matching the usernames.
			String testUserName = "user" + (i+1);
			tasks[i].setUserName(testUserName);
			tasks[i].setPassword(testUserName);
			tasks[i].setHostConfig(hostConfig);
			tasks[i].setEditSequence(mix.get(seqIndex));
			tasks[i].setRepeatCount(repeatCount);
			tasks[i].setOnSend(LoadTesterActivityReporter.getInstance().getOnSendCallback());
			tasks[i].setOnSubmissionResult(LoadTesterActivityReporter.getInstance().getOnSubmissionResultCallback());
			
			seqIndex++;
			if (seqIndex >= mix.size()) {
				seqIndex = 0;
			}
		}
		
		// Create threads to execute the tasks, and start them
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(tasks[i]);
			threads[i].start();
		}
		
		// Wait for tasks to complete
		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.err.println("InterruptedException waiting for thread " + i);
			}
		}
		
		System.out.println("\nLoading testing complete");
	}
}
