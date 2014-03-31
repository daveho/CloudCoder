// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, York College of Pennsylvania
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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Implementation of {@link IDevActionsPanel} which places
 * the reset and submit buttons side-by-side.
 * 
 * @author David Hovemeyer
 */
public class DevActionsPanel2 extends Composite implements IDevActionsPanel {
	private static final double BUTTON_HEIGHT_PX = 28.0;
	private static final double SUBMIT_BUTTON_WIDTH_PX = 100.0;
	private static final double RESET_BUTTON_WIDTH_PX = 80.00;
	public static final double WIDTH_PX = RESET_BUTTON_WIDTH_PX + 5.0 + SUBMIT_BUTTON_WIDTH_PX;
	private Runnable submitHandler;
	private Runnable resetHandler;

	public DevActionsPanel2() {
		LayoutPanel panel = new LayoutPanel();

		Button resetButton = new Button("Reset");
		panel.add(resetButton);
		panel.setWidgetLeftWidth(resetButton, 0.0, Unit.PX, RESET_BUTTON_WIDTH_PX, Unit.PX);
		panel.setWidgetTopHeight(resetButton, 0.0, Unit.PX, BUTTON_HEIGHT_PX, Unit.PX);
		resetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (resetHandler != null) {
					resetHandler.run();
				}
			}
		});
		
		Button submitButton = new Button("Submit!");
		submitButton.setStylePrimaryName("cc-emphButton");
		panel.add(submitButton);
		panel.setWidgetRightWidth(submitButton, 0.0, Unit.PX, SUBMIT_BUTTON_WIDTH_PX, Unit.PX);
		panel.setWidgetTopHeight(submitButton, 0.0, Unit.PX, BUTTON_HEIGHT_PX, Unit.PX);
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (submitHandler != null) {
					submitHandler.run();
				}
			}
		});
		
		initWidget(panel);
	}
	
	@Override
	public void setSubmitHandler(Runnable submitHandler) {
		this.submitHandler = submitHandler;
	}

	@Override
	public void setResetHandler(Runnable resetHandler) {
		this.resetHandler = resetHandler;
	}
}
