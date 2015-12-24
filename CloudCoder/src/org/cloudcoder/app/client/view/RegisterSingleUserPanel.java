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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.validator.MatchingTextBoxValidator;
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.client.validator.TextBoxIntegerValidator;
import org.cloudcoder.app.client.validator.TextBoxNonemptyValidator;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * UI for registering a single user.
 * 
 * @author Jaime Spacco
 */
public class RegisterSingleUserPanel extends ValidatedFormUI implements SessionObserver, Subscriber
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
    // currently we never use the session, but we activate it
    private Session session;
    
    private Runnable onRegisterUser;
    
    /**
     * 
     */
    public RegisterSingleUserPanel() {
        super(new LayoutPanel());
        
        // Set a fixed height to allow this UI to be placed in an
        // AccordionPanel
        getPanel().setHeight("320px");
        getPanel().setWidth("100%");
        
        double y = 10.0;
        
        this.usernameBox = new TextBox();
        y = addWidget(y, usernameBox, "Username:", new TextBoxNonemptyValidator("A username is required"));
        
        this.firstnameBox = new TextBox();
        y = addWidget(y, firstnameBox, "First name:", new TextBoxNonemptyValidator("A first name is required"));
        this.lastnameBox = new TextBox();
        y = addWidget(y, lastnameBox, "Last name:", new TextBoxNonemptyValidator("A last name is required"));
        this.emailBox = new TextBox();
        y = addWidget(y, emailBox, "email:", new TextBoxNonemptyValidator("An email is required"));
        this.sectionBox=new TextBox();
        sectionBox.setValue("1");
        y = addWidget(y, sectionBox, "section", new TextBoxIntegerValidator());
        
        // make sure the passwords match
        this.passwordBox = new PasswordTextBox();
        this.passwordVerifyBox = new PasswordTextBox();

        y = addWidget(y, passwordBox, "password:",
        		new CompositeValidator<TextBox>()
        				.add(new TextBoxNonemptyValidator("A password is required"))
        				.add(new MatchingTextBoxValidator("passwords must match", passwordVerifyBox))
        );
        y = addWidget(y, passwordVerifyBox, "password (verify):", new TextBoxNonemptyValidator("A password is required"));
        
        this.registrationTypeBox = new ListBox();
        for (CourseRegistrationType type : CourseRegistrationType.values()) {
            registrationTypeBox.addItem(type.name());
        }
        y = addWidget(y, registrationTypeBox, "registration type:", new NoopFieldValidator());

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
        // We send the password in the clear from the client side
        // and the hash is created on the server side in the DB backend code
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
    public void eventOccurred(Object key, Publisher publisher, Object hint) {
        // TODO Auto-generated method stub
        
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
    public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
    {
        this.session = session;
        session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
    }
}
