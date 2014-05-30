// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.tractionsoftware.gwt.user.client.ui.UTCDateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCTimeBox;

/**
 * Attempt at a date/time picker widget using UTCDateBox and
 * UTCTimeBox from GWT-Traction.  <em>Important</em>: this
 * widget allows the user to pick a UTC time, and the result
 * will likely need to be adjusted for the local timezone
 * (which can be done with the {@link #utcToLocal(long)} method. 
 * 
 * @author David Hovemeyer
 */
public class DateTimePicker extends Composite implements HasValueChangeHandlers<Long>, HasValue<Long> {
	private UTCDateBox dateBox;
	private UTCTimeBox timeBox;
	
	public DateTimePicker() {
		FlowPanel panel = new FlowPanel();
		
		dateBox = new UTCDateBox();
		FlowPanel dateDiv = new FlowPanel();
		dateDiv.add(dateBox);
		panel.add(dateDiv);
		timeBox = new UTCTimeBox();
		FlowPanel timeDiv = new FlowPanel();
		timeDiv.add(timeBox);
		panel.add(timeDiv);
		initWidget(panel);
		
		// Listen for change events from the date box and time box
		dateBox.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
				ValueChangeEvent.fire(DateTimePicker.this, getValue());
			}
		});
		timeBox.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
				ValueChangeEvent.fire(DateTimePicker.this, getValue());
			}
		});
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Long> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Long getValue() {
		return dateBox.getValue() + timeBox.getValue();
	}

	@Override
	public void setValue(Long value) {
		long midnight = UTCDateBox.trimTimeToMidnight(value);
		long excess = value - midnight;
		dateBox.setValue(value, false);
		timeBox.setValue(excess, false);
	}

	@Override
	public void setValue(Long value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}
	
	/**
	 * Convert a time in UTC to a local time.
	 * E.g., convert 9:00 AM in UTC to 9:00 AM in the local
	 * timezone.  Note that this is a <em>different time</em>
	 * in the sense of not being the same instant as the UTC
	 * time, which makes sense if the user thinks he/she
	 * is editing a local time rather than a UTC time.
	 * 
	 * @param utcTime  a UTC time
	 * @return a local time (adjusting for the current time zone offset)
	 */
	public static long utcToLocal(long utcTime) {
		Date local = UTCDateBox.utc2date(utcTime);
		return local.getTime();
	}

	/**
	 * Convert a local time to a time in UTC.
	 * This does a conversion that is the opposite of
	 * {@link #utcToLocal(long)}.
	 *
	 * @param value a local time
	 * @return UTC time
	 */
	public static Long localToUtc(long value) {
		return UTCDateBox.date2utc(new Date(value));
	}
}
