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
import com.google.gwt.user.client.ui.Label;

/**
 * Base class for UI widgets for {@link EditModelObjectField}
 * implementations.
 * 
 * @author David Hovemeyer
 */
public abstract class EditModelObjectFieldUI extends Composite {
	private Label errorLabel;
	
	/**
	 * Constructor.
	 */
	public EditModelObjectFieldUI() {
	}

	/**
	 * @return the errorLabel
	 */
	public Label getErrorLabel() {
		if (errorLabel == null) {
			errorLabel = new Label("");
			errorLabel.setStyleName("cc-fieldEditorErrorLabel", true);
			errorLabel.setVisible(false);
		}
		return errorLabel;
	}
	
	/**
	 * Set an error message.
	 * 
	 * @param errorMessage error message to set
	 */
	public void setError(String errorMessage) {
		errorLabel.setText(errorMessage);
		errorLabel.setVisible(true);
	}
	
	/**
	 * Clear the error message.
	 */
	public void clearError() {
		errorLabel.setVisible(false);
	}
}
