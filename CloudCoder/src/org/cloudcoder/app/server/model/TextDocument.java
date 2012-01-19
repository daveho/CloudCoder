// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.app.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple text document class; treats document as sequence of lines.
 */
public class TextDocument {
	private List<String> lineList;

	/**
	 * Constructor: initialize empty text document.
	 */
	public TextDocument() {
		lineList = new ArrayList<String>();
	}

	/**
	 * Append a line to the document.
	 * 
	 * @param line line of text to append
	 */
	public void append(String line) {
		lineList.add(line);
	}
	
	/**
	 * @return number of lines of text in document
	 */
	public int getNumLines() {
		return lineList.size();
	}
	
	/**
	 * Get line at given index (0 for first line).
	 * 
	 * @param index index of line (0 for first line)
	 * @return line of text
	 */
	public String getLine(int index) {
		return lineList.get(index);
	}
	
	/**
	 * Replace line at given index (0 for first line).
	 * 
	 * @param index index of line (0 for first line)
	 * @param line text value to set as new value of line
	 */
	public void setLine(int index, String line) {
		lineList.set(index, line);
	}
	
	/**
	 * Insert a line in a text document,
	 * pushing lines at or below index down one line.
	 * 
	 * @param index where to insert the line
	 * @param line line of text to insert
	 */
	public void insertLine(int index, String line) {
		lineList.add(index, line);
	}

	/**
	 * Remove line at given index.
	 * Lines below are moved up.
	 * 
	 * @param index index of line to remove
	 */
	public void removeLine(int index) {
		lineList.remove(index);
	}

	/**
	 * @return complete text of document as string
	 */
	public String getText() {
		StringBuilder buf = new StringBuilder();

		for (String s : lineList) {
			buf.append(s);
		}
		
		return buf.toString();
	}

	public void setText(String text) {
		lineList.clear();
		String[] lines = text.split("\n");
		for (String line : lines) {
			lineList.add(line + "\n");
		}
	}
}
