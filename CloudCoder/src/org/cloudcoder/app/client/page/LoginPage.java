package org.cloudcoder.app.client.page;

import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

public class LoginPage extends LayoutPanel {
	public LoginPage() {
		setSize("640px", "480px");
		
		InlineLabel usernameLabel = new InlineLabel("User name:");
		add(usernameLabel);
		setWidgetLeftWidth(usernameLabel, 57.0, Unit.PX, 91.0, Unit.PX);
		setWidgetTopHeight(usernameLabel, 127.0, Unit.PX, 15.0, Unit.PX);
		
		TextBox usernameTextBox = new TextBox();
		add(usernameTextBox);
		setWidgetLeftWidth(usernameTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
		setWidgetTopHeight(usernameTextBox, 148.0, Unit.PX, 31.0, Unit.PX);
		
		InlineLabel passwordLabel = new InlineLabel("Password:");
		add(passwordLabel);
		setWidgetLeftWidth(passwordLabel, 57.0, Unit.PX, 91.0, Unit.PX);
		setWidgetTopHeight(passwordLabel, 185.0, Unit.PX, 15.0, Unit.PX);
		
		PasswordTextBox passwordTextBox = new PasswordTextBox();
		add(passwordTextBox);
		setWidgetLeftWidth(passwordTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
		setWidgetTopHeight(passwordTextBox, 206.0, Unit.PX, 33.0, Unit.PX);
		
		Button loginButton = new Button("Log in");
		add(loginButton);
		setWidgetLeftWidth(loginButton, 176.0, Unit.PX, 81.0, Unit.PX);
		setWidgetTopHeight(loginButton, 245.0, Unit.PX, 27.0, Unit.PX);
		
		Image cloudCoderLogoImage = new Image(GWT.getModuleBaseURL() + "images/CloudCoderLogo-med.png");
		add(cloudCoderLogoImage);
		setWidgetLeftWidth(cloudCoderLogoImage, 301.0, Unit.PX, 240.0, Unit.PX);
		setWidgetTopHeight(cloudCoderLogoImage, 148.0, Unit.PX, 165.0, Unit.PX);
		
		InlineLabel promptLabel = new InlineLabel("Please enter your username and password.");
		add(promptLabel);
		setWidgetLeftWidth(promptLabel, 57.0, Unit.PX, 343.0, Unit.PX);
		setWidgetTopHeight(promptLabel, 328.0, Unit.PX, 40.0, Unit.PX);
		
		InlineLabel errorLabel = new InlineLabel("");
		add(errorLabel);
		setWidgetLeftWidth(errorLabel, 57.0, Unit.PX, 484.0, Unit.PX);
		setWidgetTopHeight(errorLabel, 374.0, Unit.PX, 73.0, Unit.PX);
	}
}
