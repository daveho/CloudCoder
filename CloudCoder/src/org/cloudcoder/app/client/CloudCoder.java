package org.cloudcoder.app.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CloudCoder implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
		
		InlineLabel usernameLabel = new InlineLabel("Username:");
		rootLayoutPanel.add(usernameLabel);
		rootLayoutPanel.setWidgetLeftWidth(usernameLabel, 50.0, Unit.PX, 91.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(usernameLabel, 101.0, Unit.PX, 15.0, Unit.PX);
		
		InlineLabel passwordLabel = new InlineLabel("Password:");
		rootLayoutPanel.add(passwordLabel);
		rootLayoutPanel.setWidgetLeftWidth(passwordLabel, 50.0, Unit.PX, 91.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(passwordLabel, 159.0, Unit.PX, 15.0, Unit.PX);
		
		TextBox usernameTextBox = new TextBox();
		rootLayoutPanel.add(usernameTextBox);
		rootLayoutPanel.setWidgetLeftWidth(usernameTextBox, 50.0, Unit.PX, 161.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(usernameTextBox, 122.0, Unit.PX, 31.0, Unit.PX);
		
		TextBox passwordTextBox = new TextBox();
		rootLayoutPanel.add(passwordTextBox);
		rootLayoutPanel.setWidgetLeftWidth(passwordTextBox, 50.0, Unit.PX, 161.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(passwordTextBox, 180.0, Unit.PX, 31.0, Unit.PX);
		
		Button logInButton = new Button("Log in");
		rootLayoutPanel.add(logInButton);
		rootLayoutPanel.setWidgetLeftWidth(logInButton, 50.0, Unit.PX, 81.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(logInButton, 217.0, Unit.PX, 27.0, Unit.PX);
		
		InlineLabel promptLabel = new InlineLabel("Please enter your username and password.");
		rootLayoutPanel.add(promptLabel);
		rootLayoutPanel.setWidgetLeftWidth(promptLabel, 50.0, Unit.PX, 358.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(promptLabel, 270.0, Unit.PX, 15.0, Unit.PX);
		
		InlineLabel errorMessageLabel = new InlineLabel("");
		rootLayoutPanel.add(errorMessageLabel);
		rootLayoutPanel.setWidgetLeftWidth(errorMessageLabel, 50.0, Unit.PX, 530.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(errorMessageLabel, 316.0, Unit.PX, 15.0, Unit.PX);
		
		Image image = new Image("cloudcoder/images/CloudCoderLogo-med.png");
		rootLayoutPanel.add(image);
		rootLayoutPanel.setWidgetLeftWidth(image, 315.0, Unit.PX, 240.0, Unit.PX);
		rootLayoutPanel.setWidgetTopHeight(image, 101.0, Unit.PX, 165.0, Unit.PX);
	}
}
