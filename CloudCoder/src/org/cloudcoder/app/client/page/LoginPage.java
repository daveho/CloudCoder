package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class LoginPage extends CloudCoderPage {
	private InlineLabel pageTitleLabel;
	private TextBox usernameTextBox;
	private PasswordTextBox passwordTextBox;
	private InlineLabel errorLabel;
	private InlineLabel loggingInLabel;
	public LoginPage() {
		setSize("640px", "480px");
		
		InlineLabel usernameLabel = new InlineLabel("User name:");
		add(usernameLabel);
		setWidgetLeftWidth(usernameLabel, 57.0, Unit.PX, 91.0, Unit.PX);
		setWidgetTopHeight(usernameLabel, 127.0, Unit.PX, 15.0, Unit.PX);
		
		usernameTextBox = new TextBox();
		add(usernameTextBox);
		setWidgetLeftWidth(usernameTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
		setWidgetTopHeight(usernameTextBox, 148.0, Unit.PX, 31.0, Unit.PX);
		
		InlineLabel passwordLabel = new InlineLabel("Password:");
		add(passwordLabel);
		setWidgetLeftWidth(passwordLabel, 57.0, Unit.PX, 91.0, Unit.PX);
		setWidgetTopHeight(passwordLabel, 185.0, Unit.PX, 15.0, Unit.PX);
		
		passwordTextBox = new PasswordTextBox();
		add(passwordTextBox);
		setWidgetLeftWidth(passwordTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
		setWidgetTopHeight(passwordTextBox, 206.0, Unit.PX, 33.0, Unit.PX);
		
		Button loginButton = new Button("Log in");
		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				attemptLogin();
			}
		});
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
		setWidgetTopHeight(promptLabel, 328.0, Unit.PX, 20.0, Unit.PX);
		
		errorLabel = new InlineLabel("");
		errorLabel.setStylePrimaryName("cc-errorText");
		add(errorLabel);
		setWidgetLeftWidth(errorLabel, 57.0, Unit.PX, 484.0, Unit.PX);
		setWidgetTopHeight(errorLabel, 383.0, Unit.PX, 73.0, Unit.PX);
		
		pageTitleLabel = new InlineLabel("");
		pageTitleLabel.setStylePrimaryName("cc-pageTitle");
		add(pageTitleLabel);
		setWidgetLeftWidth(pageTitleLabel, 57.0, Unit.PX, 533.0, Unit.PX);
		setWidgetTopHeight(pageTitleLabel, 44.0, Unit.PX, 31.0, Unit.PX);
		
		InlineLabel welcomeLabel = new InlineLabel("Welcome to CloudCoder at");
		add(welcomeLabel);
		setWidgetLeftWidth(welcomeLabel, 57.0, Unit.PX, 313.0, Unit.PX);
		setWidgetTopHeight(welcomeLabel, 23.0, Unit.PX, 15.0, Unit.PX);
		
		loggingInLabel = new InlineLabel("");
		add(loggingInLabel);
		setWidgetLeftWidth(loggingInLabel, 57.0, Unit.PX, 343.0, Unit.PX);
		setWidgetTopHeight(loggingInLabel, 353.0, Unit.PX, 20.0, Unit.PX);
	}

	protected void attemptLogin() {
		String username = usernameTextBox.getText();
		String password = passwordTextBox.getText();
		if (username.equals("") || password.equals("")) {
			errorLabel.setText("Please enter your username and password");
		}
		
		loggingInLabel.setText("Logging in...");
		RPC.loginService.login(username, password, new AsyncCallback<User>() {
			@Override
			public void onFailure(Throwable caught) {
				loggingInLabel.setText("");
				errorLabel.setText("Error communicating with server (are you connected to the network?)");
			}
			
			@Override
			public void onSuccess(User result) {
				// Successful login!
				getSession().add(result);
				getSession().notifySubscribers(Session.Event.LOGIN, result);
			}
		});
	}

	@Override
	public void activate() {
		RPC.configurationSettingService.getConfigurationSettingValue(ConfigurationSettingName.PUB_TEXT_INSTITUTION, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(String result) {
				pageTitleLabel.setText(result);
			}
		});
	}

	@Override
	public void deactivate() {
	}
}
