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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Panel offering a UI for choosing when assigned and
 * when due date/times for a problem.
 * 
 * @author David Hovemeyer
 */
public class SetDatesPanel extends Composite {
	private DateTimePicker whenAssignedPicker;
	private DateTimePicker whenDuePicker;
	
	/**
	 * Constructor.
	 * <em>Important</em>: the {@link #setWhenAssigned(long)} and
	 * {@link #setWhenDue(long)} methods should be called before
	 * adding this panel to a UI.
	 */
	public SetDatesPanel() {
		FlowPanel panel = new FlowPanel();
		initWidget(panel);
		whenAssignedPicker = createLabeledDateTimePicker(panel, "When assigned:");
		whenDuePicker = createLabeledDateTimePicker(panel, "When due:");
	}

	/**
	 * Set the "when assigned" date/time.
	 * 
	 * @param value the "when assigned" date/time to set
	 */
	public void setWhenAssigned(long value) {
		GWT.log("Setting when assigned=" + value);
		whenAssignedPicker.setValue(value);
	}
	
	/**
	 * Set the "when due" date/time.
	 * 
	 * @param value the "when due" date/time to set
	 */
	public void setWhenDue(long value) {
		GWT.log("Setting when due=" + value);
		whenDuePicker.setValue(value);
	}
	
	/**
	 * @return the "when assigned" date/time
	 */
	public long getWhenAssigned() {
		Long value = whenAssignedPicker.getValue();
		GWT.log("When assigned value: " + value);
		return value;
	}
	
	/**
	 * @return the "when due" date/time
	 */
	public long getWhenDue() {
		Long value = whenDuePicker.getValue();
		GWT.log("When due value: " + value);
		return value;
	}

	public DateTimePicker createLabeledDateTimePicker(FlowPanel panel, String labelText) {
		FlowPanel div = new FlowPanel();
		div.setStyleName("cc-labeledDateTimePicker", true);
		div.add(new Label(labelText));
		DateTimePicker picker = new DateTimePicker();
		div.add(picker);
		panel.add(div);
		return picker;
	}
}
