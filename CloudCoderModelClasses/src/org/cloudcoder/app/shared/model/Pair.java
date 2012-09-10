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
 * Generic pair object.
 *
 * @param <L> type of left value in pair
 * @param <R> type of right value in pair

 * @author David Hovemeyer
 */
public class Pair<L, R> {
	private L left;
	private R right;
	
	/**
	 * Constructor.
	 */
	public Pair() {
		
	}
	
	/**
	 * Set left value.
	 * @param left left value
	 */
	public void setLeft(L left) {
		this.left = left;
	}
	
	/**
	 * @return left value in pair
	 */
	public L getLeft() {
		return left;
	}
	
	/**
	 * Set right value.
	 * @param right right value
	 */
	public void setRight(R right) {
		this.right = right;
	}
	
	/**
	 * @return right value in pair
	 */
	public R getRight() {
		return right;
	}
}
