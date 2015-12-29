// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.AccordionPanel;
import org.cloudcoder.app.client.view.BulkRegistrationPanel;
import org.cloudcoder.app.client.view.CourseSelectionListBox;
import org.cloudcoder.app.client.view.CreateCoursePanel;
import org.cloudcoder.app.client.view.DebugPopupPanel;
import org.cloudcoder.app.client.view.ExerciseAdminPanel;
import org.cloudcoder.app.client.view.ExerciseSummaryView;
import org.cloudcoder.app.client.view.ISelectableComposite;
import org.cloudcoder.app.client.view.LabeledCourseSelectionListBox;
import org.cloudcoder.app.client.view.ManageUsersPanel;
import org.cloudcoder.app.client.view.ModuleListBox;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.ProblemListView3;
import org.cloudcoder.app.client.view.RegisterExistingUserPanel;
import org.cloudcoder.app.client.view.RegisterSingleUserPanel;
import org.cloudcoder.app.client.view.SectionLabel;
import org.cloudcoder.app.client.view.SectionSelectionView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.UserAccountView2;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseCreationSpec;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Home page providing access to exercises, account information,
 * and the playground.  This is the third iteration of the homepage.
 * 
 * @author David Hovemeyer
 */
public class CoursesAndProblemsPage3 extends CloudCoderPage {
	private enum TabId {
		EXERCISES,
		ACCOUNT,
		PLAYGROUND,
		ADMIN,
		MANAGE_USERS,
		MANAGE_EXERCISES,
	}
	
	private static class NavigationMemento {
		TabId tabId;
		int widgetIndex;
		
		public NavigationMemento(TabId tabId, int widgetIndex) {
			this.tabId = tabId;
			this.widgetIndex = widgetIndex;
		}
	}
	
	private class UI extends Composite implements SessionObserver, Subscriber {
		private static final double WEST_PANEL_WIDTH_PX = 240.0;
		private static final double SEP_PX = 10.0; // sep between west and center panel
		private static final double REFRESH_BUTTON_WIDTH_PX = 60.0;
		private static final double LOAD_EXERCISE_BUTTON_WIDTH_PX = 120.0;
		private static final double PROGRESS_SUMMARY_HEIGHT_PX = 240.0;
		private static final double ADMIN_BUTTON_HEIGHT_PX = 32.0;
		private static final double MODULE_LISTBOX_HEIGHT_PX = 24.0;
		private static final double EXERCISES_LABEL_WIDTH_PX = 100.0;
		
		private PageNavPanel pageNavPanel;
		private StatusMessageView statusMessageView;
		private FlowPanel courseSelectionPanel;
		private LabeledCourseSelectionListBox courseListBox;
		private LayoutPanel west;
		private ProblemDescriptionView problemDescriptionView;
		private ExerciseSummaryView progressSummaryView;
		private ProblemListView3 exerciseList;
		private UserAccountView2 userAccountView;
		private Button manageExercisesButton;
		private Button manageUsersButton;
		private TabLayoutPanel tabLayoutPanel;
		private List<TabId> tabIdList;
		private CreateCoursePanel createCoursePanel;
		private boolean manageUsersTabCreated;
		private boolean manageExercisesTabCreated;
		private ModuleListBox moduleListBox;
		private BulkRegistrationPanel bulkRegistrationPanel;
		private ManageUsersPanel manageUsersPanel;
		private ExerciseAdminPanel exerciseAdminPanel;
		private SectionSelectionView sectionSelectionView;
		private boolean navigationMementoChecked;
		
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
			
			this.courseSelectionPanel = new FlowPanel();
			full.add(courseSelectionPanel);
			full.setWidgetLeftRight(courseSelectionPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			full.setWidgetTopHeight(courseSelectionPanel, PageNavPanel.HEIGHT_PX - 8.0, Unit.PX, LabeledCourseSelectionListBox.HEIGHT_PX, Unit.PX);
			this.courseListBox = new LabeledCourseSelectionListBox(CoursesAndProblemsPage3.this, "Course:");
			this.courseListBox.setDisplayMode(CourseSelectionListBox.DisplayMode.FANCY);
			courseSelectionPanel.add(courseListBox);
			
			this.tabLayoutPanel = new TabLayoutPanel(32.0, Unit.PX);
			this.tabIdList = new ArrayList<TabId>();
			
			// Exercises tab
			IsWidget exercises = createExercisesTab();
			addTab(exercises, "Exercises", TabId.EXERCISES);
			tabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {
				@Override
				public void onSelection(SelectionEvent<Integer> event) {
					// Work around bugs where DataGrid-based widgets don't display
					// their contents properly if updated while the tab in which
					// they are located is not selected.
					TabId tabId = tabIdList.get(event.getSelectedItem());
					switch (tabId) {
					case EXERCISES:
						exerciseList.redisplay();
						break;
					case MANAGE_USERS:
						manageUsersPanel.redisplay();
						break;
					case MANAGE_EXERCISES:
					    GWT.log("selection even triggering a redisplay for exercise admin");
					    exerciseAdminPanel.redisplay();
					    break;
					default:
					}
				}
			});
			
