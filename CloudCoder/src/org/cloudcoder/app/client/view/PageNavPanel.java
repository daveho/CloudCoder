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
 * Panel with buttons for navigating between the pages
 * of the CloudCoder UI.
 * 
 * @author David Hovemeyer
 */
public class PageNavPanel extends ResizeComposite {
	/**
	 * Width in pixels.
	 */
	public static final double WIDTH_PX = 250.0;
	
	/**
	 * Height in pixels.
	 */
	public static final double HEIGHT_PX = 40.0;
	
	private LayoutPanel layoutPanel;
	private Button backPageButton;
	private Button logOutButton;
	
	private Runnable backHandler;
	private Runnable logoutHandler;
	
	/**
	 * Constructor.
	 */
	public PageNavPanel() {
		this.layoutPanel = new LayoutPanel();
		
		logOutButton = new Button("Log out");
		logOutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (logoutHandler != null) {
					logoutHandler.run();
				}
			}
		});
		layoutPanel.add(logOutButton);
		layoutPanel.setWidgetRightWidth(logOutButton, 0.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(logOutButton, 0.0, Unit.PX, 27.0, Unit.PX);
		
		backPageButton = new Button("<< Back");
		backPageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (backHandler != null) {
					backHandler.run();
				}
			}
		});
		layoutPanel.add(backPageButton);
		layoutPanel.setWidgetRightWidth(backPageButton, 87.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(backPageButton, 0.0, Unit.PX, 27.0, Unit.PX);
		
		initWidget(layoutPanel);
	}

	/**
	 * Set whether or not to show the "Back" button.
	 * 
	 * @param b true if the "Back" button should be shown, false otherwise.
	 */
	public void setShowBackButton(boolean b) {
		if (!b) {
			layoutPanel.remove(backPageButton);
		}
	}
	
	/**
	 * Set callback to run when the "Back" button is clicked.
	 * @param backHandler callback to run when the "Back" button is clicked
	 */
	public void setBackHandler(Runnable backHandler) {
		this.backHandler = backHandler;
	}
	
	/**
	 * Set callback to run when the "Log out" button is clicked.
	 * @param backHandler callback to run when the "Log out" button is clicked
	 */
	public void setLogoutHandler(Runnable logoutHandler) {
		this.logoutHandler = logoutHandler;
	}
}
