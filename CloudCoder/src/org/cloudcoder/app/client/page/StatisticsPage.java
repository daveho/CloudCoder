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

import org.cloudcoder.app.client.model.CourseSelection;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.StudentProgressView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page for displaying statistics for a given {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class StatisticsPage extends CloudCoderPage {
	
	private class UI extends Composite implements Subscriber {
		private PageNavPanel pageNavPanel;
		private Label problemLabel;
		private StatusMessageView statusMessageView;
		private StudentProgressView studentProgressView;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);

			// North panel: problem label, page nav panel, stats options (TODO)
			LayoutPanel northPanel = new LayoutPanel();
			
			// page nav panel
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			
			// label to display the problem name
			this.problemLabel = new Label("");
			problemLabel.setStyleName("cc-problemName", true);
			northPanel.add(problemLabel);
			northPanel.setWidgetLeftRight(problemLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(problemLabel, 0.0, Unit.PX, 22.0, Unit.PX);
			
			// TODO: stats options (choose section, sorting)
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX);
			
			// South panel: just a status message view
			statusMessageView = new StatusMessageView();
			dockLayoutPanel.addSouth(statusMessageView, StatusMessageView.HEIGHT_PX);
			
			// Center panel: stats view
			LayoutPanel centerPanel = new LayoutPanel();
			this.studentProgressView = new StudentProgressView();
			centerPanel.add(studentProgressView);
			centerPanel.setWidgetLeftRight(studentProgressView, 0.0, Unit.PX, 0.0, Unit.PX);
			centerPanel.setWidgetTopBottom(studentProgressView, 10.0, Unit.PX, 10.0, Unit.PX);
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// Activate views
			statusMessageView.activate(session, subscriptionRegistrar);
			studentProgressView.activate(session, subscriptionRegistrar);
			
			// Set title
			Problem problem = session.get(Problem.class);
			CourseSelection courseSelection = session.get(CourseSelection.class);
			problemLabel.setText("Statistics for " + problem.toNiceString() + " in " + courseSelection.getCourse().getName());
			
			// Set back/logout handlers
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					// Go back to course admin page
					session.notifySubscribers(Session.Event.COURSE_ADMIN, session.get(Course.class));
				}
			});
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
		}
		
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			// TODO Auto-generated method stub
			
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
