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

package org.cloudcoder.app.client;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.CoursesAndProblemsPage2;
import org.cloudcoder.app.client.page.DevelopmentPage;
import org.cloudcoder.app.client.page.LoginPage;
import org.cloudcoder.app.shared.util.DefaultSubscriptionRegistrar;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * CloudCoder entry point class.
 */
public class CloudCoder implements EntryPoint, Subscriber {
	private Session session;
	private SubscriptionRegistrar subscriptionRegistrar;
	private CloudCoderPage currentPage;
	
	private LayoutPanel layoutPanel;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		session = new Session();
		subscriptionRegistrar = new DefaultSubscriptionRegistrar();

		// Subscribe to all Session events
		session.subscribeToAll(Session.Event.values(), this, subscriptionRegistrar);
		
		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
		
		layoutPanel = new LayoutPanel();
		rootLayoutPanel.add(layoutPanel);
		rootLayoutPanel.setWidgetLeftRight(layoutPanel, 10.0, Unit.PX, 10.0, Unit.PX);
		rootLayoutPanel.setWidgetTopBottom(layoutPanel, 10.0, Unit.PX, 10.0, Unit.PX);
		
		changePage(new LoginPage());
	}
	
	private void changePage(CloudCoderPage page) {
		if (currentPage != null) {
			currentPage.deactivate();
			layoutPanel.remove(currentPage.getWidget());
			
			// make sure there is no StatusMessage from the previous page
			session.remove(StatusMessage.class);
		}
		page.setSession(session);
		// Create the page's Widget and add it to the DOM tree
		page.createWidget();
		IsWidget w = page.getWidget();
		layoutPanel.add(w);
		layoutPanel.setWidgetLeftRight(w, 0.0, Unit.PX, 0.0, Unit.PX);
		layoutPanel.setWidgetTopBottom(w, 0.0, Unit.PX, 0.0, Unit.PX);
		// Now it is safe to activate the page
		page.activate();
		currentPage = page;
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.LOGIN || key == Session.Event.BACK_HOME) {
			//changePage(new CoursesAndProblemsPage());
			changePage(new CoursesAndProblemsPage2());
		} else if (key == Session.Event.PROBLEM_CHOSEN) {
			changePage(new DevelopmentPage());
		} else if (key == Session.Event.LOGOUT) {
			changePage(new LoginPage());
		}
		
	}
}
