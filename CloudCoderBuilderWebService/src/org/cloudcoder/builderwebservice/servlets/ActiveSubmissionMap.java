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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;

/**
 * Singelton collection to keep track of all active submissions, accessed by a
 * generated unique key for each submission.
 * 
 * @author David Hovemeyer
 */
public class ActiveSubmissionMap {
	private static final ActiveSubmissionMap theInstance = new ActiveSubmissionMap();
	
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
	private ConcurrentHashMap<String, IFutureSubmissionResult> submissionMap;
	
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
		this.submissionMap = new ConcurrentHashMap<String, IFutureSubmissionResult>();
	}
	
	/**
	 * Add an {@link IFutureSubmissionResult} to the collection.
	 * 
	 * @param result the {@link IFutureSubmissionResult} to add
	 * @return the unique key that will identify
	 */
	public String add(IFutureSubmissionResult result) {
		String key = "" + String.format("%x", rng.nextLong()) + ":" + next.incrementAndGet();
		submissionMap.put(key, result);
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
		return submissionMap.get(key);
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
