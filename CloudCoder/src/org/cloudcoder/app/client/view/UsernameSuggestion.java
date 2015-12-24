// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Username suggestion (for using a SuggestBox to suggest usernames.)
 * 
 * @author David Hovemeyer
 */
public class UsernameSuggestion implements SuggestOracle.Suggestion {
	private User user;
	
	/**
	 * Constructor.
	 * 
	 * @param user the {@link User} to suggest
	 */
	public UsernameSuggestion(User user) {
		this.user = user;
	}
	
	@Override
	public String getDisplayString() {
		return user.getUsername() + " (" + user.getFirstname() + " " + user.getLastname() + ")";
	}
	
	@Override
	public String getReplacementString() {
		return user.getUsername();
	}
}