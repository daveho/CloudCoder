// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * A record for recording the code coverage of a single line.
 * 
 * @author David Hovemeyer
 */
public class LineCoverageRecord {
	private int lineNumber;
	private int timesExecuted;

	/**
	 * Constructor.
	 */
	public LineCoverageRecord() {
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param lineNumber     the line number
	 * @param timesExecuted  the number of times the line was executed
	 */
	public LineCoverageRecord(int lineNumber, int timesExecuted) {
		this.lineNumber = lineNumber;
		this.timesExecuted = timesExecuted;
	}

	/**
	 * Set the line number.
	 * @param lineNumber the line number to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	/**
	 * @return the line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Set the number of times the line was executed.
	 * @param timesExecuted number of times the line was executed
	 */
	public void setTimesExecuted(int timesExecuted) {
		this.timesExecuted = timesExecuted;
	}

	/**
	 * @return number of times the line was executed
	 */
	public int getTimesExecuted() {
		return timesExecuted;
	}
}
