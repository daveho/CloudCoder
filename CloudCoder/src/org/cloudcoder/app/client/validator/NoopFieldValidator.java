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
 * {@link IFieldValidator} implementation that succeeds trivially
 * without checking anything.
 * 
 * @author David Hovemeyer
 */
public class NoopFieldValidator implements IFieldValidator<Widget> {
	@Override
	public void setWidget(Widget widget) {
		// No need to do anything
	}

	@Override
	public boolean validate(IValidationCallback callback) {
		callback.onSuccess();
		return true;
	}
}
