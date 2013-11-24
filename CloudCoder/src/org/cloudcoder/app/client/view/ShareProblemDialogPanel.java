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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Panel that goes inside a ShareProblemDialog.
 * 
 * @author David Hovemeyer
 */
public class ShareProblemDialogPanel extends Composite {
	private TextBox usernameTextBox;
	private PasswordTextBox passwordTextBox;
	
	private Runnable cancelButtonCallback;
	private Runnable shareExerciseButtonCallback;
	private Label exerciseNameLabel;
	private Label exerciseLicenseLabel;
	private Label errorLabel;
	
	public ShareProblemDialogPanel() {
		
		LayoutPanel layoutPanel = new LayoutPanel();
		initWidget(layoutPanel);
		layoutPanel.setSize("450px", "340px");
		
		Label dialogTitleLabel = new Label("Share exercise to repository");
		dialogTitleLabel.setStyleName("cc-dialogTitle");
		layoutPanel.add(dialogTitleLabel);
		layoutPanel.setWidgetLeftRight(dialogTitleLabel, 10.0, Unit.PX, 10.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(dialogTitleLabel, 10.0, Unit.PX, 28.0, Unit.PX);
		
		HTML instructionTextLabel = new HTML("To publish this exercise to the CloudCoder exercise repository, enter your repository username and password and click the \"Share exercise\" button.<br /><br />\nClick the \"Cancel\" button if you do not want to share this exercise.", true);
		layoutPanel.add(instructionTextLabel);
		layoutPanel.setWidgetLeftRight(instructionTextLabel, 10.0, Unit.PX, 10.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(instructionTextLabel, 44.0, Unit.PX, 80.0, Unit.PX);
		
		usernameTextBox = new TextBox();
		layoutPanel.add(usernameTextBox);
		layoutPanel.setWidgetLeftWidth(usernameTextBox, 132.0, Unit.PX, 161.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(usernameTextBox, 180.0, Unit.PX, 31.0, Unit.PX);
		
		passwordTextBox = new PasswordTextBox();
		layoutPanel.add(passwordTextBox);
		layoutPanel.setWidgetLeftWidth(passwordTextBox, 132.0, Unit.PX, 161.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(passwordTextBox, 217.0, Unit.PX, 31.0, Unit.PX);
		
		InlineLabel usernameLabel = new InlineLabel("Username:");
		layoutPanel.add(usernameLabel);
		layoutPanel.setWidgetLeftWidth(usernameLabel, 60.0, Unit.PX, 68.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(usernameLabel, 188.0, Unit.PX, 15.0, Unit.PX);
		
		InlineLabel passwordLabel = new InlineLabel("Password:");
		layoutPanel.add(passwordLabel);
		layoutPanel.setWidgetLeftWidth(passwordLabel, 60.0, Unit.PX, 68.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(passwordLabel, 225.0, Unit.PX, 15.0, Unit.PX);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				handleCancelButtonClick();
			}
		});
		layoutPanel.add(cancelButton);
		layoutPanel.setWidgetLeftWidth(cancelButton, 100.0, Unit.PX, 120.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(cancelButton, 267.0, Unit.PX, 27.0, Unit.PX);
		
		Button shareExerciseButton = new Button("Share exercise");
		shareExerciseButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				handleSharedExerciseButtonClick();
			}
		});
		layoutPanel.add(shareExerciseButton);
		layoutPanel.setWidgetRightWidth(shareExerciseButton, 100.0, Unit.PX, 120.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(shareExerciseButton, 267.0, Unit.PX, 27.0, Unit.PX);
		
		exerciseNameLabel = new Label("");
		layoutPanel.add(exerciseNameLabel);
		layoutPanel.setWidgetLeftRight(exerciseNameLabel, 100.0, Unit.PX, 10.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(exerciseNameLabel, 124.0, Unit.PX, 15.0, Unit.PX);
		
		exerciseLicenseLabel = new Label("");
		layoutPanel.add(exerciseLicenseLabel);
		layoutPanel.setWidgetLeftRight(exerciseLicenseLabel, 100.0, Unit.PX, 10.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(exerciseLicenseLabel, 145.0, Unit.PX, 15.0, Unit.PX);
		
		errorLabel = new Label("");
		errorLabel.setStyleName("cc-errorText");
		layoutPanel.add(errorLabel);
		layoutPanel.setWidgetLeftRight(errorLabel, 10.0, Unit.PX, 10.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(errorLabel, 313.0, Unit.PX, 15.0, Unit.PX);
		
		InlineLabel licenseLabel = new InlineLabel("License:");
		layoutPanel.add(licenseLabel);
		layoutPanel.setWidgetLeftWidth(licenseLabel, 20.0, Unit.PX, 60.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(licenseLabel, 145.0, Unit.PX, 15.0, Unit.PX);
		
		InlineLabel exerciseLabel = new InlineLabel("Exercise:");
		layoutPanel.add(exerciseLabel);
		layoutPanel.setWidgetLeftWidth(exerciseLabel, 20.0, Unit.PX, 60.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(exerciseLabel, 124.0, Unit.PX, 15.0, Unit.PX);
	}
	
	/**
	 * Set a callback to run when the "Cancel" button is clicked.
	 * 
	 * @param cancelButtonCallback the cancelButtonCallback to set
	 */
	public void setCancelButtonCallback(Runnable cancelButtonCallback) {
		this.cancelButtonCallback = cancelButtonCallback;
	}
	
	/**
	 * Set a callback to run when the "Share exercise" button is clicked.
	 * 
	 * @param shareExerciseButtonCallback the shareExerciseButtonCallback to set
	 */
	public void setShareExerciseButtonCallback(
			Runnable shareExerciseButtonCallback) {
		this.shareExerciseButtonCallback = shareExerciseButtonCallback;
	}
	
	/**
	 * Get the username entered in the username text box.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return usernameTextBox.getText();
	}
	
	/**
	 * Get the password entered in the password text box.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return passwordTextBox.getText();
	}
	
	/**
	 * Set the exercise name to display.
	 * 
	 * @param exerciseName the exercise name
	 */
	public void setExerciseName(String exerciseName) {
		exerciseNameLabel.setText(exerciseName);
	}
	
	/**
	 * Set the license to display.
	 * 
	 * @param license the license
	 */
	public void setExerciseLicense(String license) {
		exerciseLicenseLabel.setText(license);
	}
	
	/**
	 * Set error message to display.
	 * 
	 * @param errorMessage error message
	 */
	public void setErrorMessage(String errorMessage) {
		errorLabel.setText(errorMessage);
	}
	
	protected void handleSharedExerciseButtonClick() {
		if (shareExerciseButtonCallback != null) {
			shareExerciseButtonCallback.run();
		}
	}

	protected void handleCancelButtonClick() {
		if (cancelButtonCallback != null) {
			cancelButtonCallback.run();
		}
	}
}
