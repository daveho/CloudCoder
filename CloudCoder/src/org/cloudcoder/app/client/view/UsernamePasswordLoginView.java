// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import org.cloudcoder.app.shared.model.LoginSpec;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A login view which allows the user to enter a username
 * and password.
 * 
 * @author David Hovemeyer
 */
public class UsernamePasswordLoginView extends Composite implements ILoginView {
	private TextBox usernameTextBox;
	private PasswordTextBox passwordTextBox;
	private InlineLabel errorLabel;
	private InlineLabel loggingInLabel;
	private LoginSpec loginSpec;
	private Runnable callback;

	public UsernamePasswordLoginView() {
		LayoutPanel panel = new LayoutPanel();
		
		InlineLabel promptLabel = new InlineLabel("Please enter your username and password.");
		panel.add(promptLabel);
		panel.setWidgetLeftWidth(promptLabel, 57.0, Unit.PX, 343.0, Unit.PX);
		panel.setWidgetTopHeight(promptLabel, 288.0, Unit.PX, 20.0, Unit.PX);
		
		InlineLabel usernameLabel = new InlineLabel("User name:");
		panel.add(usernameLabel);
		panel.setWidgetLeftWidth(usernameLabel, 57.0, Unit.PX, 91.0, Unit.PX);
		panel.setWidgetTopHeight(usernameLabel, 87.0, Unit.PX, 15.0, Unit.PX);
		
		usernameTextBox = new TextBox();
		usernameTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					passwordTextBox.setFocus(true);
				}
			}
		});
		panel.add(usernameTextBox);
		panel.setWidgetLeftWidth(usernameTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(usernameTextBox, 108.0, Unit.PX, 31.0, Unit.PX);
		
		InlineLabel passwordLabel = new InlineLabel("Password:");
		panel.add(passwordLabel);
		panel.setWidgetLeftWidth(passwordLabel, 57.0, Unit.PX, 91.0, Unit.PX);
		panel.setWidgetTopHeight(passwordLabel, 145.0, Unit.PX, 15.0, Unit.PX);
		
		passwordTextBox = new PasswordTextBox();
		passwordTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					attemptLogin();
				}
			}
		});
		panel.add(passwordTextBox);
		panel.setWidgetLeftWidth(passwordTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(passwordTextBox, 166.0, Unit.PX, 33.0, Unit.PX);
		
		Button loginButton = new Button("Log in");
		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				attemptLogin();
			}
		});
		panel.add(loginButton);
		panel.setWidgetLeftWidth(loginButton, 176.0, Unit.PX, 81.0, Unit.PX);
		panel.setWidgetTopHeight(loginButton, 205.0, Unit.PX, 27.0, Unit.PX);

		loggingInLabel = new InlineLabel("");
		panel.add(loggingInLabel);
		panel.setWidgetLeftWidth(loggingInLabel, 57.0, Unit.PX, 343.0, Unit.PX);
		panel.setWidgetTopHeight(loggingInLabel, 353.0, Unit.PX, 20.0, Unit.PX);

		errorLabel = new InlineLabel("");
		errorLabel.setStylePrimaryName("cc-errorText");
		panel.add(errorLabel);
		panel.setWidgetLeftWidth(errorLabel, 57.0, Unit.PX, 484.0, Unit.PX);
		panel.setWidgetTopHeight(errorLabel, 383.0, Unit.PX, 73.0, Unit.PX);

		initWidget(panel);
	}
	
	public void activate() {
		usernameTextBox.setFocus(true);
	}
	
	private void attemptLogin() {
		callback.run();
	}

	@Override
	public void setLoginSpec(LoginSpec loginSpec) {
		this.loginSpec = loginSpec;
	}

	@Override
	public String getUsername() {
		return usernameTextBox.getText();
	}

	@Override
	public String getPassword() {
		return passwordTextBox.getText();
	}

	@Override
	public void setLoginCallback(Runnable callback) {
		this.callback = callback;
	}
	
	@Override
	public void setInfoMessage(String infoMessage) {
		loggingInLabel.setText(infoMessage);
	}
	
	@Override
	public void setErrorMessage(String errorMessage) {
		errorLabel.setText(errorMessage);
	}
}
