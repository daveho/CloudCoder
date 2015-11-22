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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;

/**
 * UI for creating a course.
 * 
 * @author David Hovemeyer
 */
public class CreateCoursePanel extends Composite implements SessionObserver, Subscriber {
	private static class UsernameSuggestion implements SuggestOracle.Suggestion {
		private User user;
		
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
	
	private static class UsernameSuggestOracle extends SuggestOracle {
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
	
	private static final double FIELD_HEIGHT_PX = 28.0;
	private static final double FIELD_PADDING_PX = 8.0;
	
	private Course course;
	
	private ListBox termListBox;
	private TextBox yearTextBox;
	private TextBox nameTextBox;
	private TextBox titleTextBox;
	private TextBox urlTextBox;
	private SuggestBox instructorSuggestBox;
	private TextBox sectionTextBox;
	
	public CreateCoursePanel() {
		this.course = new Course();
		
		LayoutPanel panel = new LayoutPanel();
		
		// Set a fixed height to allow this UI to be placed in an
		// AccordionPanel
		panel.setHeight("480px");
		panel.setWidth("100%");
		
		double y = 10.0;
		
		this.termListBox = new ListBox();
		y = addWidget(y, panel, termListBox, "Term:");
		this.yearTextBox = new TextBox();
		y = addWidget(y, panel, yearTextBox, "Year:");
		this.nameTextBox = new TextBox();
		y = addWidget(y, panel, nameTextBox, "Course name:");
		this.titleTextBox = new TextBox();
		y = addWidget(y, panel, titleTextBox, "Course title:");
		this.urlTextBox = new TextBox();
		y = addWidget(y, panel, urlTextBox, "Course URL:");
		this.instructorSuggestBox = new SuggestBox(new UsernameSuggestOracle());
		y = addWidget(y, panel, instructorSuggestBox, "Instructor username:");
		this.sectionTextBox = new TextBox();
		y = addWidget(y, panel, sectionTextBox, "Section:");
		
		initWidget(panel);
	}
	
	private double addWidget(double y, LayoutPanel panel, IsWidget widget, String labelText) {
		InlineLabel label = new InlineLabel(labelText);
		label.setStyleName("cc-rightJustifiedLabel", true);
		panel.add(label);
		panel.setWidgetTopHeight(label, y+8, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		panel.setWidgetLeftWidth(label, 20.0, Unit.PX, 120.0, Unit.PX);

		panel.add(widget);
		panel.setWidgetTopHeight(widget, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		panel.setWidgetLeftWidth(widget, 160.0, Unit.PX, 320.0, Unit.PX);
		
		return y + FIELD_HEIGHT_PX + FIELD_PADDING_PX;
	}
	
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		// Get terms
		if (session.get(Term[].class) == null) {
			RPC.getCoursesAndProblemsService.getTerms(new AsyncCallback<Term[]>() {
				@Override
				public void onSuccess(Term[] result) {
					GWT.log("Adding " + result.length + " Terms to session");
					session.add(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					session.add(StatusMessage.error("Could not get terms", caught));
				}
			});
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof Term[]) {
			populateTerms((Term[])hint);
		}
	}

	private void populateTerms(Term[] terms) {
		termListBox.clear();
		for (Term term : terms) {
			termListBox.addItem(term.getName());
		}
	}
}
