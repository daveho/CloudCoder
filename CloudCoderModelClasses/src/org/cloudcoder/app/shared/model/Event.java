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
import java.util.Arrays;

/**
 * "Superclass" for event types.
 * Records common information (timestamp, user id, problem id) about each
 * event, and has a link field (data_id) to a corresponding row in
 * another table with additional information about the specific event.
 */
public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int userId;
	private int problemId;
	private EventType type;
	private long timestamp;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema SCHEMA = new ModelObjectSchema(Arrays.asList(
			new ModelObjectField("id", Integer.class, 0, ModelObjectIndexType.IDENTITY),
			new ModelObjectField("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE),
			new ModelObjectField("problem_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE),
			new ModelObjectField("type", EventType.class, 0),
			new ModelObjectField("timestamp", Long.class, 0)
	));

	public Event() {

	}
	
	public Event(int userId, int problemId, EventType type, long timestamp) {
		this.userId = userId;
		this.problemId = problemId;
		this.type = type;
		this.timestamp = timestamp;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	public int getProblemId() {
		return problemId;
	}

	public void setType(int type) {
		this.type = EventType.values()[type];
	}

	public void setType(EventType type) {
		this.type = type;
	}

//	public int getType() {
//		return eventType.ordinal();
//	}

	public EventType getType() {
		return type;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
