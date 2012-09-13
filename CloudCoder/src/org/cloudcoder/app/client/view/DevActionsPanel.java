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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * The DevActionsPanel contains the buttons for the DevelopmentPage
 * (such as the "Submit!" button.)
 * 
 * @author David Hovemeyer
 */
public class DevActionsPanel extends ResizeComposite {
	/**
	 * 
	 */
	private static final double BUTTON_HEIGHT_PX = 32.0;
	/**
	 * 
	 */
	private static final double BUTTON_WIDTH_PX = 120.0;
	private Runnable submitHandler;
	private Runnable resetHandler;
	
	/**
	 * Constructor.
	 */
	public DevActionsPanel() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		Button submitButton = new Button("Submit!");
		submitButton.setStylePrimaryName("cc-emphButton");
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (submitHandler != null) {
					submitHandler.run();
				}
			}
		});
		layoutPanel.add(submitButton);
		layoutPanel.setWidgetRightWidth(submitButton, 0.0, Unit.PX, BUTTON_WIDTH_PX, Unit.PX);
		layoutPanel.setWidgetBottomHeight(submitButton, 10.0, Unit.PX, BUTTON_HEIGHT_PX, Unit.PX);
		
		Button resetButton = new Button("Reset");
		resetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (resetHandler != null) {
					resetHandler.run();
				}
			}
		});
		layoutPanel.add(resetButton);
		layoutPanel.setWidgetRightWidth(resetButton, 0.0, Unit.PX, BUTTON_WIDTH_PX, Unit.PX);
		layoutPanel.setWidgetBottomHeight(resetButton, 10.0 + BUTTON_HEIGHT_PX + 10.0, Unit.PX, BUTTON_HEIGHT_PX, Unit.PX);

		initWidget(layoutPanel);
	}
	
	/**
	 * Set the handler to run when the Submit! button is clicked.
	 * 
	 * @param submitHandler handler to run when the Submit! button is clicked
	 */
	public void setSubmitHandler(Runnable submitHandler) {
		this.submitHandler = submitHandler;
	}
	
	/**
	 * Set the handler to run when the Reset button is clicked.
	 * 
	 * @param resetHandler handler to run when the Reset button is clicked
	 */
	public void setResetHandler(Runnable resetHandler) {
		this.resetHandler = resetHandler;
	}
}
