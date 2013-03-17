// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.PageId;

/**
 * Stack of {@link PageId}s representing the user's navigation
 * history.
 * 
 * @author David Hovemeyer
 */
public class PageStack {
	private List<PageId> stack;
	
	/**
	 * Constructor.
	 */
	public PageStack() {
		this.stack = new ArrayList<PageId>();
	}
	
	/**
	 * Push a new page.
	 * 
	 * @param cls the {@link PageId} of the page to push
	 */
	public void push(PageId cls) {
		stack.add(cls);
	}
	
	/**
	 * Pop a page.
	 * 
	 * @return the {@link PageId} of the popped page
	 */
	public PageId pop() {
		int last = stack.size() - 1;
		return stack.remove(last);
	}
	
	/**
	 * @return true if the page stack is empty, false otherwise
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}
}
