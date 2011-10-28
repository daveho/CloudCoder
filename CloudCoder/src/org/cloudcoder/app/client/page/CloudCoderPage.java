// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.shared.util.DefaultSubscriptionRegistrar;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Widget;

/**
 * Common superclass for all CloudCoder "pages".
 * Provides helper methods for managing session data and event subscribers.
 */
public abstract class CloudCoderPage {
	private List<Object> sessionObjectList;
	private DefaultSubscriptionRegistrar subscriptionRegistrar;
	private Session session;
	
	/**
	 * Constructor.
	 */
	public CloudCoderPage() {
		this.sessionObjectList = new ArrayList<Object>();
		this.subscriptionRegistrar = new DefaultSubscriptionRegistrar();
	}

	/**
	 * Set the Session object that the page should use.
	 * 
	 * @param session the Session object
	 */
	public void setSession(Session session) {
		this.session = session;
	}
	
	/**
	 * Add an object to the Session.
	 * 
	 * @param obj  object to add to the Session
	 */
	protected void addSessionObject(Object obj) {
		session.add(obj);
		sessionObjectList.add(obj);
	}
	
	/**
	 * Remove all objects added to the Session.
	 */
	protected void removeAllSessionObjects() {
		for (Object obj : sessionObjectList) {
			session.remove(obj.getClass());
		}
	}

	/**
	 * Create this page's widget.
	 */
	public abstract void createWidget();
	
	/**
	 * This method is called after the page's UI has been
	 * added to the DOM tree.
	 */
	public abstract void activate();
	
	/**
	 * This method is called just before the UI is removed
	 * from the client web page.  Subclasses may override this
	 * to do cleanup.
	 */
	public abstract void deactivate();

	/**
	 * @return the Widget that is the UI for this page 
	 */
	public abstract Widget getWidget();
	
	/**
	 * @return the Session object
	 */
	public Session getSession() {
		return session;
	}
	
	/**
	 * Get the SubscriptionRegistrar which keeps track of subscribers
	 * for this view.
	 * 
	 * @return the SubscriptionRegistrar
	 */
	public SubscriptionRegistrar getSubscriptionRegistrar() {
		return subscriptionRegistrar;
	}
}
