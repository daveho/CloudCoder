// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.validator.CompositeValidator;
import org.cloudcoder.app.client.validator.MatchingTextBoxValidator;
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.client.validator.TextBoxIntegerValidator;
import org.cloudcoder.app.client.validator.TextBoxNonemptyValidator;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * UI for registering a single user.
 * 
 * @author Jaime Spacco
 */
public class RegisterSingleUserPanel extends ValidatedFormUI
		implements CourseInstructorUI, SessionObserver
{
    private TextBox usernameBox;
    private TextBox firstnameBox;
    private TextBox lastnameBox;
    private TextBox emailBox;
    private TextBox sectionBox;
    private PasswordTextBox passwordBox;
    private PasswordTextBox passwordVerifyBox;
    private ListBox registrationTypeBox;
    private Button registerSingleUserButton;
    
    private Runnable onRegisterUser;
    
    /**
     * Constructor.
     * 
     * @param page the {@link CloudCoderPage}
     */
    public RegisterSingleUserPanel() {
        // Set a fixed height to allow this UI to be placed in an
        // AccordionPanel
        getPanel().setHeight("360px");
        getPanel().setWidth("100%");
        
        double y = 10.0;
        
        this.usernameBox = new TextBox();
        y = addWidget(y, usernameBox, "Username:", new TextBoxNonemptyValidator("A username is required"));
        
        this.firstnameBox = new TextBox();
        y = addWidget(y, firstnameBox, "First name:", new TextBoxNonemptyValidator("A first name is required"));
        this.lastnameBox = new TextBox();
        y = addWidget(y, lastnameBox, "Last name:", new TextBoxNonemptyValidator("A last name is required"));
        this.emailBox = new TextBox();
        y = addWidget(y, emailBox, "Email:", new TextBoxNonemptyValidator("An email is required"));
        this.sectionBox=new TextBox();
        sectionBox.setValue("1");
        y = addWidget(y, sectionBox, "Section", new TextBoxIntegerValidator());
        
        // make sure the passwords match
        this.passwordBox = new PasswordTextBox();
        this.passwordVerifyBox = new PasswordTextBox();

        y = addWidget(y, passwordBox, "Password:",
        		new CompositeValidator<TextBox>()
        				.add(new TextBoxNonemptyValidator("A password is required"))
        				.add(new MatchingTextBoxValidator("Passwords must match", passwordVerifyBox))
        );
        y = addWidget(y, passwordVerifyBox, "Password (verify):", new TextBoxNonemptyValidator("A password is required"));
        
        this.registrationTypeBox = new ListBox();
        for (CourseRegistrationType type : CourseRegistrationType.values()) {
            registrationTypeBox.addItem(type.name());
        }
        y = addWidget(y, registrationTypeBox, "Registration type:", new NoopFieldValidator());

        this.registerSingleUserButton=new Button("Register User");
        y = addWidget(y, registerSingleUserButton, "", new NoopFieldValidator());

        // Trigger creation of a Single user using RPC call to backend
        this.registerSingleUserButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (onRegisterUser != null) {
                    onRegisterUser.run();
                } else {
                    // TODO: log error message
                }
            }
        });
    }
    
    @Override
    public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		// Keep track of changes to instructor status
		new CourseInstructorStatusMonitor(this).activate(session, subscriptionRegistrar);
    }
    
    /**
     * Set callback to run when the Register User button is clicked.
     * 
     * @param onRegisterUser the callback
     */
    public void setOnRegisterSingleUser(Runnable onRegisterUser) {
        this.onRegisterUser= onRegisterUser;
    }
    
    /**
     * Get a {@link EditedUser} populated from the
     * form fields.  Assumes that {@link #validate()} has been called
     * and returned true.
     * 
     * @return the populated {@link EditedUser}
     */
    public EditedUser getEditedUser() {
        EditedUser editedUser=new EditedUser();
        User user=new User();
        user.setUsername(usernameBox.getText().trim());
        user.setFirstname(firstnameBox.getText().trim());
        user.setLastname(lastnameBox.getText().trim());
        user.setEmail(emailBox.getText().trim());
        
        // For creating an existing user account, the initial password
        // is set in the EditedUser object, not the User object.
        // Just to be paranoid, however, we'll set everything that looks
        // remotely like a password :-)
        editedUser.setCurrentPassword(passwordBox.getText());
        editedUser.setPassword(passwordBox.getText()); // <-- this alone is sufficient
        user.setPasswordHash(passwordBox.getText());

        // TODO some way to set super-user permissions
        // Also, what are we doing with the website information?
        user.setSuperuser(false);
        user.setWebsite("");
        
        editedUser.setUser(user);
        CourseRegistrationType courseRegistrationType=
                CourseRegistrationType.valueOf(
                        registrationTypeBox.getItemText(registrationTypeBox.getSelectedIndex()));
        editedUser.setRegistrationType(courseRegistrationType);
        editedUser.setSection(Integer.parseInt(sectionBox.getText().trim()));
        
        return editedUser;
    }
    
    @Override
    public void clear() {
        usernameBox.setText("");
        firstnameBox.setText("");
        lastnameBox.setText("");
        emailBox.setText("");
        sectionBox.setText("1");
        passwordBox.setText("");
        passwordVerifyBox.setText("");
        registrationTypeBox.setSelectedIndex(0);
    }
    
    @Override
    public void onCourseChange(Course course) {
    	// Nothing special to do here
    }
    
    @Override
    public void setEnabled(boolean b) {
    	GWT.log("RegisterSingleUser: setting enabled to " + b);
    	usernameBox.setEnabled(b);
    	firstnameBox.setEnabled(b);
    	lastnameBox.setEnabled(b);
    	emailBox.setEnabled(b);
    	sectionBox.setEnabled(b);
    	passwordBox.setEnabled(b);
    	passwordVerifyBox.setEnabled(b);
    	registrationTypeBox.setEnabled(b);
    	registerSingleUserButton.setEnabled(b);
    }
}
