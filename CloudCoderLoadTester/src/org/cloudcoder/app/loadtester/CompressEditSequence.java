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
