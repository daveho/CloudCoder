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

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * Implementation of {@link EditModelObjectField} for {@link Date} fields.
 * Uses a DatePicker widget.
 * 
 * @author David Hovemeyer
 */
public abstract class EditDateField<ModelObjectType>
		extends EditModelObjectField<ModelObjectType, Date> {
	
	private class UI extends Composite {
		private DatePicker datePicker;

		public UI() {
			FlowPanel panel = new FlowPanel();
			panel.setStyleName("cc-fieldEditor", true);
			
			Label label = new Label(getDescription());
			label.setStyleName("cc-fieldEditorLabel", true);
			panel.add(label);
			
			this.datePicker = new DatePicker();
			panel.add(datePicker);
			
			datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
				@Override
				public void onValueChange(ValueChangeEvent<Date> event) {
					commit();
				}
			});
			
			initWidget(panel);
		}

		public Date getDate() {
			return datePicker.getValue();
		}

		public void setDate(Date date) {
			datePicker.setValue(date);
		}
	}

	private UI ui;

	/**
	 * Constructor.
	 */
	public EditDateField(String desc) {
		super(desc);
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
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#commit()
	 */
	@Override
	public void commit() {
		setField(ui.getDate());
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#update()
	 */
	@Override
	public void update() {
		ui.setDate(getField());
	}
}
