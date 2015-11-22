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
 * Field validator to check whether a TextBox contains an integer.
 * 
 * @author David Hovemeyer
 */
public class TextBoxIntegerValidator implements IFieldValidator<TextBox> {
	private TextBox textBox;
	
	@Override
	public void setWidget(TextBox widget) {
		this.textBox = widget;
	}
	
	@Override
	public boolean validate(IValidationCallback callback) {
		String value = textBox.getValue().trim();
		if (value.length() == 0) {
			callback.onFailure("A value is required");
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				callback.onFailure("Value must be an integer");
				return false;
			}
		}
		callback.onSuccess();
		return true;
	}
}
