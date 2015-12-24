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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * SuggestOracle implementation for suggesting usernames.
 * 
 * @author David Hovemeyer
 */
public class UsernameSuggestOracle extends SuggestOracle {
	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		String q = request.getQuery();
		if (q.length() > 0) {
			RPC.usersService.suggestUsernames(q, new AsyncCallback<User[]>() {
				@Override
				public void onFailure(Throwable caught) {
					// Don't report the error: we just won't have
					// autocompletions
				}
				
				@Override
				public void onSuccess(User[] result) {
					SuggestOracle.Response response = new SuggestOracle.Response();
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (User user: result) {
						suggestions.add(new UsernameSuggestion(user));
					}
					response.setSuggestions(suggestions);
					callback.onSuggestionsReady(request, response);
				}
			});
		}
	}
}
