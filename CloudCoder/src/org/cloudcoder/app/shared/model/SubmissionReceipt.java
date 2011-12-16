// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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
public class SubmissionReceipt implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long eventId;
	private long lastEditEventId;
	private int status;
	
	public SubmissionReceipt() {
		
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	
	/**
	 * @return the eventId
	 */
	public long getEventId() {
		return eventId;
	}
	
	/**
	 * @param lastEditEventId the lastEditEventId to set
	 */
	public void setLastEditEventId(long lastEditEventId) {
		this.lastEditEventId = lastEditEventId;
	}
	
	/**
	 * @return the lastEditEventId
	 */
	public long getLastEditEventId() {
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
}
