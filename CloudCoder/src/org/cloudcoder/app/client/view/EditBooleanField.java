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

import org.cloudcoder.app.shared.model.ModelObjectField;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

/**
 * Implementation of {@link EditModelObjectField} for editing a boolean
 * field.
 * 
 * @author David Hovemeyer
 */
public class EditBooleanField<ModelObjectType>
		extends EditModelObjectField<ModelObjectType, Boolean> {
	
	private class UI extends Composite {
		private CheckBox checkBox;

		public UI(String checkboxLabel) {
			FlowPanel panel = new FlowPanel();
			panel.setStyleName("cc-fieldEditor");
			
			Label label = new Label(getDescription());
			label.setStyleName("cc-fieldEditorLabel", true);
			panel.add(label);
			
			this.checkBox = new CheckBox(checkboxLabel);
			panel.add(checkBox);
			
			initWidget(panel);
		}
		
		public void setValue(Boolean value) {
			checkBox.setValue(value);
		}
		
		public Boolean getValue() {
			return checkBox.getValue();
		}
	}

	private UI ui;

	/**
	 * Constructor.
	 * 
	 * @param desc the human-readable description of the field
	 * @param checkboxLabel label to display next to checkbox
	 * @param field the {@link ModelObjectField} being edited
	 */
	public EditBooleanField(String desc, String checkboxLabel, ModelObjectField<? super ModelObjectType, Boolean> field) {
		super(desc, field);
		ui = new UI(checkboxLabel);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#getUI()
	 */
	@Override
	public IsWidget getUI() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#commit()
	 */
	@Override
	public void commit() {
		setField(ui.getValue());
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#update()
	 */
	@Override
	public void update() {
		ui.setValue(getField());
	}
}
