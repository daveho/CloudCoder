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

import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.client.validator.SuggestBoxNonEmptyValidator;
import org.cloudcoder.app.client.validator.TextBoxIntegerValidator;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationSpec;
import org.cloudcoder.app.shared.model.CourseRegistrationType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Form UI for adding a single existing user to a course.
 * 
 * @author David Hovemeyer
 */
public class RegisterExistingUserPanel extends CourseInstructorFormUI {
	private SuggestBox usernameBox;
	private TextBox sectionBox;
	private ListBox registrationTypeBox;
	private Button registerUserButton;
	private Runnable onRegisterUser;
	private int courseId;

	/**
	 * Constructor.
	 */
	public RegisterExistingUserPanel(CloudCoderPage page) {
		super(page);
		
		getPanel().setWidth("100%");
		getPanel().setHeight("180px");
		
		double y = 10.0;
		
		this.usernameBox = new SuggestBox(new UsernameSuggestOracle());
		y = addWidget(y, usernameBox, "Username:", new SuggestBoxNonEmptyValidator("Username is required"));
		
		this.sectionBox = new TextBox();
		y = addWidget(y, sectionBox, "Section:", new TextBoxIntegerValidator());
		
		this.registrationTypeBox = new ListBox();
		registrationTypeBox.setVisibleItemCount(1);
		y = addWidget(y, registrationTypeBox, "Registration type:", new NoopFieldValidator());
		for (CourseRegistrationType registrationType : CourseRegistrationType.values()) {
			registrationTypeBox.addItem(registrationType.toString());
		}
		
		this.registerUserButton = new Button("Register User");
		y = addWidget(y, registerUserButton, "", new NoopFieldValidator());
		
		registerUserButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (onRegisterUser != null) {
					onRegisterUser.run();
				}
			}
		});
	}
	
	/**
	 * Set the callback to be run when the "Register user" button is clicked.
	 * 
	 * @param callback the callback
	 */
	public void setOnRegisterUser(Runnable callback) {
		this.onRegisterUser = callback;
	}

	@Override
	protected void setEnabled(boolean b) {
		usernameBox.setEnabled(b);
		sectionBox.setEnabled(b);
		registerUserButton.setEnabled(b);
	}

	@Override
	protected void onCourseChange(Course course) {
		this.courseId = course.getId();
	}

	@Override
	public void clear() {
		usernameBox.setText("");
		sectionBox.setText("");
	}

	/**
	 * Return a {@link CourseRegistrationSpec} populated from current form
	 * data.  Assumes that {@link #validate()} has been called and returned true.
	 * 
	 * @return the {@link CourseRegistrationSpec}
	 */
	public CourseRegistrationSpec getCourseRegistrationSpec() {
		CourseRegistrationSpec spec = new CourseRegistrationSpec();
		
		spec.setUsername(usernameBox.getText());
		spec.setSection(Integer.parseInt(sectionBox.getText()));
		spec.setCourseId(courseId);
		spec.setRegistrationType(CourseRegistrationType.values()[registrationTypeBox.getSelectedIndex()]);
		
		return spec;
	}
}
