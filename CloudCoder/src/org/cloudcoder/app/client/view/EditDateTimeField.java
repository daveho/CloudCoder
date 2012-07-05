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
import com.google.gwt.user.datepicker.client.DatePicker;
//import com.summatech.gwt.client.HourMinutePicker;
//import com.summatech.gwt.client.HourMinutePicker.PickerFormat;

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
	private static final DateTimeFormat HOUR_MINUTE_FORMAT = DateTimeFormat.getFormat("HH mm");

	private class UI extends Composite {
		private DatePicker datePicker;
		//private HourMinutePicker hourMinutePicker;

		public UI() {
			FlowPanel panel = new FlowPanel();
			
			Label label = new Label(getDescription());
			label.setStyleName("cc-fieldEditorLabel");
			panel.add(label);
			
			this.datePicker = new DatePicker();
			panel.add(datePicker);
			
//			this.hourMinutePicker = new HourMinutePicker(PickerFormat._24_HOUR);
//			panel.add(hourMinutePicker);
			
			initWidget(panel);
		}

		public Date getDate() {
			Date result = null;
			
			Date datePickerDate = datePicker.getValue();
			if (datePickerDate != null) {
				result = datePickerDate;
				
//				Integer hour = hourMinutePicker.getHour();
//				Integer minute = hourMinutePicker.getMinute();
				
//				if (hour != null && minute != null) {
//					result = new Date(result.getTime() + (hour * MILLIS_PER_HOUR) + (minute * MILLIS_PER_MINUTE));
//				}
			}
			
			return result;
		}

		public void setDate(Date value) {
			DateTimeFormat dateFormat = DATE_FORMAT;
			DateTimeFormat hourMinuteFormat = HOUR_MINUTE_FORMAT;
			
			String dateString = dateFormat.format(value);
			String hourMinuteString = hourMinuteFormat.format(value);
			
			datePicker.setValue(dateFormat.parse(dateString));
			
			int space = hourMinuteString.indexOf(' ');
			int hours = Integer.parseInt(hourMinuteString.substring(0, space));
			int minutes = Integer.parseInt(hourMinuteString.substring(space+1));
			
//			hourMinutePicker.setTime("", hours, minutes);
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