			// Account tab
			IsWidget account = createAccountTab();
			addTab(account, "Account", TabId.ACCOUNT);
			
			// Playground tab
			IsWidget playground = createPlaygroundTab();
			addTab(playground, "Playground", TabId.PLAYGROUND);
			
			// The admin tab will be added later if the user
			// has superuser privileges
			
			full.add(tabLayoutPanel);
			full.setWidgetLeftRight(tabLayoutPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			full.setWidgetTopBottom(tabLayoutPanel, PageNavPanel.HEIGHT_PX + LabeledCourseSelectionListBox.HEIGHT_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);

			this.statusMessageView = new StatusMessageView();
			full.add(statusMessageView);
			full.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			full.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			
			initWidget(full);
		}
		
		private void addTab(IsWidget widget, String title, TabId tabId) {
			tabLayoutPanel.add(widget, title);
			tabIdList.add(tabId);
		}

		private IsWidget createExercisesTab() {
			LayoutPanel wrap = new LayoutPanel();
			
			DockLayoutPanel exercises = new DockLayoutPanel(Unit.PX);

			// FIXME: this will go away once the "Manage course" tab is completely working
			this.west = new LayoutPanel();
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
			top.setWidgetLeftRight(exercisesLabel, 0.0, Unit.PX, EXERCISES_LABEL_WIDTH_PX, Unit.PX);
			top.setWidgetTopHeight(exercisesLabel, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			
			this.moduleListBox = new ModuleListBox(CoursesAndProblemsPage3.this);
			top.add(moduleListBox);
			top.setWidgetLeftRight(moduleListBox, EXERCISES_LABEL_WIDTH_PX + 80.0, Unit.PX, r+10.0, Unit.PX);
			top.setWidgetTopHeight(moduleListBox, 0.0+6.0, Unit.PX, MODULE_LISTBOX_HEIGHT_PX, Unit.PX);
			
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
		
		private IsWidget createAdminTab() {
			AccordionPanel accordionPanel = new AccordionPanel();

			// Add create course UI widget
			this.createCoursePanel = new CreateCoursePanel();
			createCoursePanel.setOnCreateCourse(new Runnable() {
				@Override
				public void run() {
					if (createCoursePanel.validate()) {
						CourseCreationSpec spec = createCoursePanel.getCourseCreationSpec();
						createCourse(spec);
					}
				}
			});
			accordionPanel.add(createCoursePanel, "Create course");

			// Could put other widgets in the accordion panel here...
			
			return accordionPanel;
		}

		protected void createCourse(CourseCreationSpec spec) {
			RPC.getCoursesAndProblemsService.createCourse(spec, new AsyncCallback<OperationResult>() {
				@Override
				public void onSuccess(OperationResult result) {
					getSession().add(StatusMessage.fromOperationResult(result));
					if (result.isSuccess()) {
						// Success, reload the user's list of courses
						// and course registrations
						GWT.log("Course created successfully, loading courses and course registrations...");
						SessionUtil.getCourseAndCourseRegistrationsRPC(CoursesAndProblemsPage3.this, getSession());
						createCoursePanel.clear();
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
				    getSession().add(StatusMessage.error("Error trying to create course", caught));
				}
			});
		}
		
		private void registerSingleUser(final EditedUser editedUser) {
		    final CourseSelection courseSelection=getSession().get(CourseSelection.class);
		    if (courseSelection==null) {
		        String msg="No course in the session when trying to Register Single User";
                GWT.log(msg);
                DebugPopupPanel p=new DebugPopupPanel(msg);
                p.show();
                return;
		    }
		    final Course course=courseSelection.getCourse();

		    RPC.usersService.addUserToCourse(editedUser, course.getId(), new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    //String msg=String.format("Unable to create user %s in course %s", editedUser.getUser().getUsername(), course.getName());
                    String msg="Unable to create user "+editedUser.getUser().getUsername()+" in course "+course.getName();
                    getSession().add(StatusMessage.error(msg, caught));
                }
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        //String msg=String.format("Successfully added %s to course %s", editedUser.getUser().getUsername(), course.getName());
                        String msg="Successfully added "+editedUser.getUser().getUsername()+" to course "+course.getName();
                        GWT.log(msg);
                        getSession().add(StatusMessage.goodNews(msg));
                    } else {
                        // TODO Is this even possible? What does a false return value mean?
                    }
                }
            });
		}
		
