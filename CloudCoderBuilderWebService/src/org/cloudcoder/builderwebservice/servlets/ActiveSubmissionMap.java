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

package org.cloudcoder.builderwebservice.servlets;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singelton collection to keep track of all active submissions, accessed by a
 * generated unique key for each submission.
 * 
 * @author David Hovemeyer
 */
public class ActiveSubmissionMap {
	private static final Logger logger = LoggerFactory.getLogger(ActiveSubmissionMap.class);
	
	/**
	 * The maximum age that a submission may reach before being reaped.
	 * Currently, set to 2.5 minutes.
	 */
	private static final long MAX_SUBMISSION_AGE_MS = (5*(60*1000))/2;
	
	/**
	 * How often the reaper thread should scan the active submissions and
	 * reap the ones that are too old.  Currently set to 30 seconds.
	 */
	private static final long REAPER_INTERVAL_MS = 30*1000;

	private static final ActiveSubmissionMap theInstance = new ActiveSubmissionMap();
	static {
		// Start up the reaper thread (to purge abandoned submissions)
		theInstance.startReaperThread();
	}
	
	/**
	 * Get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static ActiveSubmissionMap getInstance() {
		return theInstance;
	}
	
	// FIXME: need a task to periodically scan for old submissions and remove them
	
	private Random rng;
	private AtomicInteger next;
	private ConcurrentHashMap<String, ActiveSubmission> submissionMap;
	
	/**
	 * Constructor.
	 */
	private ActiveSubmissionMap() {
		// Create a SecureRandom (which is based on actual physical entropy)
		// to seed the random number generator which will generate the
		// unique keys.
		SecureRandom sr = new SecureRandom();
		this.rng = new Random(sr.nextLong());
		this.next = new AtomicInteger(1);
		this.submissionMap = new ConcurrentHashMap<String, ActiveSubmission>();
	}
	
	private void startReaperThread() {
		Thread reaper = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("reaper thread starting");
				try {
					while (true) {
						Thread.sleep(REAPER_INTERVAL_MS);
						reap();
					}
				} catch (InterruptedException e) {
					logger.error("reaper thread interrupted unexpectedly", e);
				}
			}
		});
		reaper.setDaemon(true);
		reaper.start();
	}
	
	private void reap() {
		//System.out.println("reaping stale submissions...");
		
		long now = System.currentTimeMillis();
		
		Iterator<Map.Entry<String, ActiveSubmission>> i = submissionMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, ActiveSubmission> entry = i.next();
			if (entry.getValue().getTimestamp() + MAX_SUBMISSION_AGE_MS < now) {
				//System.out.println("reaper removing entry " + entry.getKey());
				i.remove();
			}
		}
	}
	
	/**
	 * Add an {@link IFutureSubmissionResult} to the collection.
	 * 
	 * @param result the {@link IFutureSubmissionResult} to add
	 * @return the unique key that will identify
	 */
	public String add(IFutureSubmissionResult result) {
		String key = "" + String.format("%x", rng.nextLong()) + ":" + next.incrementAndGet();
		submissionMap.put(key, new ActiveSubmission(result, System.currentTimeMillis()));
		return key;
	}
	
	/**
	 * Get a {@link IFutureSubmissionResult} given its unique key (returned from a previous
	 * call to {@link #add(IFutureSubmissionResult)}.
	 * 
	 * @param key the unique key
	 * @return the {@link IFutureSubmissionResult}, or null if the key is unknown
	 */
	public IFutureSubmissionResult get(String key) {
		ActiveSubmission submission = submissionMap.get(key);
		return (submission == null) ? null : submission.getResult();
	}
	
	/**
	 * Remove the {@link IFutureSubmissionResult} for given key.
	 * This should be done once an actual submission result is produced
	 * and returned to a client.
	 * 
	 * @param key the unique key
	 */
	public void purge(String key) {
		submissionMap.remove(key);
	}
}
