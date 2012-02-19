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
 * Object representing a textual change.
 * The client sends these to the server so that we
 * can capture the user's edit history.
 */
public class Change implements Serializable, IContainsEvent {
	private static final long serialVersionUID = 1L;

	public static final int NUM_FIELDS = 7;

	//private long id;
	private int eventId;
	private int type;
	private int startRow;
	private int startColumn;
	private int endRow;
	private int endColumn;
	private String text;

	private Event event;


	// Zero-arg constructor - required for serialization
	// also required for persistence
	public Change() {
	}

	private Change(ChangeType type, int sr, int sc, int er, int ec, long ts, int userId, int problemId) {
		this.type = type.ordinal();
		this.startRow = sr;
		this.startColumn = sc;
		this.endRow = er;
		this.endColumn = ec;
		//this.timestamp = ts;

		this.event = new Event(userId, problemId, EventType.CHANGE, ts);
	}

	public Change(ChangeType type, int sr, int sc, int er, int ec, long ts, int userId, int problemId, String text) {
		this(type, sr, sc, er, ec, ts, userId, problemId);
		//this.text = Collections.singletonList(text);
		this.text=text;
	}

	public Change(ChangeType type, int sr, int sc, int er, int ec, long ts, int userId, int problemId, String[] textToAdopt) {
		this(type, sr, sc, er, ec, ts, userId, problemId);
		StringBuffer buf=new StringBuffer();
		for (int i=0; i<textToAdopt.length-1; i++) {
			buf.append(textToAdopt[i]);
			buf.append("\n");
		}
		buf.append(textToAdopt[textToAdopt.length-1]);
		this.text = buf.toString();
	}

	public ChangeType getType() {
		return ChangeType.values()[type];
	}

	/**
	 * Get single chunk of text (for INSERT_TEXT and REMOVE_TEXT events).
	 * 
	 * @return chunk of text inserted or removed
	 */
	public String getText() {
		//assert text.length == 1;
		//return text[0];
		return text;
	}

	/**
	 * Get given line (for INSERT_LINES and REMOVE_LINES) events.
	 * 
	 * @param index index of the inserted or removed line (0 for first).
	 * @return the inserted or removed line
	 */
	public String getLine(int index) {
		return text.split("\n")[index];
	}

	/**
	 * Get number of lines.
	 * Always 1 for INSERT_TEXT and REMOVE_TEXT events.
	 * Could be greater than 1 for INSERT_LINES and REMOVE_LINES events.
	 * 
	 * @return number of lines
	 */
	public int getNumLines() {
		ChangeType type = getType();
		if (type.equals(ChangeType.INSERT_TEXT.toString()) ||
				type.equals(ChangeType.REMOVE_TEXT.toString()))
		{
			return 1;
		}
		return text.split("\n").length;
	}

	/**
	 * @return start row of change
	 */
	public int getStartRow() {
		return startRow;
	}

	/**
	 * @return start column of change
	 */
	public int getStartColumn() {
		return startColumn;
	}

	/**
	 * @return end row of change
	 */
	public int getEndRow() {
		return endRow;
	}

	/**
	 * @return end column of change
	 */
	public int getEndColumn() {
		return endColumn;
	}

	//	/**
	//	 * @return timestamp of change (milliseconds since epoch), as reported by client
	//	 */
	//	public long getTimestamp() {
	//		return timestamp;
	//	}

//	/**
//	 * @return the id
//	 */
//	public long getId()
//	{
//		return id;
//	}
//
//	/**
//	 * @param id the id to set
//	 */
//	public void setId(long id)
//	{
//		this.id = id;
//	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IContainsEvent#setEventId(int)
	 */
	@Override
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IContainsEvent#getEventId()
	 */
	@Override
	public int getEventId() {
		return eventId;
	}

	//    /**
	//     * @return the userId
	//     */
	//    public long getUserId()
	//    {
	//        return userId;
	//    }
	//
	//    /**
	//     * @param userId the userId to set
	//     */
	//    public void setUserId(long userId)
	//    {
	//        this.userId = userId;
	//    }
	//
	//    /**
	//     * @return the problemId
	//     */
	//    public long getProblemId()
	//    {
	//        return problemId;
	//    }
	//
	//    /**
	//     * @param problemId the problemId to set
	//     */
	//    public void setProblemId(long problemId)
	//    {
	//        this.problemId = problemId;
	//    }

	/**
	 * @param type the type to set (as an integer)
	 */
	public void setType(int type){
		this.type = type;
	}

	/**
	 * @param type the type to set (as a ChangeType value)
	 */
	public void setType(ChangeType type) {
		this.type = type.ordinal();
	}

	/**
	 * @param startRow the startRow to set
	 */
	public void setStartRow(int startRow)    {
		this.startRow = startRow;
	}

	/**
	 * @param startColumn the startColumn to set
	 */
	public void setStartColumn(int startColumn){
		this.startColumn = startColumn;
	}

	/**
	 * @param endRow the endRow to set
	 */
	public void setEndRow(int endRow){
		this.endRow = endRow;
	}

	/**
	 * @param endColumn the endColumn to set
	 */
	public void setEndColumn(int endColumn){
		this.endColumn = endColumn;
	}

	//    /**
	//     * @param timestamp the timestamp to set
	//     */
	//    public void setTimestamp(long timestamp){
	//        this.timestamp = timestamp;
	//    }

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

	/**
	 * @param text the text to set
	 */
	public void setText(String text){
		this.text = text;
	}

	@Override
	public String toString() {
		return type + "," + startRow + "," + startColumn + "," + endRow + "," + endColumn/* + "," + timestamp*/ + "," + Arrays.asList(text);
	}
}
