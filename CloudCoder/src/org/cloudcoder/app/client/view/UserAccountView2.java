// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, Shane Bonner
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

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.page.SessionUtil;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * User account page main view.
 * Users can change their passwords and view their progress
 * in their courses.
 * 
 * @author Shane Bonner
 * @author David Hovemeyer
 */
public class UserAccountView2 extends ResizeComposite implements Subscriber, SessionObserver
{
	private CloudCoderPage page;
	private Session session;
	private LayoutPanel panel;
	private ScrollPanel scrollPanel;
	//	private Label passwordLabel;
	//	private Label usernameLabel;
	//	private Label firstNameLabel;
	//	private Label lastNameLabel;
	//	private Label emailLabel;
	//	private Label passwordCheckLabel;
	//	private TextBox emailTextBox;
	private PasswordTextBox passwordTextBox;
	private PasswordTextBox passwordCheckBox;
//	private User user;
	private CourseSelectionListBox courseSelectionList;
	private Label userIdentityLabel;
	private Label passwordLabel;
	private Label passwordCheckLabel;
	private Button editPasswordButton;

	/**
	 * Constructor.
	 */
	public UserAccountView2(CloudCoderPage page) {
		this.page = page;
		
		panel = new LayoutPanel();

		/*
		panel.add(new HTML(new SafeHtmlBuilder().appendEscapedLines("Change the fields you want to edit.\n" +
				"Any fields left blank will be unchanged\n\n").toSafeHtml()));

		usernameLabel = new Label("");
		panel.add(usernameLabel);
		panel.setWidgetLeftWidth(usernameLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(usernameLabel, 50.0, Unit.PX, 200.0, Unit.PX);


		firstNameLabel = new Label("");
		panel.add(firstNameLabel);
		panel.setWidgetLeftWidth(firstNameLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(firstNameLabel, 70.0, Unit.PX, 200.0, Unit.PX);

		lastNameLabel = new Label("");
		panel.add(lastNameLabel);
		panel.setWidgetLeftWidth(lastNameLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(lastNameLabel, 130.0, Unit.PX, 200.0, Unit.PX);

		emailLabel = new Label("");
		panel.add(emailLabel);
		panel.setWidgetLeftWidth(emailLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(emailLabel, 185.0, Unit.PX, 200.0, Unit.PX);

		//email textbox
		emailTextBox = new TextBox();
		panel.add(emailTextBox);
		panel.setWidgetLeftWidth(emailTextBox, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(emailTextBox, 200.0, Unit.PX, 32.0, Unit.PX);

		passwordLabel = new Label("Enter a new password:");
		panel.add(passwordLabel);
		panel.setWidgetLeftWidth(passwordLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordLabel, 250.0, Unit.PX, 32.0, Unit.PX);

		passwordTextBox = new PasswordTextBox();
		panel.add(passwordTextBox);
		panel.setWidgetLeftWidth(passwordTextBox, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordTextBox, 265.0, Unit.PX, 32.0, Unit.PX);

		passwordCheckLabel = new Label("Re-enter password:");
		panel.add(passwordCheckLabel);
		panel.setWidgetLeftWidth(passwordCheckLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordCheckLabel, 300.0, Unit.PX, 32.0, Unit.PX);

		passwordCheckBox = new PasswordTextBox();
		panel.add(passwordCheckBox);
		panel.setWidgetLeftWidth(passwordCheckBox, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordCheckBox, 315.0, Unit.PX, 32.0, Unit.PX);


		Button editUserButton = new Button("Edit user", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleEditButtonClick();
			}
		});
		panel.add(editUserButton);
		panel.setWidgetLeftWidth(editUserButton, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(editUserButton, 350.0, Unit.PX, 32.0, Unit.PX);

		Button userProgressButton = new Button("User Progress", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event){
				handleProgressButtonClick();

			}
		});
		panel.add(userProgressButton);
		panel.setWidgetLeftWidth(userProgressButton, 275.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(userProgressButton, 350.0, Unit.PX, 32.0, Unit.PX);

		// TODO: create course list widget
		courseSelectionList = new CourseSelectionListBox();
		panel.add(courseSelectionList);
		panel.setWidgetLeftWidth(courseSelectionList, 500, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(courseSelectionList, 300, Unit.PX, 200.0, Unit.PX);
		 */
		
		final double top = 10.0;

		this.userIdentityLabel = new Label();
		userIdentityLabel.setStyleName("cc-userIdentity", true);
		panel.add(userIdentityLabel);
		panel.setWidgetLeftRight(userIdentityLabel, 40.0, Unit.PX, 0.0, Unit.PX);
		panel.setWidgetTopHeight(userIdentityLabel, top, Unit.PX, 24.0, Unit.PX);
		
		Label passwordChangeLabel = new Label("Change your password");
		passwordChangeLabel.setStyleName("cc-sectionLabel", true);
		panel.add(passwordChangeLabel);
		panel.setWidgetLeftWidth(passwordChangeLabel, 40.0, Unit.PX, 400.0, Unit.PX);
		panel.setWidgetTopHeight(passwordChangeLabel, top + 40.0, Unit.PX, 24.0, Unit.PX);
		
		passwordLabel = new Label("Enter a new password:");
		panel.add(passwordLabel);
		panel.setWidgetLeftWidth(passwordLabel, 40.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordLabel, top + 80.0, Unit.PX, 32.0, Unit.PX);

		passwordTextBox = new PasswordTextBox();
		panel.add(passwordTextBox);
		panel.setWidgetLeftWidth(passwordTextBox, 40.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordTextBox, top + 100.0, Unit.PX, 32.0, Unit.PX);

		passwordCheckLabel = new Label("Re-enter password:");
		panel.add(passwordCheckLabel);
		panel.setWidgetLeftWidth(passwordCheckLabel, 40.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordCheckLabel, top + 150.0, Unit.PX, 32.0, Unit.PX);

		passwordCheckBox = new PasswordTextBox();
		panel.add(passwordCheckBox);
		panel.setWidgetLeftWidth(passwordCheckBox, 40.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordCheckBox, top + 170.0, Unit.PX, 32.0, Unit.PX);
		
		editPasswordButton = new Button("Update password");
		panel.add(editPasswordButton);
		panel.setWidgetLeftWidth(editPasswordButton, 40.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(editPasswordButton, 240.0, Unit.PX, 32.0, Unit.PX);
		editPasswordButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleEditButtonClick();
			}
		});

		// Allow the view to scroll if necessary
		scrollPanel = new ScrollPanel();
		scrollPanel.add(panel);
		initWidget(scrollPanel);


	}

	protected void handleProgressButtonClick(){
		// The UserProgressPage requires a UserSelection object to be in the
		// session.  The idea here is that the User object in the session
		// represents the logged-in user, and the UserSelection object
		// represents the "selected" user (the user whose progress we
		// want to see.)  We are basically allowing the user to
		// see his/her own progress.

		UserSelection userSelection = new UserSelection(session.get(User.class));
		session.add(userSelection);

		final CourseSelection course = session.get(CourseSelection.class);
		if (course == null) {
			GWT.log("Can't view user progress because no course is selected");
			return;
		}
		session.get(PageStack.class).push(PageId.USER_PROGRESS); //******USE THIS TO NAV BETWEEN PAGES****//

	}

	protected void handleEditButtonClick() {
		GWT.log("edit user submit clicked");

		final User user = session.get(User.class);

		if (!passwordTextBox.getValue().equals(passwordCheckBox.getValue())) {
			session.add(StatusMessage.error("Passwords do not match"));
			return;
		}
		
		String plaintextPassword = passwordTextBox.getText().trim();
		if (plaintextPassword.equals("")) {
			session.add(StatusMessage.error("Cannot set an empty password"));
			return;
		}

		user.setPasswordHash(passwordTextBox.getValue());
		
		GWT.log("Attempting to update password for " + user.getUsername());
		SessionUtil.editUser(page, user, session, new Runnable() {
			@Override
			public void run() {
				// Password updated successfully: clear the password textboxes
				passwordTextBox.setText("");
				passwordCheckBox.setText("");
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
	{
		this.session = session;
//		this.user = session.get(User.class);
//		usernameLabel.setText("Username: " + user.getUsername());
//		firstNameLabel.setText("First name: " + user.getFirstname());
//		lastNameLabel.setText("Last name: " + user.getLastname());
//		emailLabel.setText("Email: " + user.getEmail());
		User user = session.get(User.class);
		userIdentityLabel.setText(user.getFirstname() + " " + user.getLastname() + " - " + user.getUsername());

//		// Activate the course selection list (allowing it to populate
//		// the list of courses the user is registered for)
//		courseSelectionList.activate(session, subscriptionRegistrar);

	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
	}
}
