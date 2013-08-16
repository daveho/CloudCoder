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

/**
 * Type of textual change.
 */
public enum ChangeType {
	/** Insertion of text within a particular line */
	INSERT_TEXT,
	
	/** Removal of text within a particular line. */
	REMOVE_TEXT,
	
	/** Insertion of one or more lines. */
	INSERT_LINES,
	
	/** Removal of one or more lines. */
	REMOVE_LINES,
	
	/**
	 * The full text of the document.
	 * Not really a change, but provides a convenient synchronization
	 * point for incremental changes.
	 */
	FULL_TEXT;
	
	public static ChangeType valueOf(int type) {
	    switch (type) {
        case 0:
            return INSERT_TEXT;
        case 1:
            return REMOVE_TEXT;
        case 2: 
            return INSERT_LINES;
        case 3:
            return REMOVE_LINES;
        case 4:
            return FULL_TEXT;
        default:
            throw new IllegalStateException("Unknown ChangeType int value: " + type);
	    }
	}
}
