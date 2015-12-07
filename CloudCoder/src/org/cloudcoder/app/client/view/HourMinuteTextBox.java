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

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;

/**
 * Widget for entering hour and minute in HH:MM format.
 * 
 * @author David Hovemeyer
 */
public class HourMinuteTextBox extends Composite implements HasValueChangeHandlers<Long>, HasValue<Long> {
	private TextBox textBox;

	/**
	 * Constructor.
	 */
	public HourMinuteTextBox() {
		this.textBox = new TextBox();
		initWidget(textBox);
	}

	@Override
	public Long getValue() {
		return DateTimeUtil.parseHourAndMinute(textBox.getValue());
	}

	@Override
	public void setValue(Long value) {
		textBox.setText(DateTimeUtil.formatHourAndMinute(value));
	}

	@Override
	public void setValue(Long value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Long> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}
