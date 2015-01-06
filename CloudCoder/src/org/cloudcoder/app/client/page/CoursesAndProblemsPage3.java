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
import org.cloudcoder.app.client.view.ExerciseSummaryView;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.ProblemListView3;
import org.cloudcoder.app.client.view.SectionLabel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TermAndCourseTreeView;
import org.cloudcoder.app.client.view.UserAccountView2;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Home page providing access to exercises, account information,
 * and the playground.  This is the third iteration of the homepage.
 * 
 * @author David Hovemeyer
 */
public class CoursesAndProblemsPage3 extends CloudCoderPage {
	private class UI extends Composite implements SessionObserver, Subscriber {
		private static final double WEST_PANEL_WIDTH_PX = 240.0;
		private static final double SEP_PX = 10.0; // sep between west and center panel
		private static final double REFRESH_BUTTON_WIDTH_PX = 60.0;
		private static final double LOAD_EXERCISE_BUTTON_WIDTH_PX = 120.0;
		private static final double PROGRESS_SUMMARY_HEIGHT_PX = 240.0;
		private static final double ADMIN_BUTTON_HEIGHT_PX = 32.0;
		private static final double COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX = ADMIN_BUTTON_HEIGHT_PX*2 + 4.0;
		
		private PageNavPanel pageNavPanel;
		private StatusMessageView statusMessageView;
		private ScrollPanel termAndCourseTreeViewScrollPanel; // term and course tree view will go here
		private LayoutPanel west;
		private ProblemDescriptionView problemDescriptionView;
		private ExerciseSummaryView progressSummaryView;
		private ProblemListView3 exerciseList;
		private UserAccountView2 userAccountView;
		private Button manageExercisesButton;
		private Button manageUsersButton;

		
		public UI() {
			LayoutPanel full = new LayoutPanel();
			
			Label pageTitle = new Label("Welcome to CloudCoder!");
			pageTitle.setStyleName("cc-pageTitle", true);
			full.add(pageTitle);
			full.setWidgetLeftRight(pageTitle, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			full.setWidgetTopHeight(pageTitle, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			full.add(pageNavPanel);
			full.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			full.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			pageNavPanel.setShowBackButton(false);
			
			TabLayoutPanel panel = new TabLayoutPanel(32.0, Unit.PX);
			
			// Exercises tab
			IsWidget exercises = createExercisesTab();
			panel.add(exercises, "Exercises");
			
			// Account tab
			IsWidget account = createAccountTab();
			panel.add(account, "Account");
			
			// Playground tab
			IsWidget playground = createPlaygroundTab();
			panel.add(playground, "Playground");
			
			full.add(panel);
			full.setWidgetLeftRight(panel, 0.0, Unit.PX, 0.0, Unit.PX);
			full.setWidgetTopBottom(panel, PageNavPanel.HEIGHT_PX - 8.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);

			this.statusMessageView = new StatusMessageView();
			full.add(statusMessageView);
			full.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			full.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			
			initWidget(full);
		}

		private IsWidget createExercisesTab() {
			LayoutPanel wrap = new LayoutPanel();
			
			DockLayoutPanel exercises = new DockLayoutPanel(Unit.PX);
			
			this.west = new LayoutPanel();
			SectionLabel coursesLabel = new SectionLabel("Courses");
			west.add(coursesLabel);
			west.setWidgetLeftRight(coursesLabel, 0.0, Unit.PX, SEP_PX, Unit.PX);
			west.setWidgetTopHeight(coursesLabel, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			// term and course tree view will be added dynamically when page is activated
			exercises.addWest(west, WEST_PANEL_WIDTH_PX);
			
			LayoutPanel east = new LayoutPanel();
			SectionLabel exerciseDescriptionLabel = new SectionLabel("Exercise description");
			east.add(exerciseDescriptionLabel);
			east.setWidgetLeftRight(exerciseDescriptionLabel, SEP_PX, Unit.PX, 0.0, Unit.PX);
			east.setWidgetTopHeight(exerciseDescriptionLabel, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			
			this.problemDescriptionView = new ProblemDescriptionView();
			east.add(problemDescriptionView);
			east.setWidgetLeftRight(problemDescriptionView, SEP_PX, Unit.PX, 0.0, Unit.PX);
			east.setWidgetTopBottom(problemDescriptionView, SectionLabel.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			exercises.addEast(east, WEST_PANEL_WIDTH_PX + 80.0);
			
			SplitLayoutPanel center = new SplitLayoutPanel();
			LayoutPanel south = new LayoutPanel();
			double southTop = 10.0;
			SectionLabel progressSummaryLabel = new SectionLabel("Progress summary");
			south.add(progressSummaryLabel);
			south.setWidgetLeftRight(progressSummaryLabel, 0.0, Unit.PX, 0.0, Unit.PX);
			south.setWidgetTopHeight(progressSummaryLabel, southTop+0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			this.progressSummaryView = new ExerciseSummaryView();
			south.add(progressSummaryView);
			south.setWidgetLeftRight(progressSummaryView, 0.0, Unit.PX, 0.0, Unit.PX);
			south.setWidgetTopBottom(progressSummaryView, southTop+SectionLabel.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			center.addSouth(south, PROGRESS_SUMMARY_HEIGHT_PX);
			LayoutPanel top = new LayoutPanel();
			SectionLabel exercisesLabel = new SectionLabel("Exercises");
			top.add(exercisesLabel);
			double r = REFRESH_BUTTON_WIDTH_PX + 4.0 + LOAD_EXERCISE_BUTTON_WIDTH_PX;
			top.setWidgetLeftRight(exercisesLabel, 0.0, Unit.PX, r, Unit.PX);
			top.setWidgetTopHeight(exercisesLabel, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			Button refreshButton = new Button("Refresh");
			top.add(refreshButton);
			top.setWidgetRightWidth(refreshButton, LOAD_EXERCISE_BUTTON_WIDTH_PX + 4.0, Unit.PX, REFRESH_BUTTON_WIDTH_PX, Unit.PX);
			top.setWidgetTopHeight(refreshButton, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			refreshButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleRefreshButtonPress();
				}
			});
			Button loadExerciseButton = new Button("Load exercise");
			loadExerciseButton.setStyleName("cc-emphButton", true);
			top.add(loadExerciseButton);
			top.setWidgetRightWidth(loadExerciseButton, 0.0, Unit.PX, LOAD_EXERCISE_BUTTON_WIDTH_PX, Unit.PX);
			top.setWidgetTopHeight(loadExerciseButton, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			loadExerciseButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleLoadExerciseButtonPress();
				}
			});
			this.exerciseList = new ProblemListView3(CoursesAndProblemsPage3.this);
			top.add(exerciseList);
			top.setWidgetLeftRight(exerciseList, 0.0, Unit.PX, 0.0, Unit.PX);
			top.setWidgetTopBottom(exerciseList, SectionLabel.HEIGHT_PX + 8.0, Unit.PX, 0.0, Unit.PX);
			
			center.add(top);
			
			exercises.add(center);
			
			wrap.add(exercises);
			wrap.setWidgetLeftRight(exercises, 10.0, Unit.PX, 10.0, Unit.PX);
			wrap.setWidgetTopBottom(exercises, 10.0, Unit.PX, 10.0, Unit.PX);
			
			return wrap;
		}

		private IsWidget createAccountTab() {
			// This is super easy
			LayoutPanel wrap = new LayoutPanel();
			this.userAccountView = new UserAccountView2(CoursesAndProblemsPage3.this);
			wrap.add(userAccountView);
			wrap.setWidgetLeftRight(userAccountView, 10.0, Unit.PX, 10.0, Unit.PX);
			wrap.setWidgetTopBottom(userAccountView, 10.0, Unit.PX, 10.0, Unit.PX);
			return wrap;
		}

		protected void handleRefreshButtonPress() {
			CourseSelection courseSelection = getSession().get(CourseSelection.class);
			if (courseSelection != null) {
				// Force a reload of the course
				getSession().add(courseSelection);
			}
		}

		protected void handleLoadExerciseButtonPress() {
			Problem problem = getSession().get(Problem.class);
			if (problem != null) {
				// Switch to DevelopmentPage
				getSession().get(PageStack.class).push(PageId.DEVELOPMENT);
			}
		}
		
		private IsWidget createPlaygroundTab() {
			LayoutPanel panel = new LayoutPanel();
			
			double top = 30.0;
			
			Label playgroundDesc = new Label("The playground allows free-form programming.");
			panel.add(playgroundDesc);
			panel.setWidgetLeftRight(playgroundDesc, 40.0, Unit.PX, 0.0, Unit.PX);
			panel.setWidgetTopHeight(playgroundDesc, top, Unit.PX, 24.0, Unit.PX);
			
			Button playgroundButton = new Button("Enter the playground!");
			playgroundButton.setStyleName("cc-emphButton", true);
			panel.add(playgroundButton);
			panel.setWidgetLeftWidth(playgroundButton, 40.0, Unit.PX, 240.0, Unit.PX);
			panel.setWidgetTopHeight(playgroundButton, top + 30.0, Unit.PX, 32.0, Unit.PX);
			
			playgroundButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handlePlaygroundButtonPress();
				}
			});
			
			return panel;
		}

		protected void handlePlaygroundButtonPress() {
			getSession().get(PageStack.class).push(PageId.PLAYGROUND_PAGE);
		}

		@Override
		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
			
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));

