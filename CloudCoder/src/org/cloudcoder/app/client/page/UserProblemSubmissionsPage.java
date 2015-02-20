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

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.ProblemSubmissionHistory;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemSubmissionHistorySliderView;
import org.cloudcoder.app.client.view.ReadOnlyProblemTextView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestOutcomeSummaryView;
import org.cloudcoder.app.client.view.TestResultListView;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * Page for viewing a user's submissions on a particular {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class UserProblemSubmissionsPage extends CloudCoderPage {
	private class UI extends Composite implements SessionObserver, Subscriber {
		private static final double SLIDER_HEIGHT_PX = 28.0;
		
		private Label usernameAndProblemLabel;
		private PageNavPanel pageNavPanel;
		private ProblemSubmissionHistorySliderView sliderView;
		private ReadOnlyProblemTextView problemTextView;
		private StatusMessageView statusMessageView;
		private TestOutcomeSummaryView testOutcomeSummaryView;
		private TestResultListView testResultListView;

		public UI() {
			SplitLayoutPanel panel = new SplitLayoutPanel();
			
			// North panel has username/problem name, page nav panel,
			// and slider to select the user's submissions.
			LayoutPanel northPanel = new LayoutPanel();
			
			this.usernameAndProblemLabel = new Label("");
			usernameAndProblemLabel.setStyleName("cc-problemName", true);
			northPanel.add(usernameAndProblemLabel);
			northPanel.setWidgetLeftRight(usernameAndProblemLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(usernameAndProblemLabel, 0.0, Unit.PX, 22.0, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);

			this.sliderView = new ProblemSubmissionHistorySliderView();
			northPanel.add(sliderView);
			northPanel.setWidgetTopHeight(sliderView, PageNavPanel.HEIGHT_PX, Unit.PX, ProblemSubmissionHistorySliderView.HEIGHT_PX, Unit.PX);
			northPanel.setWidgetLeftRight(sliderView, 0.0, Unit.PX, 0.0, Unit.PX);
			
			panel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + 8.0 + SLIDER_HEIGHT_PX);
			
			// South panel has status message view, test outcome summary view,
			// and test result view.  TODO: indicate whether or not the sumission compiled.
			LayoutPanel southPanel = new LayoutPanel();
			
			this.statusMessageView = new StatusMessageView();
			southPanel.add(statusMessageView);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, TestOutcomeSummaryView.WIDTH_PX, Unit.PX);
			southPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			
			this.testOutcomeSummaryView = new TestOutcomeSummaryView();
			southPanel.add(testOutcomeSummaryView);
			southPanel.setWidgetRightWidth(testOutcomeSummaryView, 0.0, Unit.PX, TestOutcomeSummaryView.WIDTH_PX, Unit.PX);
			southPanel.setWidgetTopHeight(testOutcomeSummaryView, 0.0, Unit.PX, TestOutcomeSummaryView.HEIGHT_PX, Unit.PX);
			
			this.testResultListView = new TestResultListView();
			southPanel.add(testResultListView);
			southPanel.setWidgetLeftRight(testResultListView, 0.0, Unit.PX, 0.0, Unit.PX);
			southPanel.setWidgetTopBottom(testResultListView, TestOutcomeSummaryView.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			
			panel.addSouth(southPanel, 200.0);
			
			// Center panel has a ReadOnlyProblemTextView
			this.problemTextView = new ReadOnlyProblemTextView();
			panel.add(problemTextView);
			
			initWidget(panel);
		}

		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// Add a ProblemSubmissionHistory object to the session.
			ProblemSubmissionHistory history = new ProblemSubmissionHistory();
			session.add(history);
			
			// FIXME: for now, subscribe the page directly to the ProblemSubmissionHistory
			// Eventually, only the various views should be subscribed
			history.subscribe(ProblemSubmissionHistory.Event.SET_SUBMISSION_RECEIPT_LIST, this, subscriptionRegistrar);
			history.subscribe(ProblemSubmissionHistory.Event.SET_SELECTED, this, subscriptionRegistrar);
			
			// FIXME: also subscribe to session add object events
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
			
			// Show username, problem name and description
			Problem problem = session.get(Problem.class);
			UserSelection userSelection = session.get(UserSelection.class);
			usernameAndProblemLabel.setText(userSelection.getUser().getUsername() + ", " + problem.toNiceString());
			
			// Activate views
			pageNavPanel.setBackHandler(new PageBackHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			sliderView.activate(session, subscriptionRegistrar);
			problemTextView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);
			testOutcomeSummaryView.activate(session, subscriptionRegistrar);
			testResultListView.activate(session, subscriptionRegistrar);
			
			// Get all SubmissionReceipts for this user on this problem
			RPC.getCoursesAndProblemsService.getAllSubmissionReceiptsForUser(problem, userSelection.getUser(), new AsyncCallback<SubmissionReceipt[]>() {
				@Override
				public void onSuccess(SubmissionReceipt[] result) {
					onLoadSubmissionReceipts(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					session.add(StatusMessage.error("Could not get submission receipts", caught));
				}
			});
		}

		private void onLoadSubmissionReceipts(SubmissionReceipt[] result) {
			GWT.log("Loaded submission receipts");
			
			// Add to ProblemSubmissionHistory
			getSession().get(ProblemSubmissionHistory.class).setSubmissionReceiptList(result);
			
			if (result.length == 0) {
				// The user has no submissions for this problem
				getSession().add(StatusMessage.information("There are no submissions to view"));
				// Add empty test result list
				getSession().add(new NamedTestResult[0]);
				return;
			}
			
			// Find the best submission
			int best = 0;
			for (int i = 1; i < result.length; i++) {
				if (result[i].getNumTestsPassed() > result[best].getNumTestsPassed()) {
					best = i;
				}
			}
			GWT.log("Best submission is " + best);
			
			// Set selected Submission to best one
			getSession().get(ProblemSubmissionHistory.class).setSelected(best);
			
		}
		
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == ProblemSubmissionHistory.Event.SET_SELECTED) {
				GWT.log("Setting selected submission");
				ProblemSubmissionHistory problemSubmissionHistory = getSession().get(ProblemSubmissionHistory.class);
				int selected = problemSubmissionHistory.getSelected();
				
				// Load text
				Problem problem = getSession().get(Problem.class);
				UserSelection userSelection = getSession().get(UserSelection.class);
				
				// Determine which SubmissionReceipt was selected
				SubmissionReceipt receipt = problemSubmissionHistory.getSubmissionReceipt(selected);
				
				// Load the ProblemText, add to the session
				GWT.log("Loading ProblemText for submission " + selected);
				RPC.editCodeService.getSubmissionText(userSelection.getUser(), problem, receipt, new AsyncCallback<ProblemText>() {
					@Override
					public void onFailure(Throwable caught) {
						getSession().add(StatusMessage.error("Couldn't get text for submission", caught));
					}
					
					@Override
					public void onSuccess(ProblemText result) {
						//editor.setText(result.getText());
						GWT.log("Problem text loaded");
						getSession().add(result);
					}
				});

				// Get test results
				GWT.log("Getting test results for submission");
				RPC.getCoursesAndProblemsService.getTestResultsForSubmission(problem, receipt, new AsyncCallback<NamedTestResult[]>() {
					@Override
					public void onFailure(Throwable caught) {
						getSession().add(StatusMessage.error("Couldn't load test results for submission", caught));
					}
					
					public void onSuccess(NamedTestResult[] result) {
						getSession().add(result);
					}
				});
			}
		}
	}

	@Override
	public void createWidget() {
		setWidget(new UI());
	}
	
	@Override
	public Class<?>[] getRequiredPageObjects() {
		return new Class<?>[]{
				// Which course the problem was assigned in
				CourseSelection.class,
				// The Problem
				Problem.class,
				// Course registrations for the logged-in user (to check instructor status)
				CourseAndCourseRegistration[].class,
				// List of students registered in the course
				User[].class,
				// Selected user
				UserSelection.class
		};
	}

	@Override
	public void activate() {
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public PageId getPageId() {
		return PageId.USER_PROBLEM_SUBMISSIONS;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
		pageStack.push(PageId.PROBLEM_ADMIN);
		pageStack.push(PageId.STATISTICS);
	}

}
