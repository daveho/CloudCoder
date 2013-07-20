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

import org.cloudcoder.app.shared.model.Change;

/**
 * Compress an {@link EditSequence} to limit the maximum pause time.
 * 
 * @author David Hovemeyer
 */
public class CompressEditSequence {
	/**
	 * Default max pause time.
	 */
	public static final long DEFAULT_MAX_PAUSE_TIME_MS = 5000L;
	
	private long maxPauseTime;

	/**
	 * Constructor.
	 */
	public CompressEditSequence() {
		this.maxPauseTime = DEFAULT_MAX_PAUSE_TIME_MS;
	}
	
	/**
	 * Set the max pause time in milliseconds, which defaults to {@link #DEFAULT_MAX_PAUSE_TIME_MS}.
	 * 
	 * @param maxPauseTime default max pause time in milliseconds
	 */
	public void setMaxPauseTime(long maxPauseTime) {
		this.maxPauseTime = maxPauseTime;
	}
	
	/**
	 * Compress the given {@link EditSequence}.
	 * 
	 * @param seq the {@link EditSequence}
	 */
	public void compress(EditSequence seq) {
		long shift = 0L;
		
		Change last = null;
		for (Change c : seq.getChangeList()) {
			// Move the event back in time by the accumulated shift amount
			c.getEvent().setTimestamp(c.getEvent().getTimestamp() - shift);
			
			if (last != null) {
				// Compute the delta between this event's timestamp and the previous event's timestamp
				long delta = c.getEvent().getTimestamp() - last.getEvent().getTimestamp();
				if (delta > maxPauseTime) {
					// How late is this event?
					long lateness = delta - maxPauseTime;
					
					// Move the event earlier, and add to the accumulated shift amount
					c.getEvent().setTimestamp(c.getEvent().getTimestamp() - lateness);
					shift += lateness;
				}
			}
			
			last = c;
		}
	}
}
