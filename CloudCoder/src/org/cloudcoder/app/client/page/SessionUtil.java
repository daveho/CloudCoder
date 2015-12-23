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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.ModelObjectUtil;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Utility methods for working with the {@link Session} object.
 * There is some sort of crazy code here to
 * <ul>
 * <li>Allow only one RPC call with a particular set of parameter
 *   values to be in-progress at one time</li>
 * <li>Recover from session timeouts (by popping up a password dialog)</li>
 * </ul>
 * 
 * @author David Hovemeyer
 */
public class SessionUtil {
	/**
	 *  Keeps track of a pending RPC call (and its parameters).
	 */
	private static class RunOnce {
		int pending;
		Object[] pendingParams;
	}
	
	/**
	 * Execute an RPC call with specified parameters, but only if
	 * an identical call is not already running.
	 */
	private static abstract class OneTimeRunnable {
		private RunOnce runner;
		private Object[] params;

		/**
		 * Constructor.
		 * 
		 * @param runner the {@link RunOnce} keeping track of pending executions
		 * @param params the parameters of this (attempted) RPC call
		 */
		public OneTimeRunnable(RunOnce runner, Object... params) {
			this.runner = runner;
			this.params = params;
		}
		
		/**
		 * Execute the RPC call, unless the currently-pending RPC call
		 * used identical parameters.
		 */
		public void execute() {
			if (runner.pending == 0 || !sameParams(this.params, runner.pendingParams)) {
				runner.pending++;
				runner.pendingParams = params;
				run();
			}
		}

		/**
		 * The actual execution of the RPC call.
		 */
		public abstract void run();
		
		/**
		 * Called when the actual RPC call finishes or reports an error.
		 */
		protected void onDone() {
			runner.pending--;
		}
		
		private static boolean sameParams(Object[] params, Object[] pendingParams) {
			return ModelObjectUtil.arrayEquals(params, pendingParams);
		}
	}
	
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
	
	private static final RunOnce loadProblemsAndSubmissionReceiptsRunner = new RunOnce();

