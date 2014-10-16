
// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Shane Bonner
 */
public class UserAccountView2 extends ResizeComposite implements Subscriber, SessionObserver
{
	private Session session;
	private LayoutPanel panel;
	private Label passwordLabel;
	private Label usernameLabel;
	private Label firstNameLabel;
	private Label lastNameLabel;
	private Label emailLabel;
	private Label passwordCheckLabel;
	private TextBox firstnameTextBox;
	private TextBox lastnameTextBox;
	private TextBox emailTextBox;
	private PasswordTextBox passwordTextBox;
	private PasswordTextBox passwordCheckBox;
	private User user;

	/**
	 * Constructor.
	 */
	public UserAccountView2() {

		panel = new LayoutPanel();

		panel.add(new HTML(new SafeHtmlBuilder().appendEscapedLines("Change the fields you want to edit.\n" +
				"Any fields left blank will be unchanged\n\n").toSafeHtml()));

		usernameLabel = new Label("");
		panel.add(usernameLabel);
		panel.setWidgetLeftWidth(usernameLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(usernameLabel, 50.0, Unit.PX, 200.0, Unit.PX);


		firstNameLabel = new Label("first name");
		panel.add(firstNameLabel);
		panel.setWidgetLeftWidth(firstNameLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(firstNameLabel, 70.0, Unit.PX, 200.0, Unit.PX);

		//first name text box
		firstnameTextBox = new TextBox();
		panel.add(firstnameTextBox);
		panel.setWidgetLeftWidth(firstnameTextBox, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(firstnameTextBox, 85.0, Unit.PX, 32.0, Unit.PX);

		lastNameLabel = new Label("last name");
		panel.add(lastNameLabel);
		panel.setWidgetLeftWidth(lastNameLabel, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(lastNameLabel, 130.0, Unit.PX, 200.0, Unit.PX);

		//last name text box
		lastnameTextBox = new TextBox();
		panel.add(lastnameTextBox);
		panel.setWidgetLeftWidth(lastnameTextBox, 20.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(lastnameTextBox, 145.0, Unit.PX, 32.0, Unit.PX);

		emailLabel = new Label("email");
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
		
		initWidget(panel);


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
		if (course != null) {
			GWT.log("Can't view user progress because no course is selected");
			return;
		}
		session.get(PageStack.class).push(PageId.USER_PROGRESS); //******USE THIS TO NAV BETWEEN PAGES****//
	}

	protected void handleEditButtonClick() {
		//This is more like a fake form
		//we're not submitting it to a server-side servlet
		GWT.log("edit user submit clicked");
		final User user = session.get(User.class);
		final CourseSelection course = session.get(CourseSelection.class);


		if (user.getFirstname().equals(firstnameTextBox.getValue()) ||
				user.getLastname().equals(lastnameTextBox.getValue()) ||
				user.getEmail().equals(emailTextBox.getValue()) ||
				passwordTextBox.getValue().length()>0)
		{
			if (!passwordTextBox.getValue().equals(passwordCheckBox.getValue())) {
				// TODO: User Daveho's warning system
				Window.alert("Passwords do no match");
				return;
			}
			if (passwordTextBox.getValue().length()==60) {
				Window.alert("Passwords cannot be 60 characters long");
				return;
			}
			// set the new fields to be saved into the DB
			user.setFirstname(firstnameTextBox.getValue());
			user.setLastname(lastnameTextBox.getValue());
			user.setEmail(emailTextBox.getValue());
			//user.setConsent(consent);
			if (passwordTextBox.getValue().length()>0) {
				user.setPasswordHash(passwordTextBox.getValue());
			}
			// at least one field was edited
			GWT.log("user id is "+user.getId());
			GWT.log("username from the session is "+user.getUsername());
			RPC.usersService.editUser(user,
					new AsyncCallback<Boolean>()
					{ 
				@Override
				public void onSuccess(Boolean result) {
					session.add(StatusMessage.goodNews("Edited "+user.getUsername()+" in course "+ course.getCourse().getNameAndTitle()));
				}

				@Override
				public void onFailure(Throwable caught) {
					GWT.log("Failed to edit student");
					session.add(StatusMessage.error("Unable to edit "+user.getUsername()+" in course "+ course.getCourse().getNameAndTitle()));
				}
					});
		} else {
			session.add(StatusMessage.information("Nothing was changed"));
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
	{
		this.session = session;
		this.user = session.get(User.class);
		usernameLabel.setText("Username: " + user.getUsername());
		firstNameLabel.setText("First name: " + user.getFirstname());
		lastNameLabel.setText("Last name: " + user.getLastname());
		emailLabel.setText("Email: " + user.getEmail());

	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
	}
}
