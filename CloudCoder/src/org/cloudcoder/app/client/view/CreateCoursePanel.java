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
import org.cloudcoder.app.client.validator.IFieldValidator;
import org.cloudcoder.app.client.validator.IValidationCallback;
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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI for creating a course.
 * Creates a {@link CourseCreationSpec} object.
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
	
	private Runnable onCreateCourse;
	
	private Session session;
	
	private List<IFieldValidator<? extends Widget>> validatorList;
	private List<IValidationCallback> validationCallbackList;
	
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
		this.validatorList = new ArrayList<IFieldValidator<? extends Widget>>();
		this.validationCallbackList = new ArrayList<IValidationCallback>();
		
		LayoutPanel panel = new LayoutPanel();
		
		// Set a fixed height to allow this UI to be placed in an
		// AccordionPanel
		panel.setHeight("320px");
		panel.setWidth("100%");
		
		double y = 10.0;
		
		this.termListBox = new ListBox();
		y = addWidget(y, panel, termListBox, "Term:", new NoopFieldValidator());
		this.yearTextBox = new TextBox();
		y = addWidget(y, panel, yearTextBox, "Year:", new TextBoxIntegerValidator());
		this.nameTextBox = new TextBox();
		y = addWidget(y, panel, nameTextBox, "Course name:", new TextBoxNonemptyValidator("A course name is required"));
		this.titleTextBox = new TextBox();
		y = addWidget(y, panel, titleTextBox, "Course title:", new TextBoxNonemptyValidator("A course title is required"));
		this.urlTextBox = new TextBox();
		y = addWidget(y, panel, urlTextBox, "Course URL:", new NoopFieldValidator());
		this.instructorSuggestBox = new SuggestBox(new UsernameSuggestOracle());
		y = addWidget(y, panel, instructorSuggestBox, "Instructor username:", new SuggestBoxNonEmptyValidator("An instructor username is required"));
		this.sectionTextBox = new TextBox();
		y = addWidget(y, panel, sectionTextBox, "Section:", new TextBoxIntegerValidator());
		this.createCourseButton = new Button("Create course");
		y = addWidget(y, panel, createCourseButton, "", new NoopFieldValidator());
		this.createCourseButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (onCreateCourse != null) {
					onCreateCourse.run();
				}
			}
		});
		
		initWidget(panel);
	}
	
	/**
	 * Set callback to run when the Create course button is clicked.
	 * 
	 * @param onCreateCourse the callback
	 */
	public void setOnCreateCourse(Runnable onCreateCourse) {
		this.onCreateCourse = onCreateCourse;
	}
	
	private<E extends Widget> double addWidget(double y, LayoutPanel panel, final E widget, String labelText, IFieldValidator<E> validator) {
		InlineLabel label = new InlineLabel(labelText);
		label.setStyleName("cc-rightJustifiedLabel", true);
		panel.add(label);
		panel.setWidgetTopHeight(label, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		panel.setWidgetLeftWidth(label, 20.0, Unit.PX, 120.0, Unit.PX);

		panel.add(widget);
		panel.setWidgetTopHeight(widget, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		panel.setWidgetLeftWidth(widget, 160.0, Unit.PX, 320.0, Unit.PX);
		
		final InlineLabel validationErrorLabel = new InlineLabel();
		validationErrorLabel.setStyleName("cc-errorText", true);
		panel.add(validationErrorLabel);
		panel.setWidgetTopHeight(validationErrorLabel, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		panel.setWidgetLeftRight(validationErrorLabel, 500.0, Unit.PX, 0.0, Unit.PX);
		
		validatorList.add(validator);
		validator.setWidget(widget);
		
		IValidationCallback callback = new IValidationCallback() {
			@Override
			public void onSuccess() {
				validationErrorLabel.setText("");
				widget.removeStyleName("cc-invalid");
			}
			
			@Override
			public void onFailure(String msg) {
				validationErrorLabel.setText(msg);
				widget.setStyleName("cc-invalid", true);
			}
		};
		validationCallbackList.add(callback);
		
		return y + FIELD_HEIGHT_PX + FIELD_PADDING_PX;
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
	 * Validate the form fields.
	 * 
	 * @return true if all fields successfully validated, false otherwise
	 */
	public boolean validate() {
		int numFailures = 0;
		for (int i = 0; i < validatorList.size(); i++) {
			IFieldValidator<? extends Widget> validator = validatorList.get(i);
			IValidationCallback callback = validationCallbackList.get(i);
			if (!validator.validate(callback)) {
				numFailures++;
			}
		}
		return numFailures == 0;
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

	/**
	 * Clear field values.
	 */
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
