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

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * Edit a {@link Date} field with a UI that can set both
 * date and time.
 * 
 * @author David Hovemeyer
 */
public abstract class EditDateTimeField<ModelObjectType>
		extends EditModelObjectField<ModelObjectType, Date> {
	
	private static final int MILLIS_PER_HOUR = 60*60*1000;
	private static final int MILLIS_PER_MINUTE = 60*1000;
	private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy MM dd");
	private static final DateTimeFormat HOUR_MINUTE_FORMAT = DateTimeFormat.getFormat("HH:mm");

	private class UI extends Composite {
		private DatePicker datePicker;
		private TextBox hourMinuteTextBox;
		//private HourMinutePicker hourMinutePicker;

		public UI() {
			FlowPanel panel = new FlowPanel();
			
			Label label = new Label(getDescription());
			label.setStyleName("cc-fieldEditorLabel");
			panel.add(label);
			
			// Add a date picker and float it left
			this.datePicker = new DatePicker();
			datePicker.setStyleName("cc-editDateTimeFloatLeft", true);
			panel.add(datePicker);
			
			// Add a label/textbox (for the hour/minute) and float it left
			FlowPanel hourMinutePanel = new FlowPanel();
			hourMinutePanel.setStyleName("cc-editDateTimeFloatLeft");
			Label hourMinuteLabel = new Label("Time (HH:MM)");
			hourMinutePanel.add(hourMinuteLabel);
			this.hourMinuteTextBox = new TextBox();
			hourMinuteTextBox.setWidth("100px");
			hourMinutePanel.add(hourMinuteTextBox);
			panel.add(hourMinutePanel);
			
			// Add an empty div with clear: both so that the overall widget has a natural height.
			FlowPanel clear = new FlowPanel();
			clear.setStyleName("cc-editDateTimeClearFloats");
			panel.add(clear);
			
			initWidget(panel);
		}

		public Date getDate() {
			Date result = null;
			
			Date datePickerDate = datePicker.getValue();
			if (datePickerDate != null) {
				result = datePickerDate;
				
				String hourMinuteString = hourMinuteTextBox.getText();
				int colon = hourMinuteString.indexOf(':');
				if (colon > 0) {
					try {
						int hour = Integer.parseInt(hourMinuteString.substring(0, colon));
						int minute = Integer.parseInt(hourMinuteString.substring(colon + 1));
						
						result = new Date(result.getTime() + (hour * MILLIS_PER_HOUR) + (minute * MILLIS_PER_MINUTE));
					} catch (NumberFormatException e) {
						// invalid format
					}
				}
			}
			
			return result;
		}

		public void setDate(Date value) {
			DateTimeFormat dateFormat = DATE_FORMAT;
			DateTimeFormat hourMinuteFormat = HOUR_MINUTE_FORMAT;
			
			String dateString = dateFormat.format(value);
			String hourMinuteString = hourMinuteFormat.format(value);
			
			datePicker.setValue(dateFormat.parse(dateString));
			hourMinuteTextBox.setText(hourMinuteString);
		}
	}

	private UI ui;
	
	/**
	 * Constructor.
	 * 
	 * @param desc human-readable description of field being edited
	 */
	public EditDateTimeField(String desc) {
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
