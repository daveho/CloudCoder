// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.client.page.CloudCoderPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Utility methods for working with the {@link Session} object.
 * 
 * @author David Hovemeyer
 */
public class SessionUtil {
	/**
	 * Retrieve list of {@link ProblemAndSubmissionReceipt}s for given {@link Course}.
	 * This method retrieves problems in all {@link Module}s.
	 * 
	 * @param page     the {@link CloudCoderPage} which initiated the loading of problems
	 * @param course   the {@link Course}
	 * @param session  the {@link Session}
	 */
	public static void loadProblemAndSubmissionReceiptsInCourse(final CloudCoderPage page, final Course course, final Session session) {
		loadProblemAndSubmissionReceiptsInCourse(page, new CourseSelection(course, null), session);
	}

	/**
	 * Retrieve list of {@link ProblemAndSubmissionReceipt}s for given {@link CourseSelection}.
	 * 
	 * @param page             the {@link CloudCoderPage} which initiated the loading of problems
	 * @param courseSelection  the {@link CourseSelection}
	 * @param session          the {@link Session}
	 */
	public static void loadProblemAndSubmissionReceiptsInCourse(final CloudCoderPage page, final CourseSelection courseSelection, final Session session) {
		Course course = courseSelection.getCourse();
		Module module = courseSelection.getModule();
		GWT.log("RPC to load problems and submission receipts for course " + course.getNameAndTitle());
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, session.get(User.class), module, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
            @Override
            public void onFailure(Throwable caught) {
            	if (caught instanceof CloudCoderAuthenticationException) {
            		// See if we can log back in
            		page.recoverFromServerSessionTimeout(new Runnable() {
            			public void run() {
            				// Try again!
            				loadProblemAndSubmissionReceiptsInCourse(page, courseSelection, session);
            			}
            		});
            	} else {
	                GWT.log("Error loading problems", caught);
	                session.add(StatusMessage.error("Error loading problems: " + caught.getMessage()));
            	}
            }

            @Override
            public void onSuccess(ProblemAndSubmissionReceipt[] result) {
            	GWT.log(result.length + " ProblemAndSubmissionReceipts loaded successfully, adding to client-side session...");
                session.add(result);
            }
        });
	}

	/**
	 * Load a complete {@link ProblemAndTestCaseList} for given {@link Problem}.
	 * An RPC call is made to fetch the {@link TestCase}s for the problem,
	 * and the result is delivered asynchronously to a callback.
	 *
	 * @param page       the {@link CloudCoderPage} (needed if a session timeout occurs)
	 * @param problem    the problem
	 * @param onSuccess  the callback to receive the full {@link ProblemAndTestCaseList}
	 * @param onFailure  callback invoked if the {@link ProblemAndTestCaseList} can't be loaded
	 */
	public static void loadProblemAndTestCaseList(
			final CloudCoderPage page,
			final Problem problem,
			final ICallback<ProblemAndTestCaseList> onSuccess,
			final ICallback<Pair<String, Throwable>> onFailure) {
		RPC.getCoursesAndProblemsService.getTestCasesForProblem(problem.getProblemId(), new AsyncCallback<TestCase[]>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable() {
						public void run() {
							// Try again!
							loadProblemAndTestCaseList(page, problem, onSuccess, onFailure);
						}
					});
				} else {
					//page.getSession().add(StatusMessage.error("Could not load test cases for problem: " + caught.getMessage()));
					onFailure.call(new Pair<String, Throwable>("Could not load test cases for problem", caught));
				}
			}

			@Override
			public void onSuccess(TestCase[] result) {
				// Success!
				ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
				problemAndTestCaseList.setProblem(problem);
				problemAndTestCaseList.setTestCaseList(result);
				onSuccess.call(problemAndTestCaseList);
			}
		});
	}

	/**
	 * Load the list of CourseAndCourseRegistrations for the logged-in user.
	 * 
	 * @param page       the {@link CloudCoderPage}
	 * @param onSuccess  success callback
	 * @param onFailure  failure callback
	 */
	public static void loadCourseAndCourseRegistrationList(
			final CloudCoderPage page,
			final ICallback<CourseAndCourseRegistration[]> onSuccess,
			final ICallback<Pair<String, Throwable>> onFailure) {
		RPC.getCoursesAndProblemsService.getCourseAndCourseRegistrations(new AsyncCallback<CourseAndCourseRegistration[]>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable(){
						@Override
						public void run() {
							loadCourseAndCourseRegistrationList(page, onSuccess, onFailure);
						}
					});
				} else {
					onFailure.call(new Pair<String, Throwable>("Error loading courses and course registrations", caught));
				}
			}
			
			@Override
			public void onSuccess(CourseAndCourseRegistration[] result) {
				onSuccess.call(result);
			}
		});
	}

	/**
	 * Load list of {@link User}s in course indicated by a {@link CourseSelection}.
	 * 
	 * @param page            the {@link CloudCoderPage}
	 * @param courseSelection the {@link CourseSelection}
	 * @param onSuccess       success callback
	 * @param onFailure       failure callback
	 */
	public static void loadUsersInCourse(
			final CloudCoderPage page,
			final CourseSelection courseSelection,
			final ICallback<User[]> onSuccess,
			final ICallback<Pair<String, Throwable>> onFailure) {
		RPC.usersService.getUsers(courseSelection.getCourse().getId(), 0, new AsyncCallback<User[]>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable() {
						@Override
						public void run() {
							loadUsersInCourse(page, courseSelection, onSuccess, onFailure);
						}
					});
				} else {
					onFailure.call(new Pair<String, Throwable>("Error loading users in course", caught));
				}
			}
			
			@Override
			public void onSuccess(User[] result) {
				onSuccess.call(result);
			}
		});
	}
	
	public static void getCourseAndCourseRegistrationsRPC(
			final CloudCoderPage page,
			final Session session) {
		RPC.getCoursesAndProblemsService.getCourseAndCourseRegistrations(new AsyncCallback<CourseAndCourseRegistration[]>() {
			@Override
			public void onSuccess(CourseAndCourseRegistration[] result) {
				GWT.log(result.length + " course(s) loaded");
				page.addSessionObject(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable() {
						@Override
						public void run() {
							// Try again!
							getCourseAndCourseRegistrationsRPC(page, session);
						}
					});
				} else {
					GWT.log("Error loading courses", caught);
					session.add(StatusMessage.error("Error loading courses", caught));
				}
			}
		});
	}

	/**
	 * Update (edit) user information, handling a session timeout
	 * if one has occurred.
	 * 
	 * @param page     the {@link CloudCoderPage}
	 * @param user     the updated {@link User}
	 * @param session  the {@link Session}
	 */
	public static void editUser(final CloudCoderPage page, final User user, final Session session, final Runnable onSuccess) {
		RPC.usersService.editUser(
				user,
				new AsyncCallback<Boolean>() { 
					@Override
					public void onSuccess(Boolean result) {
						session.add(StatusMessage.goodNews("Successfully updated user " + user.getUsername()));
						onSuccess.run();
					}
		
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							page.recoverFromServerSessionTimeout(new Runnable() {
								@Override
								public void run() {
									editUser(page, user, session, onSuccess);
								}
							});
						} else {
							GWT.log("Failed to edit user");
							session.add(StatusMessage.error("Error updating user " + user.getUsername(), caught));
						}
					}
				});
	}
}
