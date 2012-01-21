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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * The DevActionsPanel contains the buttons for the DevelopmentPage
 * (such as the "Submit!" button.)
 * 
 * @author David Hovemeyer
 */
public class DevActionsPanel extends Composite {
	private Runnable submitHandler;
	
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
		layoutPanel.setWidgetRightWidth(submitButton, 0.0, Unit.PX, 100.0, Unit.PX);
		layoutPanel.setWidgetBottomHeight(submitButton, 15.0, Unit.PX, 32.0, Unit.PX);

		initWidget(layoutPanel);
	}
	
	public void setSubmitHandler(Runnable submitHandler) {
		this.submitHandler = submitHandler;
	}
}
