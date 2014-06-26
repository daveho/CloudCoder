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
public class Change implements Serializable, IContainsEvent, IModelObject<Change>, Cloneable {
	private static final long serialVersionUID = 1L;

	/**
	 * Maximum number of characters that may be stored as text
	 * directly in a row of the changes table.
	 * Text values which are longer will be stored in a blob.
	 */
	public static final int MAX_TEXT_LEN_IN_ROW = 40;
	
	public static final int NUM_FIELDS = 7;

	private int eventId;
	private int type;
	private int startRow;
	private int startColumn;
	private int endRow;
	private int endColumn;
	private String text;
	
	/** {@link ModelObjectField} for unique id. */
	public static final ModelObjectField<Change, Integer> EVENT_ID =
			new ModelObjectField<Change, Integer>("event_id", Integer.class, 0, ModelObjectIndexType.UNIQUE) {
		public void set(Change obj, Integer value) { obj.setEventId(value); }
		public Integer get(Change obj) { return obj.getEventId(); }
	};
	/** {@link ModelObjectField} for change type. */
	public static final ModelObjectField<Change, ChangeType> TYPE =
			new ModelObjectField<Change, ChangeType>("type", ChangeType.class, 0) {
		public void set(Change obj, ChangeType value) { obj.setType(value); }
		public ChangeType get(Change obj) { return obj.getType(); }
	};
	/** {@link ModelObjectField} for start row. */
	public static final ModelObjectField<Change, Integer> START_ROW =
			new ModelObjectField<Change, Integer>("start_row", Integer.class, 0) {
		public void set(Change obj, Integer value) { obj.setStartRow(value); }
		public Integer get(Change obj) { return obj.getStartRow(); }
	};
	/** {@link ModelObjectField} for end row. */
	public static final ModelObjectField<Change, Integer> END_ROW =
			new ModelObjectField<Change, Integer>("end_row", Integer.class, 0) {
		public void set(Change obj, Integer value) { obj.setEndRow(value); }
		public Integer get(Change obj) { return obj.getEndRow(); }
	};
	/** {@link ModelObjectField} for start column. */
	public static final ModelObjectField<Change, Integer> START_COL =
			new ModelObjectField<Change, Integer>("start_col", Integer.class, 0) {
		public void set(Change obj, Integer value) { obj.setStartColumn(value); }
		public Integer get(Change obj) { return obj.getStartColumn(); }
	};
	/** {@link ModelObjectField} for end column. */
	public static final ModelObjectField<Change, Integer> END_COL =
			new ModelObjectField<Change, Integer>("end_col", Integer.class, 0) {
		public void set(Change obj, Integer value) { obj.setEndColumn(value); }
		public Integer get(Change obj) { return obj.getStartColumn(); }
	};
	/** {@link ModelObjectField} for short change text. */
	public static final ModelObjectField<Change, String> TEXT_SHORT =
			new ModelObjectField<Change, String>("text_short", String.class, 80, ModelObjectIndexType.NONE, ModelObjectField.ALLOW_NULL) {
		public void set(Change obj, String value) { obj.setText(value); }
		public String get(Change obj) { return obj.getText(); }
	};
	/** {@link ModelObjectField} for long change text. */
	public static final ModelObjectField<Change, String> TEXT =
			new ModelObjectField<Change, String>("text", String.class, 32768, ModelObjectIndexType.NONE, ModelObjectField.ALLOW_NULL) {
		public void set(Change obj, String value) { obj.setText(value); }
		public String get(Change obj) { return obj.getText(); }
	};
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<Change> SCHEMA = new ModelObjectSchema<Change>("change")
		.add(EVENT_ID)
		.add(TYPE)
		.add(START_ROW)
		.add(END_ROW)
		.add(START_COL)
		.add(END_COL)
		.add(TEXT_SHORT)
		.add(TEXT);
	
	// Transient link to the Event object associated with this Change.
	private Event event;

	// Zero-arg constructor - required for serialization
	// also required for persistence
	public Change() {
	}
	
	@Override
	public ModelObjectSchema<Change> getSchema() {
		return SCHEMA;
	}

	private Change(ChangeType type, int sr, int sc, int er, int ec, long ts, int userId, int problemId) {
		this.type = type.ordinal();
		this.startRow = sr;
		this.startColumn = sc;
		this.endRow = er;
		this.endColumn = ec;

		this.event = new Event(userId, problemId, EventType.CHANGE, ts);
	}

	public Change(ChangeType type, int sr, int sc, int er, int ec, long ts, int userId, int problemId, String text) {
		this(type, sr, sc, er, ec, ts, userId, problemId);
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
	
	/**
	 * @return an exact deep copy of this object
	 */
	public Change duplicate() {
		Change dup = new Change();
	
		// Shallow copy
		ModelObjectUtil.copy(this, dup);
		
		// If there is an Event object, make a duplicate
		dup.event = this.event; // transient field, not copied by ModelObjectUtil.copy()
		if (dup.event != null) {
			dup.event = this.event.duplicate();
		}
		
		return dup;
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
		return ChangeType.valueOf(type) + "," + startRow + "," + startColumn + "," + endRow + "," + endColumn/* + "," + timestamp*/ + "," + Arrays.asList(text);
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + endColumn;
        result = prime * result + endRow;
        result = prime * result + eventId;
        result = prime * result + startColumn;
        result = prime * result + startRow;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Change other = (Change) obj;
        if (endColumn != other.endColumn)
            return false;
        if (endRow != other.endRow)
            return false;
        if (eventId != other.eventId)
            return false;
        if (startColumn != other.startColumn)
            return false;
        if (startRow != other.startRow)
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
	
}
