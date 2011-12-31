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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.ProblemListView2;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TermAndCourseTreeView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * @author David Hovemeyer
 */
public class CoursesAndProblemsPage2 extends CloudCoderPage {
	/**
	 * UI class for CoursesAndProblemsPage2.
	 */
	private class UI extends Composite implements SessionObserver, Subscriber {
		private LayoutPanel eastLayoutPanel;

		private TermAndCourseTreeView termAndCourseTreeView;
		private ProblemDescriptionView problemDescriptionView;
		private StatusMessageView statusMessageView;
		private ProblemListView2 problemListView2;

		private Button loadProblemButton;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
			
			this.eastLayoutPanel = new LayoutPanel();
			
			dockLayoutPanel.addEast(eastLayoutPanel, 24.0);
			
			LayoutPanel southLayoutPanel = new LayoutPanel();
			
			this.statusMessageView = new StatusMessageView();
			southLayoutPanel.add(statusMessageView);
			southLayoutPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			southLayoutPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT, StatusMessageView.HEIGHT_UNIT);
			
			this.loadProblemButton = new Button("Load problem!");
			loadProblemButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadProblemButtonClicked();
				}
			});
			southLayoutPanel.add(loadProblemButton);
			southLayoutPanel.setWidgetRightWidth(loadProblemButton, 0.0, Unit.PX, 160.0, Unit.PX);
			southLayoutPanel.setWidgetTopHeight(loadProblemButton, StatusMessageView.HEIGHT, StatusMessageView.HEIGHT_UNIT, StatusMessageView.HEIGHT, StatusMessageView.HEIGHT_UNIT);
			
			this.problemDescriptionView = new ProblemDescriptionView();
			southLayoutPanel.add(problemDescriptionView);
			southLayoutPanel.setWidgetLeftRight(problemDescriptionView, 0.0, Unit.PX, 0.0, Unit.PX);
			southLayoutPanel.setWidgetTopBottom(problemDescriptionView, StatusMessageView.HEIGHT*2, StatusMessageView.HEIGHT_UNIT, 0.0, Unit.PX);
			
			dockLayoutPanel.addSouth(southLayoutPanel, 15.0);
			
			this.problemListView2 = new ProblemListView2();
			dockLayoutPanel.add(problemListView2);
			
			initWidget(dockLayoutPanel);
		}

		private void loadProblemButtonClicked() {
			Problem problem = getSession().get(Problem.class);
			if (problem != null) {
				getSession().notifySubscribers(Session.Event.PROBLEM_CHOSEN, problem);
			}
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

			// activate views
			problemListView2.activate(session, subscriptionRegistrar);
			problemDescriptionView.activate(session, subscriptionRegistrar);
			
			// Load courses
			RPC.getCoursesAndProblemsService.getCourses(new AsyncCallback<Course[]>() {
				@Override
				public void onSuccess(Course[] result) {
					GWT.log(result.length + " course(s) loaded");
					addSessionObject(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					GWT.log("Error loading courses", caught);
					session.add(new StatusMessage(StatusMessage.Category.ERROR, "Error loading courses: " + caught.getMessage()));
				}
			});
		}
		
		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == Session.Event.ADDED_OBJECT && hint instanceof Course[]) {
				// Courses are loaded - create and activate TermAndCourseTreeView
				termAndCourseTreeView = new TermAndCourseTreeView((Course[]) hint);
				eastLayoutPanel.add(termAndCourseTreeView);
				eastLayoutPanel.setWidgetLeftRight(termAndCourseTreeView, 8.0, Unit.PX, 0.0, Unit.PX);
				eastLayoutPanel.setWidgetTopBottom(termAndCourseTreeView, 0.0, Unit.PX, 0.0, Unit.PX);
				
				// add selection event handler
				termAndCourseTreeView.addSelectionHandler(new SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						Course course = termAndCourseTreeView.getSelectedCourse();
						getSession().add(course);
					}
				});
			} else if (key == Session.Event.ADDED_OBJECT && hint instanceof Course) {
				Course course = (Course) hint;
				
				// Load problems
				RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error loading problems", caught);
						getSession().add(new StatusMessage(StatusMessage.Category.ERROR, "Error loading problems: " + caught.getMessage()));
					}

					@Override
					public void onSuccess(ProblemAndSubmissionReceipt[] result) {
						// Add ProblemAndSubmissionReceipt list to session so that
						// ProblemListView2 will know about it
						getSession().add(result);
					}
				});
			}
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
		getSession().add(new ProblemAndSubmissionReceipt[0]);
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

}
