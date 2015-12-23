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
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.client.validator.SuggestBoxNonEmptyValidator;
import org.cloudcoder.app.client.validator.TextBoxIntegerValidator;
import org.cloudcoder.app.client.validator.TextBoxNonemptyValidator;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseCreationSpec;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;

/**
 * UI for creating a course.
 * Creates a {@link CourseCreationSpec} object.
 * 
 * @author David Hovemeyer
 */
public class CreateCoursePanel extends ValidatedFormUI implements SessionObserver, Subscriber {
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
	
	private Runnable onCreateCourse;
	
	private Session session;
	
	private ListBox termListBox;
	private TextBox yearTextBox;
	private TextBox nameTextBox;
	private TextBox titleTextBox;
	private TextBox urlTextBox;
	private SuggestBox instructorSuggestBox;
	private TextBox sectionTextBox;
	private Button createCourseButton;

	/**
	 * Constructor.
	 */
	public CreateCoursePanel() {
		// Set a fixed height to allow this UI to be placed in an
		// AccordionPanel
		getPanel().setHeight("320px");
		getPanel().setWidth("100%");
		
		double y = 10.0;
		
		this.termListBox = new ListBox();
		y = addWidget(y, termListBox, "Term:", new NoopFieldValidator());
		this.yearTextBox = new TextBox();
		y = addWidget(y, yearTextBox, "Year:", new TextBoxIntegerValidator());
		this.nameTextBox = new TextBox();
		y = addWidget(y, nameTextBox, "Course name:", new TextBoxNonemptyValidator("A course name is required"));
		this.titleTextBox = new TextBox();
		y = addWidget(y, titleTextBox, "Course title:", new TextBoxNonemptyValidator("A course title is required"));
		this.urlTextBox = new TextBox();
		y = addWidget(y, urlTextBox, "Course URL:", new NoopFieldValidator());
		this.instructorSuggestBox = new SuggestBox(new UsernameSuggestOracle());
		y = addWidget(y, instructorSuggestBox, "Instructor username:", new SuggestBoxNonEmptyValidator("An instructor username is required"));
		this.sectionTextBox = new TextBox();
		y = addWidget(y, sectionTextBox, "Section:", new TextBoxIntegerValidator());
		this.createCourseButton = new Button("Create course");
		y = addWidget(y, createCourseButton, "", new NoopFieldValidator());
		this.createCourseButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (onCreateCourse != null) {
					onCreateCourse.run();
				}
			}
		});
	}
	
	/**
	 * Set callback to run when the Create course button is clicked.
	 * 
	 * @param onCreateCourse the callback
	 */
	public void setOnCreateCourse(Runnable onCreateCourse) {
		this.onCreateCourse = onCreateCourse;
	}
	
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
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
	
	/**
	 * Get a {@link CourseCreationSpec} populated from the
	 * form fields.  Assumes that {@link #validate()} has been called
	 * and returned true.
	 * 
	 * @return the populated {@link CourseCreationSpec}
	 */
	public CourseCreationSpec getCourseCreationSpec() {
		CourseCreationSpec spec = new CourseCreationSpec();
		
		Course course = new Course();
		Term[] terms = session.get(Term[].class);
		Term term = terms[termListBox.getSelectedIndex()];
		course.setYear(Integer.parseInt(yearTextBox.getText().trim()));
		course.setTermId(term.getId());
		course.setName(nameTextBox.getText().trim());
		course.setTitle(titleTextBox.getText().trim());
		course.setUrl(urlTextBox.getText().trim());
		
		spec.setCourse(course);
		
		spec.setUsername(instructorSuggestBox.getText());
		
		spec.setSection(Integer.parseInt(sectionTextBox.getText()));
		
		return spec;
	}

	private void populateTerms(Term[] terms) {
		termListBox.clear();
		for (Term term : terms) {
			termListBox.addItem(term.getName());
		}
	}

	public void clear() {
		termListBox.setSelectedIndex(0);
		yearTextBox.setText("");
		nameTextBox.setText("");
		titleTextBox.setText("");
		urlTextBox.setText("");
		instructorSuggestBox.setText("");
		sectionTextBox.setText("");
	}
}
