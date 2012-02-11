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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * A SubmissionReceipt is a persistent record that a submission
 * occurred.  It is a type of event, specifically an event
 * with type {@link EventType#SUBMIT EventType.SUBMIT}.
 * Its purpose is to record the status of the submission
 * and to record the last edit event (i.e., revision) of the
 * submitted text.
 * 
 * @author David Hovemeyer
 */
public class SubmissionReceipt implements Serializable, IContainsEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * Number of database fields.
	 */
	public static final int NUM_FIELDS = 6;

	private Event event;
	private int id;
	private int eventId;
	private int lastEditEventId;
	private int status;
	private int numTestsAttempted;
	private int numTestsPassed;
	
	public SubmissionReceipt() {
		this.event = new Event();
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IContainsEvent#setEvent(org.cloudcoder.app.shared.model.Event)
	 */
	@Override
	public void setEvent(Event event) {
		this.event = event;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IContainsEvent#getEvent()
	 */
	@Override
	public Event getEvent() {
		return event;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IContainsEvent#setEventId(int)
	 */
	@Override
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	
	/**
	 * @return the eventId
	 */
	public int getEventId() {
		return this.eventId;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @param lastEditEventId the lastEditEventId to set
	 */
	public void setLastEditEventId(int lastEditEventId) {
		this.lastEditEventId = lastEditEventId;
	}
	
	/**
	 * @return the lastEditEventId
	 */
	public int getLastEditEventId() {
		return lastEditEventId;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(SubmissionStatus status) {
		this.status = status.ordinal();
	}
	
	/**
	 * @return the status
	 */
	public SubmissionStatus getStatus() {
		return SubmissionStatus.values()[status];
	}
	
	/**
	 * @param numTestsAttempted the numTestsAttempted to set
	 */
	public void setNumTestsAttempted(int numTestsAttempted) {
		this.numTestsAttempted = numTestsAttempted;
	}
	
	/**
	 * @return the numTestsAttempted
	 */
	public int getNumTestsAttempted() {
		return numTestsAttempted;
	}
	
	/**
	 * @param numTestsPassed the numTestsPassed to set
	 */
	public void setNumTestsPassed(int numTestsPassed) {
		this.numTestsPassed = numTestsPassed;
	}
	
	/**
	 * @return the numTestsPassed
	 */
	public int getNumTestsPassed() {
		return numTestsPassed;
	}
	
	/**
	 * Create a SubmissionReceipt for given User and Problem.
	 * 
	 * @param user     the User
	 * @param problem  the Problem
	 * @param status   the SubmissionStatus to set in the SubmissionReceipt
	 * @param lastEditEventId id of last edit event (i.e., the code version of the submission)
	 * @param numTestsAttempted the number of tests that were attempted for this submission
	 * @param numTestsPassed the number of tests that were passed for this submission
	 * @return  the SubmissionReceipt
	 */
	public static SubmissionReceipt create(
			final User user,
			final Problem problem,
			SubmissionStatus status,
			int lastEditEventId,
			int numTestsAttempted,
			int numTestsPassed) {
		SubmissionReceipt receipt = new SubmissionReceipt();
		receipt.getEvent().setProblemId(problem.getProblemId());
		receipt.getEvent().setTimestamp(System.currentTimeMillis());
		receipt.getEvent().setType(EventType.SUBMIT);
		receipt.getEvent().setUserId(user.getId());
		receipt.setLastEditEventId(lastEditEventId);
		receipt.setStatus(status);
		receipt.setNumTestsAttempted(numTestsAttempted);
		receipt.setNumTestsPassed(numTestsPassed);
		return receipt;
	}
}
