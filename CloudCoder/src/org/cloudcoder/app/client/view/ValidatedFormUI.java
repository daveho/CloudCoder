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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Superclass for simple validated form UIs.
 * 
 * @author David Hovemeyer
 */
public abstract class ValidatedFormUI extends Composite {
	private static final double FIELD_HEIGHT_PX = 28.0;
	private static final double FIELD_PADDING_PX = 8.0;

	private Panel panel;
	private List<IFieldValidator<? extends Widget>> validatorList;
	private List<IValidationCallback> validationCallbackList;

	/**
	 * Constructor.
	 */
	public ValidatedFormUI() {
		this(new LayoutPanel());
	}

	public ValidatedFormUI(Panel panel) {
		this.validatorList = new ArrayList<IFieldValidator<? extends Widget>>();
		this.validationCallbackList = new ArrayList<IValidationCallback>();
		this.panel = panel;
		initWidget(panel);
	}
	
	/**
	 * Get the Panel that is the content area for
	 * this UI.  It will be a LayoutPanel unless the superclass
	 * provides its own content area Panel.
	 * Subclasses cannot override this (but they can
	 * override {@link #getLayoutPanel()}.
	 * 
	 * @return the panel
	 */
	public final Panel getPanel() {
		return panel;
	}
	
	/**
	 * Get the LayoutPanel.
	 * Subclasses that provide their own panel widget to the
	 * constructor should override this to return the LayoutPanel
	 * embedded in the actual panel widget.
	 * 
	 * @return the LayoutPanel
	 */
	public LayoutPanel getLayoutPanel() {
		return (LayoutPanel)panel;
	}

	/**
	 * Add a form widget.
	 * 
	 * @param y           y coordinate of current row
	 * @param widget      the form widget
	 * @param labelText   the label text
	 * @param validator   the validator for the widget
	 * @return y coordinate of next row
	 */
	protected<E extends Widget> double addWidget(double y, final E widget, String labelText, IFieldValidator<E> validator) {
		InlineLabel label = new InlineLabel(labelText);
		label.setStyleName("cc-rightJustifiedLabel", true);
		getLayoutPanel().add(label);
		getLayoutPanel().setWidgetTopHeight(label, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		getLayoutPanel().setWidgetLeftWidth(label, 20.0, Unit.PX, 120.0, Unit.PX);

		getLayoutPanel().add(widget);
		getLayoutPanel().setWidgetTopHeight(widget, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		getLayoutPanel().setWidgetLeftWidth(widget, 160.0, Unit.PX, 320.0, Unit.PX);
		
		final InlineLabel validationErrorLabel = new InlineLabel();
		validationErrorLabel.setStyleName("cc-errorText", true);
		getLayoutPanel().add(validationErrorLabel);
		getLayoutPanel().setWidgetTopHeight(validationErrorLabel, y, Unit.PX, FIELD_HEIGHT_PX, Unit.PX);
		getLayoutPanel().setWidgetLeftRight(validationErrorLabel, 500.0, Unit.PX, 0.0, Unit.PX);
		
		validatorList.add(validator);
		validator.setWidget(widget);
		
		IValidationCallback callback = new IValidationCallback() {
			@Override
			public void onSuccess() {
				validationErrorLabel.setText("");
				widget.removeStyleName("cc-invalid");
			}
			
			@Override
			public void onFailure(String msg) {
				validationErrorLabel.setText(msg);
				widget.setStyleName("cc-invalid", true);
			}
		};
		validationCallbackList.add(callback);
		
		return y + FIELD_HEIGHT_PX + FIELD_PADDING_PX;
	}
	
	/**
	 * Validate the form fields.
	 * 
	 * @return true if all fields successfully validated, false otherwise
	 */
	public boolean validate() {
		int numFailures = 0;
		for (int i = 0; i < validatorList.size(); i++) {
			IFieldValidator<? extends Widget> validator = validatorList.get(i);
			IValidationCallback callback = validationCallbackList.get(i);
			if (!validator.validate(callback)) {
				numFailures++;
			}
		}
		return numFailures == 0;
	}
	
	/**
	 * Clear field values.
	 */
	public abstract void clear();
}
