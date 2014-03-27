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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * Implementation of {@link IDevActionsPanel} which uses a
 * FlowPanel for the buttons.  Provides more flexibility for layout
 * and flow than the original DevActionsPanel.
 * 
 * @author David Hovemeyer
 */
public class DevActionsPanel2 extends Composite implements IDevActionsPanel {
	private static final double BUTTON_HEIGHT_PX = 32.0;
	private static final double BUTTON_WIDTH_PX = 120.0;
	private Runnable submitHandler;
	private Runnable resetHandler;

	public DevActionsPanel2() {
		FlowPanel panel = new FlowPanel();

		Button resetButton = new Button("Reset");
		resetButton.setWidth(BUTTON_WIDTH_PX + "px");
		resetButton.setHeight(BUTTON_HEIGHT_PX + "px");
		panel.add(resetButton);
		resetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (resetHandler != null) {
					resetHandler.run();
				}
			}
		});
		
		panel.add(new InlineHTML("&nbsp;"));
		
		Button submitButton = new Button("Submit!");
		submitButton.setWidth(BUTTON_WIDTH_PX + "px");
		submitButton.setHeight(BUTTON_HEIGHT_PX + "px");
		submitButton.setStylePrimaryName("cc-emphButton");
		panel.add(submitButton);
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
