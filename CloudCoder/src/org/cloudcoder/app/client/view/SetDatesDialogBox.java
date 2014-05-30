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

import org.cloudcoder.app.shared.model.Problem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Dialog to allow the user to change the when assigned
 * and when due dates for one or more {@link Problem}s.
 * 
 * @author David Hovemeyer
 */
public class SetDatesDialogBox extends DialogBox {
	private SetDatesPanel setDatesPanel;
	private Runnable onSetDatesCallback;
	
	/**
	 * Constructor.
	 */
	public SetDatesDialogBox() {
		FlowPanel panel = new FlowPanel();
		
		this.setDatesPanel = new SetDatesPanel();
		panel.add(setDatesPanel);
		
		long now = System.currentTimeMillis();
		setDatesPanel.setWhenAssigned(now);
		setDatesPanel.setWhenDue(now + 48L*60*60*1000);
		
		FlowPanel buttons = new FlowPanel();
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setStyleName("cc-floatRightButton", true);
		buttons.add(cancelButton);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		Button setDatesButton = new Button("Set dates");
		setDatesButton.setStyleName("cc-floatRightButton", true);
		buttons.add(setDatesButton);
		setDatesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (onSetDatesCallback != null) {
					hide();
					onSetDatesCallback.run();
				}
			}
		});
		
		panel.add(buttons);
		
		setGlassEnabled(true);
		setModal(false); // Necessary to allow datebox and timebox popups to receive events

		setWidget(panel);
	}
	
	/**
	 * @param onSetDatesCallback the onSetDatesCallback to set
	 */
	public void setOnSetDatesCallback(Runnable onSetDatesCallback) {
		this.onSetDatesCallback = onSetDatesCallback;
	}

	/**
	 * @return the "when assigned" date/time
	 */
	public long getWhenAssigned() {
//		return DateTimePicker.utcToLocal(setDatesPanel.getWhenAssigned());
		return setDatesPanel.getWhenAssigned();
	}

	/**
	 * @return the "when due" date/time
	 */
	public long getWhenDue() {
//		return DateTimePicker.utcToLocal(setDatesPanel.getWhenDue());
		return setDatesPanel.getWhenDue();
	}
}
