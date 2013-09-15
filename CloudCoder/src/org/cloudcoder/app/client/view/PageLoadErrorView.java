// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import org.cloudcoder.app.client.CloudCoder;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * A UI to be displayed in a page if the page's objects can't
 * be loaded.  This could happen if there is a direct link to
 * a specific page, but the page params don't specify valid
 * objects, or the objects can't be loaded.
 * 
 * @author David Hovemeyer
 */
public class PageLoadErrorView extends Composite implements SessionObserver {
	private StatusMessageView statusMessageView;
	private Button button;
	
	/**
	 * Constructor.
	 */
	public PageLoadErrorView() {
		LayoutPanel panel = new LayoutPanel();
		
		InlineLabel label = new InlineLabel("An error occurred loading this page.");
		label.setStyleName("cc-errorText", true);
		panel.add(label);
		panel.setWidgetLeftRight(label, 80.0, Unit.PX, 80.0, Unit.PX);
		panel.setWidgetTopHeight(label, 40.0, Unit.PX, 32.0, Unit.PX);
		
		statusMessageView = new StatusMessageView();
		panel.add(statusMessageView);
		panel.setWidgetLeftRight(statusMessageView, 80.0, Unit.PX, 80.0, Unit.PX);
		panel.setWidgetTopHeight(statusMessageView, 80.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
		
		button = new Button("Go to home page");
		panel.add(button);
		panel.setWidgetLeftWidth(button, 80.0, Unit.PX, 320.0, Unit.PX);
		panel.setWidgetTopHeight(button, 140.0, Unit.PX, 32.0, Unit.PX);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CloudCoder.getInstance().createPostLoginPage(PageId.COURSES_AND_PROBLEMS, "");
			}
		});
		
		initWidget(panel);
	}
	
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		statusMessageView.activate(session, subscriptionRegistrar);
	}
}
