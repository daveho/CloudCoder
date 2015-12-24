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

package org.cloudcoder.app.client.view;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.validator.IFieldValidator;
import org.cloudcoder.app.client.validator.IValidationCallback;

import com.google.gwt.user.client.ui.Widget;

/**
 * Composite validator (for associating multiple validators with
 * a single widget.)
 * 
 * @author David Hovemeyer
 */
public class CompositeValidator<E extends Widget> implements IFieldValidator<E> {
	private List<IFieldValidator<E>> validators;

	/**
	 * Constructor.
	 */
	public CompositeValidator() {
		this.validators = new ArrayList<IFieldValidator<E>>();
	}
	
	/**
	 * Add a validator.
	 * 
	 * @param v the validator to add
	 * @return a reference to this object, to allow method chaining
	 */
	public CompositeValidator<E> add(IFieldValidator<E> v) {
		validators.add(v);
		return this;
	}
	
	@Override
	public void setWidget(E widget) {
		for (IFieldValidator<E> v : validators) {
			v.setWidget(widget);
		}
	}

	@Override
	public boolean validate(IValidationCallback callback) {
		for (IFieldValidator<E> v : validators) {
			if (!v.validate(callback)) {
				// We can assume that the validator will have
				// called the callback's onFailure method
				return false;
			}
		}
		callback.onSuccess();
		return true;
	}
}
