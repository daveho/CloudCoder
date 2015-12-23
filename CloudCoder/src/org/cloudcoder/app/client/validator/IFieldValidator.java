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

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for form field validators.
 * 
 * @author David Hovemeyer
 * @param <E> type of Widget being validated
 */
public interface IFieldValidator<E extends Widget> {
	
	/**
	 * Set the Widget to validate.
	 * 
	 * @param widget the Widget to validate
	 */
	public void setWidget(E widget);
	
	/**
	 * Validate the field.
	 * 
	 * @param callback validation callback
	 * @return true if the field was successfully validated, false otherwise
	 */
	public boolean validate(IValidationCallback callback);
}
