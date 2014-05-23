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
	 */
	public SetDatesPanel() {
		FlowPanel panel = new FlowPanel();
		initWidget(panel);
		
		long now = System.currentTimeMillis();

		whenAssignedPicker = createLabeledDateTimePicker(panel, "When assigned:", now);
		whenDuePicker = createLabeledDateTimePicker(panel, "When due:", now + 48L*60*60*1000);
	}
	
	public long getWhenAssigned() {
		return whenAssignedPicker.getValue();
	}
	
	public long getWhenDue() {
		return whenDuePicker.getValue();
	}

	public DateTimePicker createLabeledDateTimePicker(FlowPanel panel, String labelText, long time) {
		FlowPanel div = new FlowPanel();
		div.setStyleName("cc-labeledDateTimePicker", true);
		div.add(new Label(labelText));
		DateTimePicker picker = new DateTimePicker();
		picker.setValue(time);
		div.add(picker);
		panel.add(div);
		return picker;
	}
}
