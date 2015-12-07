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

import java.util.Date;

import org.cloudcoder.app.client.model.DateTimeUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * Attempt at a date/time picker widget using the GWT DatePicker
 * class and our custom HourMinuteTextBox widget.
 * 
 * @author David Hovemeyer
 */
public class DateTimePicker extends Composite implements HasValueChangeHandlers<Long>, HasValue<Long> {
	private DatePicker dateBox;
	private HourMinuteTextBox timeBox;
	
	public DateTimePicker() {
		FlowPanel panel = new FlowPanel();
		
		dateBox = new DatePicker();
		FlowPanel dateDiv = new FlowPanel();
		dateDiv.add(dateBox);
		panel.add(dateDiv);
		timeBox = new HourMinuteTextBox();
		FlowPanel timeDiv = new FlowPanel();
		timeDiv.add(timeBox);
		panel.add(timeDiv);
		initWidget(panel);
		
		// Listen for change events from the date box and time box
		dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
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
		GWT.log("DateTimePicker: getting value...");
		Date date = dateBox.getValue();
		Long time = timeBox.getValue();
		GWT.log("DateTimePicker: date=" + date + ", time=" + time);
		return date.getTime() + time;
	}

	@Override
	public void setValue(Long value) {
		// Convert to a (local) date/time
		Date date = new Date(value);
		GWT.log("Original time: " + date.getTime());
		
		// Convert date to midnight by formatting without
		// hour/minute components
		Date midnight = DateTimeUtil.toMidnight(date);
		GWT.log("Midnight time: " + midnight.getTime());
		dateBox.setValue(midnight);
		
		// Excess time is the time past midnight, which gives us the
		// hour and minute
		long excess = value - midnight.getTime();
		GWT.log("Excess time: " + excess);
		timeBox.setValue(excess);
	}

	@Override
	public void setValue(Long value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}
}