		private IsWidget createManageExercisesPanel() {
		    exerciseAdminPanel=new ExerciseAdminPanel(CoursesAndProblemsPage3.this);
		    exerciseAdminPanel.activate(CoursesAndProblemsPage3.this.getSession(), 
		            CoursesAndProblemsPage3.this.getSubscriptionRegistrar());
		    return exerciseAdminPanel;
		}
		
		private IsWidget createManageUsersTab() {
			AccordionPanel accordionPanel = new AccordionPanel();
			
			// panel to register a single user
			final RegisterSingleUserPanel registerSingleUserPanel=new RegisterSingleUserPanel();
			registerSingleUserPanel.setOnRegisterSingleUser(new Runnable() {
                @Override
                public void run() {
                    if (registerSingleUserPanel.validate()) {
                        EditedUser editedUser=registerSingleUserPanel.getEditedUser();
                        registerSingleUser(editedUser);
                        registerSingleUserPanel.clear();
                    } else {
                        DebugPopupPanel p=new DebugPopupPanel("Unable to validate inputs to create a new user");
                        p.show();
                    }
                }
            });
			accordionPanel.add(registerSingleUserPanel, "Register Single User");
			registerSingleUserPanel.activate(getSession(), getSubscriptionRegistrar());
			
			// panel to register a single existing user
			final RegisterExistingUserPanel registerExistingUserPanel = new RegisterExistingUserPanel();
			registerExistingUserPanel.setOnRegisterUser(new Runnable() {
				@Override
				public void run() {
					if (registerExistingUserPanel.validate()) {
						GWT.log("Existing user data validated!");
						SessionUtil.registerExistingUser(
								CoursesAndProblemsPage3.this,
								registerExistingUserPanel.getCourseRegistrationSpec(),
								new ICallback<OperationResult>() {
									@Override
									public void call(OperationResult value) {
										if (value.isSuccess()) {
											getSession().add(StatusMessage.goodNews(value.getMessage()));
										} else {
											getSession().add(StatusMessage.error(value.getMessage()));
										}
									}
								}
						);
					}
				}
			});
			accordionPanel.add(registerExistingUserPanel, "Register Existing User");
			registerExistingUserPanel.activate(getSession(), getSubscriptionRegistrar());

			// Bulk registration UI
			this.bulkRegistrationPanel = new BulkRegistrationPanel(CoursesAndProblemsPage3.this);
			accordionPanel.add(bulkRegistrationPanel, "Bulk User Registration");
			bulkRegistrationPanel.activate(getSession(), getSubscriptionRegistrar());
			
			// Manage users UI
			this.manageUsersPanel = new ManageUsersPanel(CoursesAndProblemsPage3.this);
			accordionPanel.add(manageUsersPanel, "Manage Users");
			manageUsersPanel.activate(getSession(), getSubscriptionRegistrar());
			
			return accordionPanel;
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
			courseListBox.activate(session, subscriptionRegistrar);
			moduleListBox.activate(session, subscriptionRegistrar);
			
			// Create the Admin tab if the user is a superuser
			User user = session.get(User.class);
			GWT.log("User " + (user.isSuperuser() ? "is" : "is not") + " superuser");
			if (user.isSuperuser()) {
				addTab(createAdminTab(), "Admin", TabId.ADMIN);
				createCoursePanel.activate(session, subscriptionRegistrar);
			}

			// Load courses
			session.remove(CourseAndCourseRegistration[].class); // force a refresh
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

		/**
		 * This method is called when the user's {@link CourseAndCourseRegistration}s
		 * are known.  Additional UI tabs are created if the user is an instructor.
		 * 
		 * @param courseAndRegList the user's {@link CourseAndCourseRegistration}s
		 */
		private void onCourseAndCourseRegistrationsLoaded(
				CourseAndCourseRegistration[] courseAndRegList) {
			
			GWT.log("Courses and course registrations loaded...");
			boolean isInstructor = false;

			// Determine if the user is an instructor for any of the courses
			for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
				if (courseAndReg.getCourseRegistration().getRegistrationType() == CourseRegistrationType.INSTRUCTOR) {
					GWT.log("Instructor for course " +  courseAndReg.getCourse().getName());
					isInstructor = true;
				}
			}
			
			// Create the "Problems" and "User" admin buttons if appropriate.
			if (isInstructor && manageExercisesButton == null) {
				this.manageExercisesButton = new Button("legacy exercises");
				west.add(manageExercisesButton);
				west.setWidgetLeftRight(manageExercisesButton, 10.0, Unit.PX, 10.0, Unit.PX);
				west.setWidgetBottomHeight(manageExercisesButton, ADMIN_BUTTON_HEIGHT_PX+4.0, Unit.PX, ADMIN_BUTTON_HEIGHT_PX, Unit.PX);
				manageExercisesButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						handleManageExercisesButtonPress();
					}
				});
				this.manageUsersButton = new Button("legacy users");
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
			
			// Create "Manage users" tab if appropriate
			if (isInstructor && !this.manageUsersTabCreated) {
				IsWidget manageCoursePanel = createManageUsersTab();
				addTab(manageCoursePanel, "Manage users", TabId.MANAGE_USERS);
				this.manageUsersTabCreated = true;
			}
			
			// Create "Manage exercises" tab if appropriate
			if (isInstructor && !this.manageExercisesTabCreated) {
			    IsWidget manageExercisesPanel = createManageExercisesPanel();
			    addTab(manageExercisesPanel, "Manage exercises", TabId.MANAGE_EXERCISES);
                this.manageExercisesTabCreated = true;
			}
			
			// Create section selection widget if appropriate
			if (isInstructor && this.sectionSelectionView == null) {
				courseSelectionPanel.add(new InlineHTML(" "));
				this.sectionSelectionView = new SectionSelectionView();
				courseSelectionPanel.add(sectionSelectionView);
				sectionSelectionView.activate(getSession(), getSubscriptionRegistrar());
			}
			
			// At this point, all of the tabs that are going to exist should
			// exist, so now is a good time to check the navigation memento.
			if (!navigationMementoChecked) {
				checkNavigationMemento();
				navigationMementoChecked = true;
			}
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

		protected void saveNavigationMemento() {
			int tabIndex = tabLayoutPanel.getSelectedIndex();
			int widgetIndex = -1;
			Widget tabWidget = tabLayoutPanel.getWidget(tabIndex);
			GWT.log("tabWidget is a " + tabWidget.getClass().getSimpleName());
			if (tabWidget instanceof ISelectableComposite) {
				widgetIndex = ((ISelectableComposite)tabWidget).getSelectedIndex();
			}
			TabId tabId = tabIdList.get(tabIndex);
			GWT.log("Saving navigation memento tabId=" + tabId + ", widgetIndex=" + widgetIndex);
			getSession().add(new NavigationMemento(tabId, widgetIndex));
		}
		
		private void checkNavigationMemento() {
			NavigationMemento memento = getSession().get(NavigationMemento.class);
			if (memento != null) {
				for (int i = 0; i < tabIdList.size(); i++) {
					if (memento.tabId == tabIdList.get(i)) {
						// Restore tab
						GWT.log("Restoring tab " + memento.tabId);
						tabLayoutPanel.selectTab(i);
						Widget tabWidget = tabLayoutPanel.getWidget(i);
						GWT.log("tabWidget is a " + tabWidget.getClass().getSimpleName());
						if (tabWidget instanceof ISelectableComposite && memento.widgetIndex >= 0) {
							// Restore widget within tab
							GWT.log("Restoring widget " + memento.widgetIndex);
							((ISelectableComposite)tabWidget).setSelectedIndex(memento.widgetIndex);
						}
						break;
					}
				}
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
		
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}
	
	@Override
	public void deactivate() {
		// Record a memento indicating which tab and widget was active,
		// so that it can be restored if the user navigates back
		UI ui = (UI) getWidget();
		ui.saveNavigationMemento();
		super.deactivate();
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
