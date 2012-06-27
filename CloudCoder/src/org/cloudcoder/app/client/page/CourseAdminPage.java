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

import java.awt.FlowLayout;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * @author David Hovemeyer
 *
 */
public class CourseAdminPage extends CloudCoderPage {
	private enum ButtonPanelAction {
		NEW("New problem"),
		EDIT("Edit problem"),
		MAKE_VISIBLE("Make visible"),
		MAKE_INVISIBLE("Make invisible"),
		QUIZ("Quiz"),
		SHARE("Share");
		
		private String name;
		
		private ButtonPanelAction(String name) {
			this.name = name;
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
	}
	
	private class UI extends Composite implements SessionObserver, Subscriber {
		private PageNavPanel pageNavPanel;
		private Label courseLabel;
		private Button[] problemButtons;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// Create a north panel with course info and a PageNavPanel
			LayoutPanel northPanel = new LayoutPanel();
			
			this.courseLabel = new Label();
			northPanel.add(courseLabel);
			northPanel.setWidgetLeftRight(courseLabel, 0.0, Unit.PX, PageNavPanel.WIDTH, PageNavPanel.WIDTH_UNIT);
			northPanel.setWidgetTopHeight(courseLabel, 0.0, Unit.PX, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT);
			courseLabel.setStyleName("cc-courseLabel");
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH, PageNavPanel.WIDTH_UNIT);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT);
			
			// Create a center panel with problem button panel and problems list.
			// Can eventually put other stuff here too.
			LayoutPanel centerPanel = new LayoutPanel();

			// Create a button panel with buttons for problem-related actions
			// (new problem, edit problem, make visible, make invisible, quiz, share)
			FlowPanel problemButtonPanel = new FlowPanel();
			ButtonPanelAction[] actions = ButtonPanelAction.values();
			problemButtons = new Button[actions.length];
			for (final ButtonPanelAction action : actions) {
				final Button button = new Button(action.getName());
				problemButtons[action.ordinal()] = button;
				button.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						onProblemButtonClick(action);
					}
				});
				problemButtonPanel.add(button);
			}
			
			centerPanel.add(problemButtonPanel);
			
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		/**
		 * Called when a problem button is clicked.
		 * 
		 * @param action the ProblemButtonAction
		 */
		protected void onProblemButtonClick(ButtonPanelAction action) {
			// TODO Auto-generated method stub
			
		}

		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
			
			// Activate views
			pageNavPanel.setBackHandler(new BackHomeHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			
			// The session should contain a course
			Course course = session.get(Course.class);
			courseLabel.setText(course.getName() + " - " + course.getTitle());
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			// TODO Auto-generated method stub
			
		}
	}

	private UI ui;

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		ui = new UI();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#deactivate()
	 */
	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#getWidget()
	 */
	@Override
	public IsWidget getWidget() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
	 */
	@Override
	public boolean isActivity() {
		return true;
	}

}
