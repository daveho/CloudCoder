// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.client.model;

import org.cloudcoder.app.client.page.CloudCoderPage;

/**
 * Abstract identifier for a {@link CloudCoderPage}.
 * Used by {@link PageStack} to keep track of navigation
 * history.  Also specifies the fragment
 * name that identifies a {@link CloudCoderPage}
 * when used in the fragment part of a URL.
 * 
 * @author David Hovemeyer
 */
public enum PageId {
	/** CloudCoder initialization error. */
	INIT_ERROR("error"),
	
	/** Login page. */
	LOGIN("login"),

	/** Default home page showing courses and problems. */
	COURSES_AND_PROBLEMS("home"),
	
	/** The development page (edit code, submit, etc.) */
	DEVELOPMENT("exercise"),
	
	/** The problem admin page. */
	PROBLEM_ADMIN("exercises"),
	
	/** The edit problem page. */
	EDIT_PROBLEM("edit"),
	
	/** The user admin page. */
	USER_ADMIN("users"),
	
	/** User account page. */
	USER_ACCOUNT("account"),
	
	/** Statistics (all users progress) page. */
	STATISTICS("stats"),
	
	/** The (individual) user progress page. */
	USER_PROGRESS("progress"),
	
	/** Quiz administration page. */
	QUIZ("quiz"),
	
	/** View a selected user's submissions for a given problem. */
	USER_PROBLEM_SUBMISSIONS("submissions"),
	
	/** The development page for writing and executing code 
	 * not attached to particular exercises. */
	PLAYGROUND_PAGE("playground");
	
	private PageId(String fragmentName) {
		this.fragmentName = fragmentName;
	}
	
	private final String fragmentName;
	
	/**
	 * Get the URL fragment name identifying this page.
	 * 
	 * @return the fragment name
	 */
	public String getFragmentName() {
		return fragmentName;
	}
	
	/**
	 * Get the {@link PageId} corresponding to the given fragment name.
	 * 
	 * @param fragmentName the fragment name
	 * @return the {@link PageId}, or null if the fragment name
	 *         does not match any known {@link PageId}
	 */
	public static PageId forFragmentName(String fragmentName) {
		for (PageId id : values()) {
			if (id.getFragmentName().equals(fragmentName)) {
				return id;
			}
		}
		return null;
	}
}
