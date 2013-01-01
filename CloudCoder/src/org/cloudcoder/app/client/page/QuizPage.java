// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page allowing an instructor to administer a quiz for a {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class QuizPage extends CloudCoderPage {
	
	private class UI extends Composite {
		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			LayoutPanel northPanel = new LayoutPanel();
			PageNavPanel navPanel = new PageNavPanel();
			navPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					GWT.log("Going back to course admin page");
					getSession().notifySubscribers(Session.Event.COURSE_ADMIN, null);
				}
			});
			navPanel.setLogoutHandler(new LogoutHandler(getSession()));
			northPanel.add(navPanel);
			northPanel.setWidgetTopHeight(navPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			northPanel.setWidgetRightWidth(navPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			
			Label pageLabel = new Label("Quiz: " + getSession().get(Problem.class).toNiceString());
			pageLabel.setStyleName("cc-pageTitle", true);
			northPanel.add(pageLabel);
			northPanel.setWidgetTopHeight(pageLabel, 0.0, Unit.PX, 28, Unit.PX);
			northPanel.setWidgetLeftRight(pageLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX);
			
			LayoutPanel centerPanel = new LayoutPanel();
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		}
	}

	private UI ui;

	@Override
	public void createWidget() {
		this.ui = new UI();
	}

	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	@Override
	public IsWidget getWidget() {
		return ui;
	}

	@Override
	public boolean isActivity() {
		return true;
	}

}
