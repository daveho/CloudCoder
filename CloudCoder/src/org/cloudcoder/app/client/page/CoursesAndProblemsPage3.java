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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.LoginIndicator;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * Home page providing access to exercises, account information,
 * and the playground.
 * 
 * @author David Hovemeyer
 */
public class CoursesAndProblemsPage3 extends CloudCoderPage {
	private class UI extends Composite implements SessionObserver, Subscriber {
		
		public UI() {
			TabLayoutPanel panel = new TabLayoutPanel(32.0, Unit.PX);
			
			// Exercises tab
			LayoutPanel exercises = new LayoutPanel();
			
			
			
			panel.add(exercises, "Exercises");
			
			
			// Account tab
			LayoutPanel account = new LayoutPanel();
			
			panel.add(account, "Account");
			
			
			// Playground tab
			
			
			LayoutPanel playground = new LayoutPanel();
			
			
			panel.add(playground, "Playground");
			
			
			initWidget(panel);
		}

		@Override
		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void createWidget() {
		setWidget(new UI());
	}

	@Override
	public Class<?>[] getRequiredPageObjects() {
		// This page does not require any page objects other than the
		// ones that are added automatically when the user logs in.
		return new Class<?>[0];
	}

	@Override
	public void activate() {
		getSession().add(new ProblemAndSubmissionReceipt[0]);
		
		// If the user just logged in, add a help message indicating that he/she
		// should click on a course to get started.
		if (getSession().get(LoginIndicator.class) != null) {
			getSession().add(StatusMessage.information("Select a course (on the left hand side) to get started"));
			getSession().remove(LoginIndicator.class);
		}
		
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public PageId getPageId() {
		return PageId.COURSES_AND_PROBLEMS;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		// Nothing to do: this is the home page
	}

}
