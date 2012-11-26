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

package org.cloudcoder.builder2.model;

import java.util.Stack;

/**
 * Stack of {@link ICleanupAction}s.
 * 
 * @author David Hovemeyer
 */
public class CleanupActionStack {
	private Stack<ICleanupAction> stack;
	
	/**
	 * Constructor.
	 */
	public CleanupActionStack() {
		stack = new Stack<ICleanupAction>();
	}
	
	/**
	 * Push an {@link ICleanupAction} onto the stack.
	 * 
	 * @param cleanupAction the {@link ICleanupAction} to push
	 */
	public void push(ICleanupAction cleanupAction) {
		stack.push(cleanupAction);
	}
	
	/**
	 * Execute all of the {@link ICleanupAction}s on the stack
	 * (starting with the most recent and working backwards).
	 */
	public void executeAll() {
		while (!stack.isEmpty()) {
			ICleanupAction cleanupAction = stack.pop();
			cleanupAction.execute();
		}
	}
}
