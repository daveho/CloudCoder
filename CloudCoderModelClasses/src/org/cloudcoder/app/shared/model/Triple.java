// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2019, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * Generic triple object.
 * @author David Hovemeyer
 *
 * @param <First> type of first element
 * @param <Second> type of second element
 * @param <Third> type of third element
 */
public class Triple<First, Second, Third> extends Pair<First, Second> {
	private Third third;
	
	/**
	 * Constructor to create empty object.
	 */
	public Triple() {
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param first   first value
	 * @param second  second value
	 * @param third   third value
	 */
	public Triple(First first, Second second, Third third) {
		super(first, second);
		this.third = third;
	}
	
	/**
	 * Set the first value.
	 * 
	 * @param first the first value
	 */
	public void setFirst(First first) {
		setLeft(first);
	}
	
	/**
	 * @return the first value
	 */
	public First getFirst() {
		return getLeft();
	}
	
	/**
	 * Set the second value.
	 * 
	 * @param second the second value
	 */
	public void setSecond(Second second) {
		setRight(second);
	}
	
	/**
	 * @return the second value
	 */
	public Second getSecond() {
		return getRight();
	}
	
	/**
	 * Set the third value.
	 * 
	 * @param third the third value
	 */
	public void setThird(Third third) {
		this.third = third;
	}
	
	/**
	 * @return the third value
	 */
	public Third getThird() {
		return third;
	}
}