			statusMessageView.activate(session, subscriptionRegistrar);
			problemDescriptionView.activate(session, subscriptionRegistrar);
			progressSummaryView.activate(session, subscriptionRegistrar);
			exerciseList.activate(session, subscriptionRegistrar);
			userAccountView.activate(session, subscriptionRegistrar);

			// Load courses
			SessionUtil.getCourseAndCourseRegistrationsRPC(CoursesAndProblemsPage3.this, session);
		}

		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseAndCourseRegistration[]) {
				onCourseAndCourseRegistrationsLoaded((CourseAndCourseRegistration[]) hint);
			} else if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseSelection) {
				CourseSelection sel = (CourseSelection) hint;
				onCourseSelected(sel);
			}
		}

		private void onCourseAndCourseRegistrationsLoaded(
				CourseAndCourseRegistration[] courseAndRegList) {
			
			GWT.log("Courses and course registrations loaded...");
			if (termAndCourseTreeViewScrollPanel != null) {
				// It is possible for the CourseAndCourseRegistrations to be loaded
				// multiple times.  Make sure that there is only one
				// TermAndCourseTreeView widget!
				west.remove(termAndCourseTreeViewScrollPanel);
				termAndCourseTreeViewScrollPanel = null;
			}
			
			boolean isInstructor = false;

			// Determine if the user is an instructor for any of the courses
			for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
				if (courseAndReg.getCourseRegistration().getRegistrationType() == CourseRegistrationType.INSTRUCTOR) {
					GWT.log("Instructor for course " +  courseAndReg.getCourse().getName());
					isInstructor = true;
				}
			}
			
			// Courses are loaded - populate TermAndCourseTreeView.
			// If the user is an instructor for at least one course, leave some room for
			// the "Course admin" button.
			// Wrap the TermAndCourseTreeView in a ScrollPanel
			this.termAndCourseTreeViewScrollPanel = new ScrollPanel();
			west.add(termAndCourseTreeViewScrollPanel);
			west.setWidgetTopBottom(
					termAndCourseTreeViewScrollPanel,
					SectionLabel.HEIGHT_PX,
					Unit.PX,
					(isInstructor ? COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX + 8.0 : 0.0),
					Unit.PX);
			west.setWidgetLeftRight(termAndCourseTreeViewScrollPanel, 0.0, Unit.PX, SEP_PX, Unit.PX);
			final TermAndCourseTreeView termAndCourseTreeView = new TermAndCourseTreeView(courseAndRegList);
			termAndCourseTreeViewScrollPanel.add(termAndCourseTreeView);

			// Create the "Problems" and "User" admin buttons if appropriate.
			if (isInstructor && manageExercisesButton == null) {
				this.manageExercisesButton = new Button("Manage exercises");
				west.add(manageExercisesButton);
				west.setWidgetLeftRight(manageExercisesButton, 10.0, Unit.PX, 10.0, Unit.PX);
				west.setWidgetBottomHeight(manageExercisesButton, ADMIN_BUTTON_HEIGHT_PX+4.0, Unit.PX, ADMIN_BUTTON_HEIGHT_PX, Unit.PX);
				manageExercisesButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						handleManageExercisesButtonPress();
					}
				});
				this.manageUsersButton = new Button("Manage users");
				west.add(manageUsersButton);
				west.setWidgetLeftRight(manageUsersButton, 10.0, Unit.PX, 10.0, Unit.PX);
				west.setWidgetBottomHeight(manageUsersButton, 0.0, Unit.PX, ADMIN_BUTTON_HEIGHT_PX, Unit.PX);
				manageUsersButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						handleManageUsersButtonPress();
					}
				});
				
				// Admin buttons are disabled initially, and will be enabled
				// dynamically when a course for which the current user is an
				// administrator is selected
				manageExercisesButton.setEnabled(false);
				manageUsersButton.setEnabled(false);
			}

			// add selection event handler
			termAndCourseTreeView.addSelectionHandler(new SelectionChangeEvent.Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					CourseSelection courseSelection = termAndCourseTreeView.getSelectedCourseAndModule();
					if (courseSelection != null) {
						getSession().add(courseSelection);
					}
				}
			});
		}

		public void onCourseSelected(CourseSelection sel) {
			Course course = sel.getCourse();

			if (manageExercisesButton != null || manageUsersButton != null) {
				// Find the CourseRegistration for this Course
				CourseAndCourseRegistration[] courseAndRegList = getSession().get(CourseAndCourseRegistration[].class);
				for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
					if (courseAndReg.getCourse() == course) {
						// Enable or disable the courseAdminButton (and userAdminButton) depending on whether or not
						// user is an instructor.
						boolean isInstructor = courseAndReg.getCourseRegistration().getRegistrationType() == CourseRegistrationType.INSTRUCTOR;
						manageExercisesButton.setEnabled(isInstructor);
						manageUsersButton.setEnabled(isInstructor);
						GWT.log((isInstructor ? "enable" : "disable") + " courseAdminButton");
					}
				}
			}
		}

		protected void handleManageExercisesButtonPress() {
			if (getSession().get(CourseSelection.class) != null) {
				getSession().get(PageStack.class).push(PageId.PROBLEM_ADMIN);
			}
		}

		protected void handleManageUsersButtonPress() {
			if (getSession().get(CourseSelection.class) != null) {
				getSession().get(PageStack.class).push(PageId.USER_ADMIN);
			}
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
