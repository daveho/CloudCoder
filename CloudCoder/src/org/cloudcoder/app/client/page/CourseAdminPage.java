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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.CourseAdminProblemListView;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page for performing course admin actions.
 * 
 * @author David Hovemeyer
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
		
		public boolean isEnabledByDefault() {
			return this == NEW;
		}
	}
	
	private class UI extends Composite implements SessionObserver, Subscriber {
		private static final double PROBLEM_BUTTON_BAR_HEIGHT_PX = 28.0;

		private PageNavPanel pageNavPanel;
		private Label courseLabel;
		private Button[] problemButtons;
		private CourseAdminProblemListView courseAdminProblemListView;
		private StatusMessageView statusMessageView;

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
				button.setEnabled(action.isEnabledByDefault());
				problemButtonPanel.add(button);
			}
			
			centerPanel.add(problemButtonPanel);
			centerPanel.setWidgetTopHeight(problemButtonPanel, 0.0, Unit.PX, 28.0, Unit.PX);
			centerPanel.setWidgetLeftRight(problemButtonPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			// Create problems list
			this.courseAdminProblemListView = new CourseAdminProblemListView();
			centerPanel.add(courseAdminProblemListView);
			centerPanel.setWidgetTopBottom(courseAdminProblemListView, PROBLEM_BUTTON_BAR_HEIGHT_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			centerPanel.setWidgetLeftRight(courseAdminProblemListView, 0.0, Unit.PX, 0.0, Unit.PX);
			
			// Create a StatusMessageView
			this.statusMessageView = new StatusMessageView();
			centerPanel.add(statusMessageView);
			centerPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			centerPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		/**
		 * Called when a problem button is clicked.
		 * 
		 * @param action the ProblemButtonAction
		 */
		protected void onProblemButtonClick(ButtonPanelAction action) {
			switch (action) {
			case NEW:
				handleNewProblem();
				break;

			case EDIT:
				handleEditProblem();
				break;
				
			case MAKE_VISIBLE:
			case MAKE_INVISIBLE:
			case QUIZ:
			case SHARE:
				Window.alert("Not implemented yet, sorry");
				break;
			}
		}

		private void handleEditProblem() {
			// Get the full ProblemAndTestCaseList for the chosen Problem
			final Problem chosen = getSession().get(Problem.class);
			
			RPC.getCoursesAndProblemsService.getTestCasesForProblem(chosen.getProblemId(), new AsyncCallback<TestCase[]>() {
				@Override
				public void onFailure(Throwable caught) {
					getSession().add(new StatusMessage(
							StatusMessage.Category.ERROR, "Could not load test cases for problem: " + caught.getMessage()));
				}

				@Override
				public void onSuccess(TestCase[] result) {
					ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
					problemAndTestCaseList.setProblem(chosen);
					problemAndTestCaseList.setTestCaseList(result);
					
					getSession().add(problemAndTestCaseList);
					getSession().notifySubscribers(Session.Event.EDIT_PROBLEM, problemAndTestCaseList);
				}
			});
		}
		
		private void handleNewProblem() {
			Problem problem = Problem.createEmpty();
			problem.setCourseId(getSession().get(Course.class).getId());
			TestCase[] testCaseList= new TestCase[0];
			ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
			problemAndTestCaseList.setProblem(problem);
			problemAndTestCaseList.setTestCaseList(testCaseList);
			getSession().add(problemAndTestCaseList);
			getSession().notifySubscribers(Session.Event.EDIT_PROBLEM, problemAndTestCaseList);
		}

		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
			
			// Activate views
			pageNavPanel.setBackHandler(new BackHomeHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			courseAdminProblemListView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);
			
			// The session should contain a course
			Course course = session.get(Course.class);
			courseLabel.setText(course.getName() + " - " + course.getTitle());
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == Session.Event.ADDED_OBJECT && (hint instanceof Problem)) {
				onSelectProblem((Problem) hint);
			}
			
		}

		private void onSelectProblem(Problem problem) {
			// Problem selected: enable/disable buttons appropriately
			problemButtons[ButtonPanelAction.EDIT.ordinal()].setEnabled(true);
			problemButtons[ButtonPanelAction.MAKE_VISIBLE.ordinal()].setEnabled(!problem.isVisible());
			problemButtons[ButtonPanelAction.MAKE_INVISIBLE.ordinal()].setEnabled(problem.isVisible());
			problemButtons[ButtonPanelAction.QUIZ.ordinal()].setEnabled(true);
			problemButtons[ButtonPanelAction.SHARE.ordinal()].setEnabled(true);
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
