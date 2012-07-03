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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Editor for an enum field of a model object.
 * 
 * @author David Hovemeyer
 */
public abstract class EditEnumField<ModelObjectType, EnumType extends Enum<EnumType>>
		extends EditModelObjectField<ModelObjectType, EnumType> {
	
	private class UI extends Composite {
		private EnumType[] enumValues;
		private ListBox listBox;

		public UI() {
			FlowPanel panel = new FlowPanel();
			
			InlineLabel label = new InlineLabel(getDescription());
			panel.add(label);

			this.enumValues = enumCls.getEnumConstants();

			this.listBox = new ListBox();
			for (EnumType e : enumValues) {
				listBox.addItem(e.toString());
			}

			// Use a ChangeHandler to keep track of selecton changes
			// and commit changes to the model object as appropriate.
			listBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					int index = listBox.getSelectedIndex();
					if (index >= 0 && index < enumValues.length) {
						commit();
					}
				}
			});
			
			panel.add(listBox);
			
			initWidget(panel);
		}
		
		public void setEnumValue(EnumType value) {
			int index = 0;
			for (EnumType e : enumValues) {
				if (e == value) {
					break;
				}
				index++;
			}
			listBox.setItemSelected(index, true);
		}
		
		public EnumType getEnumValue() {
			return enumValues[listBox.getSelectedIndex()];
		}
	}
	
	private Class<EnumType> enumCls;
	private UI ui;
	
	public EditEnumField(String desc, Class<EnumType> enumCls) {
		super(desc);
		this.enumCls = enumCls;
		this.ui = new UI();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#getUI()
	 */
	@Override
	public IsWidget getUI() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#getHeightPx()
	 */
	@Override
	public double getHeightPx() {
		return EditStringField.HEIGHT_PX;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#commit()
	 */
	@Override
	public void commit() {
		setField(ui.getEnumValue());
	}
	
	@Override
	public void update() {
		ui.setEnumValue(getField());
	}
}
