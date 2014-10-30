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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.UserAccountView;
import org.cloudcoder.app.client.view.UserAccountView2;
import org.cloudcoder.app.client.view.UserProgressListView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author jspacco
 * @author apapance
 * @author sbonner1 
 *
 */
public class UserAccountPage2 extends CloudCoderPage
{
	private class UI extends Composite implements SessionObserver, Subscriber {
		private static final double USERS_BUTTON_BAR_HEIGHT_PX = 28.0;

		private PageNavPanel pageNavPanel;
		private String rawCourseTitle;
		private Label header;
		private UserAccountView2 userAccountView; //***LINE TO FIX ACCOUNT VIEWS
		private StatusMessageView statusMessageView;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);

			// Create a north panel with course info and a PageNavPanel
			LayoutPanel northPanel = new LayoutPanel();
			this.header = new Label();
			northPanel.add(header);
			northPanel.setWidgetLeftRight(header, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopHeight(header, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
			header.setStyleName("Account Settings");

			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);

			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX);

			// Create a center panel with user button panel and list of users 
			// registered for the given course.
			// Can eventually put other stuff here too.
			LayoutPanel centerPanel = new LayoutPanel();

			// Create users list
			this.userAccountView = new UserAccountView2();
			centerPanel.add(userAccountView);
			centerPanel.setWidgetTopBottom(userAccountView, USERS_BUTTON_BAR_HEIGHT_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			centerPanel.setWidgetLeftRight(userAccountView, 0.0, Unit.PX, 0.0, Unit.PX);

			// Create a StatusMessageView
			this.statusMessageView = new StatusMessageView();
			centerPanel.add(statusMessageView) ;
			centerPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			centerPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);

			dockLayoutPanel.add(centerPanel);

			initWidget(dockLayoutPanel);
		}
		
		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

			// Activate views
			pageNavPanel.setBackHandler(new PageBackHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			userAccountView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);

			// The session should contain a course
			rawCourseTitle = "Account Settings";
			header.setText(rawCourseTitle);
			header.setStyleName("cc-dialogTitle");
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		}

		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		setWidget(new UI());
	}

	@Override
	public Class<?>[] getRequiredPageObjects() {
		return new Class<?>[]{ CourseSelection.class, User.class }; //get rid of this
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public PageId getPageId() {
		return PageId.USER_ACCOUNT;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
	}
}
