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

package org.cloudcoder.app.client.validator;

import com.google.gwt.user.client.ui.TextBox;

/**
 * @author David Hovemeyer
 */
public class TextBoxNonemptyValidator implements IFieldValidator<TextBox> {
	private String msg;
	private TextBox textBox;

	/**
	 * Constructor.
	 * A generic message is used.
	 */
	public TextBoxNonemptyValidator() {
		msg = "A value is required";
	}
	
	/**
	 * Constructor.
	 * 
	 * @param msg message to use if validation fails
	 */
	public TextBoxNonemptyValidator(String msg) {
		this.msg = msg;
	}
	
	@Override
	public void setWidget(TextBox widget) {
		this.textBox = widget;
	}
	
	@Override
	public boolean validate(IValidationCallback callback) {
		String value = textBox.getValue();
		if (value.trim().length() == 0) {
			callback.onFailure(msg);
			return false;
		}
		callback.onSuccess();
		return true;
	}
}