	/**
	 * Retrieve list of {@link ProblemAndSubmissionReceipt}s for given {@link CourseSelection}.
	 * 
	 * @param page             the {@link CloudCoderPage} which initiated the loading of problems
	 * @param courseSelection  the {@link CourseSelection}
	 * @param session          the {@link Session}
	 */
	public static void loadProblemAndSubmissionReceiptsInCourse(final CloudCoderPage page, final CourseSelection courseSelection, final Session session) {
		new OneTimeRunnable(loadProblemsAndSubmissionReceiptsRunner, courseSelection) {
			public void run() {
				doLoadProblemAndSubmissionReceiptsInCourse(page, courseSelection, session);
			}

			private void doLoadProblemAndSubmissionReceiptsInCourse(final CloudCoderPage page,
					final CourseSelection courseSelection, final Session session) {
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
		            				doLoadProblemAndSubmissionReceiptsInCourse(page, courseSelection, session);
		            			}
		            		});
		            	} else {
			                GWT.log("Error loading problems", caught);
			                session.add(StatusMessage.error("Error loading problems: " + caught.getMessage()));
			                onDone();
		            	}
		            }
		
		            @Override
		            public void onSuccess(ProblemAndSubmissionReceipt[] result) {
		            	GWT.log(result.length + " ProblemAndSubmissionReceipts loaded successfully, adding to client-side session...");
		                session.add(result);
		                onDone();
		            }
		        });
			}
		}.execute();
	}
	
	private static final RunOnce loadProblemAndTestCaseListRunner = new RunOnce();

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
		new OneTimeRunnable(loadProblemAndTestCaseListRunner, problem) {
			public void run() {
				doLoadProblemAndTestCaseList(page, problem, onSuccess, onFailure);
			}

			private void doLoadProblemAndTestCaseList(final CloudCoderPage page, final Problem problem,
					final ICallback<ProblemAndTestCaseList> onSuccess,
					final ICallback<Pair<String, Throwable>> onFailure) {
				RPC.getCoursesAndProblemsService.getTestCasesForProblem(problem.getProblemId(), new AsyncCallback<TestCase[]>() {
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							page.recoverFromServerSessionTimeout(new Runnable() {
								public void run() {
									// Try again!
									doLoadProblemAndTestCaseList(page, problem, onSuccess, onFailure);
								}
							});
						} else {
							onFailure.call(new Pair<String, Throwable>("Could not load test cases for problem", caught));
							onDone();
						}
					}
		
					@Override
					public void onSuccess(TestCase[] result) {
						// Success!
						ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
						problemAndTestCaseList.setProblem(problem);
						problemAndTestCaseList.setTestCaseList(result);
						onSuccess.call(problemAndTestCaseList);
						onDone();
					}
				});
			}
		}.execute();
	}
	
	private static final RunOnce loadCourseAndCourseRegistrationListRunner = new RunOnce();

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
		new OneTimeRunnable(loadCourseAndCourseRegistrationListRunner) {
			public void run() {
				doLoadCourseAndCourseRegistrationList(page, onSuccess, onFailure);
			}

			private void doLoadCourseAndCourseRegistrationList(final CloudCoderPage page,
					final ICallback<CourseAndCourseRegistration[]> onSuccess,
					final ICallback<Pair<String, Throwable>> onFailure) {
				GWT.log("Requesting courses and course registrations...");
				RPC.getCoursesAndProblemsService.getCourseAndCourseRegistrations(new AsyncCallback<CourseAndCourseRegistration[]>() {
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							page.recoverFromServerSessionTimeout(new Runnable(){
								@Override
								public void run() {
									doLoadCourseAndCourseRegistrationList(page, onSuccess, onFailure);
								}
							});
						} else {
							onFailure.call(new Pair<String, Throwable>("Error loading courses and course registrations", caught));
							onDone();
						}
					}
					
					@Override
					public void onSuccess(CourseAndCourseRegistration[] result) {
						onSuccess.call(result);
						onDone();
					}
				});
			}
		}.execute();
	}
	
	private static final RunOnce loadUsersInCourseRunner = new RunOnce();

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
		new OneTimeRunnable(loadUsersInCourseRunner, courseSelection) {
			public void run() {
				doLoadUsersInCourse(page, courseSelection, onSuccess, onFailure);
			}

			private void doLoadUsersInCourse(final CloudCoderPage page, final CourseSelection courseSelection,
					final ICallback<User[]> onSuccess, final ICallback<Pair<String, Throwable>> onFailure) {
				RPC.usersService.getUsers(courseSelection.getCourse().getId(), 0, new AsyncCallback<User[]>() {
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							page.recoverFromServerSessionTimeout(new Runnable() {
								@Override
								public void run() {
									doLoadUsersInCourse(page, courseSelection, onSuccess, onFailure);
								}
							});
						} else {
							onFailure.call(new Pair<String, Throwable>("Error loading users in course", caught));
							onDone();
						}
					}
					
					@Override
					public void onSuccess(User[] result) {
						onSuccess.call(result);
						onDone();
					}
				});
			}
		}.execute();
	}

	/**
	 * Get the list of {@link CourseAndCourseRegistration}s for the logged-in
	 * user, and add them to the {@link Session}.
	 * 
	 * @param page     the {@link CloudCoderPage}
	 * @param session  the {@link Session}
	 */
	public static void getCourseAndCourseRegistrationsRPC(
			final CloudCoderPage page,
			final Session session) {
		loadCourseAndCourseRegistrationList(
				page,
				new ICallback<CourseAndCourseRegistration[]>() {
					@Override
					public void call(CourseAndCourseRegistration[] value) {
						session.add(value);
					}
				},
				new ICallback<Pair<String,Throwable>>() {
					@Override
					public void call(Pair<String, Throwable> value) {
						session.add(StatusMessage.error(value.getLeft(), value.getRight()));
					}
				}
		);
	}
	
	private static final RunOnce editUserRunner = new RunOnce();

	/**
	 * Update (edit) user information, handling a session timeout
	 * if one has occurred.
	 * 
	 * @param page     the {@link CloudCoderPage}
	 * @param user     the updated {@link User}
	 * @param session  the {@link Session}
	 */
	public static void editUser(final CloudCoderPage page, final User user, final Runnable onSuccess) {
		new OneTimeRunnable(editUserRunner, user) {
			@Override
			public void run() {
				doEditUser(page, user, onSuccess);
			}

			private void doEditUser(final CloudCoderPage page, final User user, final Runnable onSuccess) {
				RPC.usersService.editUser(
						user,
						new AsyncCallback<Boolean>() { 
							@Override
							public void onSuccess(Boolean result) {
								page.getSession().add(StatusMessage.goodNews("Successfully updated user " + user.getUsername()));
								onSuccess.run();
								onDone();
							}
				
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof CloudCoderAuthenticationException) {
									page.recoverFromServerSessionTimeout(new Runnable() {
										@Override
										public void run() {
											doEditUser(page, user, onSuccess);
										}
									});
								} else {
									GWT.log("Failed to edit user");
									page.getSession().add(StatusMessage.error("Error updating user " + user.getUsername(), caught));
									onDone();
								}
							}
						});
			}
		}.execute();
	}
	
	private static final RunOnce loadModulesForCourseRunner = new RunOnce();

	/**
	 * Load {@link Module}s in specified {@link Course}.
	 * 
	 * @param page the current {@link CloudCoderPage}
	 * @param course  the {@link Course}
	 * @param onSuccess callback if modules are loaded successfully
	 */
	public static void loadModulesForCourse(final CloudCoderPage page, final Course course, final ICallback<Module[]> onSuccess) {
		new OneTimeRunnable(loadModulesForCourseRunner, course) {
			@Override
			public void run() {
				doLoadModulesForCourse(page, course, onSuccess);
			}

			private void doLoadModulesForCourse(final CloudCoderPage page, final Course course, final ICallback<Module[]> onSuccess) {
				RPC.getCoursesAndProblemsService.getModulesForCourse(course, new AsyncCallback<Module[]>() {
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							// attempt to recover from session timeout
							page.recoverFromServerSessionTimeout(new Runnable() {
								@Override
								public void run() {
									// Try again
									doLoadModulesForCourse(page, course, onSuccess);
								}
							});
						} else {
							// Some unrecoverable error occurred
							page.getSession().add(StatusMessage.error("Could not load modules for course", caught));
							onDone();
						}
					}

					@Override
					public void onSuccess(Module[] result) {
						// Great success!
						onSuccess.call(result);
						onDone();
					}
				});
			}
		}.execute();
	}
}
