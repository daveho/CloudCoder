// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;

/**
 * Dialog box to show when an RPC call has failed because the user's
 * session has timed out.  Allows user to log in again and retry
 * the failed operation. 
 * 
 * @author David Hovemeyer
 */
public class SessionExpiredDialogBox extends DialogBox {
	private PasswordTextBox passwordBox;
	private Button loginButton;
	private HTML errorLabel;
	private Runnable loginButtonHandler;
	
	/**
	 * Constructor.
	 */
	public SessionExpiredDialogBox() {
		setTitle("Session has timed out");
		setGlassEnabled(true);

		FlowPanel panel = new FlowPanel();
		
		panel.add(new HTML("<p>Your session has timed out.  Please enter<br />" +
				"your password to log back in.</p>"));
		
		FlowPanel passwordBoxAndLoginButtonPanel = new FlowPanel();
		
		passwordBoxAndLoginButtonPanel.add(new InlineLabel("Password: "));
		
		passwordBox = new PasswordTextBox();
		passwordBox.setWidth("120px");
		passwordBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					attemptLogin();
				}
			}
		});
		passwordBoxAndLoginButtonPanel.add(passwordBox);
		
		passwordBoxAndLoginButtonPanel.add(new InlineHTML("&nbsp;"));
		
		loginButton = new Button("Log in");
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				attemptLogin();
			}
		});
		passwordBoxAndLoginButtonPanel.add(loginButton);
		
		errorLabel = new HTML("");
		errorLabel.setStyleName("cc-errorText", true);
		errorLabel.setHeight("24px"); // force the label to be visible even though initially blank
		passwordBoxAndLoginButtonPanel.add(errorLabel);
		
		panel.add(passwordBoxAndLoginButtonPanel);

		add(panel);
	}

	private void attemptLogin() {
		if (loginButtonHandler != null) {
			loginButtonHandler.run();
		}
	}
	
	/**
	 * Get the value entered in the password box.
	 * 
	 * @return value entered in the password box
	 */
	public String getPassword() {
		return passwordBox.getText();
	}
	
	/**
	 * Set an error message in the error label.
	 * 
	 * @param errorMessage the error message to set
	 */
	public void setError(String errorMessage) {
		errorLabel.setText(errorMessage);
	}
	
	/**
	 * Set a callback to run when the login button is clicked.
	 * 
	 * @param loginButtonHandler callback to run when login button is clicked
	 */
	public void setLoginButtonHandler(Runnable loginButtonHandler) {
		this.loginButtonHandler = loginButtonHandler;
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#center()
	 */
	@Override
	public void center() {
		super.center();
		passwordBox.setFocus(true);
	}
}
