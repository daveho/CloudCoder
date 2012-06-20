package org.cloudcoder.app.shared.model;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;

import java.io.Serializable;

/**
 * "Superclass" for event types.
 * Records common information (timestamp, user id, problem id) about each
 * event, and has a link field (data_id) to a corresponding row in
 * another table with additional information about the specific event.
 */
//@Entity
//@Table(name="events")
public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
//	@Column(name="id")
	private int id;
	
//	@Column(name="user_id")
	private int userId;
	
//	@Column(name="problem_id")
	private int problemId;

//	@Column(name="type")
	private int type;

//	@Column(name="timestamp")
	private long timestamp;

	public Event() {

	}
	
	public Event(int userId, int problemId, EventType type, long timestamp) {
		this.userId = userId;
		this.problemId = problemId;
		this.type = type.ordinal();
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
		this.type = type;
	}

	public void setType(EventType type) {
		this.type = type.ordinal();
	}

	public int getType() {
		return type;
	}

	public EventType getEventType() {
		return EventType.values()[type];
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
