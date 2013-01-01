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
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Page allowing an instructor to administer a quiz for a {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class QuizPage extends CloudCoderPage {
	
	private class UI extends Composite {
		private ListBox sectionListBox;
		private StatusMessageView statusMessageView;
		private Button startQuizButton;

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
			
			LayoutPanel southPanel = new LayoutPanel();
			this.statusMessageView = new StatusMessageView();
			southPanel.add(statusMessageView);
			southPanel.setWidgetTopBottom(statusMessageView, 0, Unit.PX, 0, Unit.PX);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			dockLayoutPanel.addSouth(southPanel, StatusMessageView.HEIGHT_PX);
			
			LayoutPanel centerPanel = new LayoutPanel();

			Label sectionLabel = new Label("Choose section:");
			centerPanel.add(sectionLabel);
			centerPanel.setWidgetLeftWidth(sectionLabel, 40, Unit.PX, 180, Unit.PX);
			centerPanel.setWidgetTopHeight(sectionLabel, 20, Unit.PX, 20, Unit.PX);
			this.sectionListBox = new ListBox();
			centerPanel.add(sectionListBox);
			centerPanel.setWidgetLeftWidth(sectionListBox, 40, Unit.PX, 140, Unit.PX);
			centerPanel.setWidgetTopHeight(sectionListBox, 44, Unit.PX, 28, Unit.PX);
			this.startQuizButton = new Button("Start Quiz");
			centerPanel.add(startQuizButton);
			centerPanel.setWidgetLeftWidth(startQuizButton, 40, Unit.PX, 140, Unit.PX);
			centerPanel.setWidgetTopHeight(startQuizButton, 86, Unit.PX, 32, Unit.PX);
			startQuizButton.setEnabled(false);
			startQuizButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					doStartQuiz();
				}
			});
			
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		protected void doStartQuiz() {
			// TODO
		}

		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			statusMessageView.activate(session, subscriptionRegistrar);
			loadCourseRegistrations();
		}

		// Load user's course registrations so we know which sections he/she
		// is an instructor for.
		private void loadCourseRegistrations() {
			final Problem problem = getSession().get(Problem.class);
			
			RPC.getCoursesAndProblemsService.getCourseAndCourseRegistrations(new AsyncCallback<CourseAndCourseRegistration[]>() {
				@Override
				public void onSuccess(CourseAndCourseRegistration[] result) {
					int numSections = 0;
					for (CourseAndCourseRegistration ccr : result) {
						if (ccr.getCourse().getId() == problem.getCourseId()
								&& ccr.getCourseRegistration().getRegistrationType().isInstructor()) {
							Integer section = (Integer)ccr.getCourseRegistration().getSection();
							sectionListBox.addItem(section.toString());
							numSections++;
						}
					}
					if (numSections > 0) {
						startQuizButton.setEnabled(true);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
				}
			});
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
