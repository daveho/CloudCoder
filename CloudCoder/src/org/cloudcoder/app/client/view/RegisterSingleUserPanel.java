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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.validator.IFieldValidator;
import org.cloudcoder.app.client.validator.IValidationCallback;
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.client.validator.TextBoxIntegerValidator;
import org.cloudcoder.app.client.validator.TextBoxNonemptyValidator;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author jspacco
 *
 */
public class RegisterSingleUserPanel extends Composite implements SessionObserver, Subscriber
{

    private static final double FIELD_HEIGHT_PX = 28.0;
    private static final double FIELD_PADDING_PX = 8.0;

    private TextBox usernameBox;
    private TextBox firstnameBox;
    private TextBox lastnameBox;
    private TextBox emailBox;
    private TextBox sectionBox;
    private PasswordTextBox passwordBox;
    private PasswordTextBox passwordVerifyBox;
    private ListBox registrationTypeBox;
    private Button registerSingleUserButton;
    
    private Session session;
    private List<IFieldValidator<? extends Widget>> validatorList;
    private List<IValidationCallback> validationCallbackList;
    private Runnable onRegisterUser;
    
    /**
     * 
     */
    public RegisterSingleUserPanel() {
        LayoutPanel panel = new LayoutPanel();
        
        this.validatorList = new ArrayList<IFieldValidator<? extends Widget>>();
        this.validationCallbackList = new ArrayList<IValidationCallback>();
        
        
        
        // Set a fixed height to allow this UI to be placed in an
        // AccordionPanel
        panel.setHeight("320px");
        panel.setWidth("100%");
        
        double y = 10.0;
        
        this.usernameBox = new TextBox();
        y = addWidget(y, panel, usernameBox, "Username:", new TextBoxNonemptyValidator("A username is required"));
        
        this.firstnameBox = new TextBox();
        y = addWidget(y, panel, firstnameBox, "First name:", new TextBoxNonemptyValidator("A first name is required"));
        this.lastnameBox = new TextBox();
        y = addWidget(y, panel, lastnameBox, "Last name:", new TextBoxNonemptyValidator("A last name is required"));
        this.emailBox = new TextBox();
        y = addWidget(y, panel, emailBox, "email:", new TextBoxNonemptyValidator("An email is required"));
        this.sectionBox=new TextBox();
        sectionBox.setValue("1");
        y = addWidget(y, panel, sectionBox, "section", new TextBoxIntegerValidator());
        
        
        // TODO: verify that the two passwords are the same BEFORE sending to the back-end
        this.passwordBox = new PasswordTextBox();
        y = addWidget(y, panel, passwordBox, "password:", new TextBoxNonemptyValidator("A password is required"));
        this.passwordVerifyBox = new PasswordTextBox();
        y = addWidget(y, panel, passwordVerifyBox, "password (verify):", new TextBoxNonemptyValidator("A password is required"));
        
        

        this.registrationTypeBox = new ListBox();
        for (CourseRegistrationType type : CourseRegistrationType.values()) {
            registrationTypeBox.addItem(type.name());
        }
        y = addWidget(y, panel, registrationTypeBox, "registration type:", new NoopFieldValidator());

        this.registerSingleUserButton=new Button("Register User");
        y = addWidget(y, panel, registerSingleUserButton, "", new NoopFieldValidator());

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
        
        initWidget(panel);
    }
    
    /**
     * Set callback to run when the Register User button is clicked.
     * 
     * @param onRegisterUser the callback
     */
    public void setOnRegisterSingleUser(Runnable onRegisterUser) {
        this.onRegisterUser= onRegisterUser;
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
    
    /**
     * Validate the form fields.
     * 
     * TODO: make sure the 2 password fields are the same
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
    public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
    {
        this.session = session;
        
        session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
        
        if (session.get(Course.class) == null) {
            // TODO: What if we don't have the course?
        }
        
    }
}
