// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Implementation of {@link ILoginView} to handle the case where
 * the client is "preauthorized" to log in to CloudCoder
 * as a specified user.  In this case, the user just clicks
 * a button to log in (and doesn't need to provide any
 * specific credentials).
 * 
 * @author David Hovemeyer
 */
public class PreauthorizedUserLoginView extends Composite implements ILoginView {
	
	private LoginSpec loginSpec;
	private InlineLabel preauthLoginLabel;
	private InlineLabel errorLabel;
	private InlineLabel loggingInLabel;
	private Button loginButton;
	private Runnable callback;

	public PreauthorizedUserLoginView() {
		LayoutPanel panel = new LayoutPanel();
		
		preauthLoginLabel = new InlineLabel("");
		panel.add(preauthLoginLabel);
		panel.setWidgetLeftWidth(preauthLoginLabel, 57.0, Unit.PX, 240.0, Unit.PX);
		panel.setWidgetTopHeight(preauthLoginLabel, 72.0, Unit.PX, 20.0, Unit.PX);
		
		InlineLabel promptLabel = new InlineLabel("Press the \"Log in\" button");
		panel.add(promptLabel);
		panel.setWidgetLeftWidth(promptLabel, 57.0, Unit.PX, 240.0, Unit.PX);
		panel.setWidgetTopHeight(promptLabel, 117.0, Unit.PX, 20.0, Unit.PX);
		
		InlineLabel promptLabel2 = new InlineLabel("to log into CloudCoder.");
		panel.add(promptLabel2);
		panel.setWidgetLeftWidth(promptLabel2, 57.0, Unit.PX, 240.0, Unit.PX);
		panel.setWidgetTopHeight(promptLabel2, 139.0, Unit.PX, 20.0, Unit.PX);
		
		loginButton = new Button("Log in");
		panel.add(loginButton);
		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				attemptLogin();
			}
		});
		panel.setWidgetLeftWidth(loginButton, 57.0, Unit.PX, 200.0, Unit.PX);
		panel.setWidgetTopHeight(loginButton, 180.0, Unit.PX, 27.0, Unit.PX);
		
		loggingInLabel = new InlineLabel("");
		panel.add(loggingInLabel);
		panel.setWidgetLeftWidth(loggingInLabel, 57.0, Unit.PX, 343.0, Unit.PX);
		panel.setWidgetTopHeight(loggingInLabel, 293.0, Unit.PX, 20.0, Unit.PX);

		errorLabel = new InlineLabel("");
		errorLabel.setStylePrimaryName("cc-errorText");
		panel.add(errorLabel);
		panel.setWidgetLeftWidth(errorLabel, 57.0, Unit.PX, 484.0, Unit.PX);
		panel.setWidgetTopHeight(errorLabel, 323.0, Unit.PX, 73.0, Unit.PX);
		
		initWidget(panel);
	}
	
	private void attemptLogin() {
		callback.run();
	}

	@Override
	public void setLoginSpec(LoginSpec loginSpec) {
		this.loginSpec = loginSpec;
		preauthLoginLabel.setText("Welcome, " + loginSpec.getPreAuthorizedUsername() + "!");
	}

	@Override
	public String getUsername() {
		return loginSpec.getPreAuthorizedUsername();
	}

	@Override
	public String getPassword() {
		return "---"; // doesn't matter what we return here
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

	@Override
	public void activate() {
		loginButton.setFocus(true);
	}

}
