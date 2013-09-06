// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.client.model.PageParams;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Load required page objects.
 * This automates the process of loading the objects
 * mentioned in the URL fragment parameters when there
 * is a direct link to a page.
 * 
 * @author David Hovemeyer
 */
public class LoadPageObjects {
	/**
	 * Load a single page object based on the {@link PageParams}.
	 */
	private interface Loader {
		/**
		 * Load the page object.
		 * 
		 * @param onSuccess callback if the page object is loaded successfully
		 * @param onFailure callback if the page object can't be loaded
		 */
		public void load(Runnable onSuccess, ICallback<Pair<String, Throwable>> onFailure);
	}
	
	private class CourseSelectionLoader implements Loader {
		@Override
		public void load(final Runnable onSuccess, final ICallback<Pair<String, Throwable>> onFailure) {
			final Integer courseId = pageParams.getInt("c");
			if (courseId == null) {
				onFailure.call(new Pair<String, Throwable>("No course id specified", null));
			} else {
				RPC.getCoursesAndProblemsService.getCourses(new AsyncCallback<Course[]>() {
					@Override
					public void onFailure(Throwable caught) {
						onFailure.call(new Pair<String, Throwable>("Could not load course", caught));
					}
					
					@Override
					public void onSuccess(Course[] result) {
						Course course = null;
						for (Course c : result) {
							if (c.getId() == courseId.intValue()) {
								course = c;
								break;
							}
						}
						if (course == null) {
							onFailure.call(new Pair<String, Throwable>("User is not registered for course", null));
						} else {
							// Add CourseSelection to session, and continue
							session.add(new CourseSelection(course, null));
							onSuccess.run();
						}
					}
				});
			}
		}
	}
	
	// Note: the Problem should always be loaded after the CourseSelection.
	private class ProblemLoader implements Loader {
		@Override
		public void load(final Runnable onSuccess, final ICallback<Pair<String, Throwable>> onFailure) {
			final Integer problemId = pageParams.getInt("p");
			if (problemId == null) {
				onFailure.call(new Pair<String, Throwable>("No problem id specified", null));
			} else {
				CourseSelection courseSelection = session.get(CourseSelection.class);
				RPC.getCoursesAndProblemsService.getProblems(courseSelection.getCourse(), new AsyncCallback<Problem[]>() {
					@Override
					public void onFailure(Throwable caught) {
						onFailure.call(new Pair<String, Throwable>("Could not get problem", caught));
					}
					
					public void onSuccess(Problem[] result) {
						Problem problem = null;
						for (Problem p : result) {
							if (p.getProblemId().equals(problemId)) {
								problem = p;
								break;
							}
						}
						if (problem == null) {
							onFailure.call(new Pair<String, Throwable>("No such problem", null));
						} else {
							// Add the Problem to the session and continue
							session.add(problem);
							onSuccess.run();
						}
					}
				});
			}
		}
	}
	
	private Class<?>[] pageObjects;
	private Session session;
	private PageParams pageParams;

	private Map<Class<?>, Loader> loaderMap;
	
	/**
	 * Constructor.
	 * 
	 * @param pageObjects the page object classes
	 * @param session     the {@link Session}
	 * @param pageParams  the page parameters from the URL fragment
	 */
	public LoadPageObjects(Class<?>[] pageObjects, Session session, String pageParams) {
		this.pageObjects = pageObjects;
		this.session = session;
		this.pageParams = new PageParams(pageParams);
		
		this.loaderMap = new HashMap<Class<?>, Loader>();
		loaderMap.put(CourseSelection.class, new CourseSelectionLoader());
		loaderMap.put(Problem.class, new ProblemLoader());
	}
	
	/**
	 * Attempt to load page objects.
	 * Because most/all of the work is asynchronous, the eventual success or failure
	 * will be reported via the onSuccess and onFailure callbacks.
	 * 
	 * @param onSuccess   callback if all page objects are loaded successfully
	 * @param onFailure   callback if any of the page objects can't be loaded
	 */
	public void execute(Runnable onSuccess, ICallback<Pair<String, Throwable>> onFailure) {
		doLoad(0, onSuccess, onFailure);
	}

	private void doLoad(final int index, final Runnable onSuccess, final ICallback<Pair<String, Throwable>> onFailure) {
		// Base case: if there are no more page objects to load, we're done
		if (index >= pageObjects.length) {
			onSuccess.run();
			return;
		}
		
		Class<?> pageObjectCls = pageObjects[index];
		
		// If the session already contains the needed object, then there is
		// nothing to do for this object, and we should continue on the
		// next object (if any).
		if (session.get(pageObjectCls) != null) {
			doLoad(index+1, onSuccess, onFailure);
			return;
		}
		
		// Get a Loader for the object
		Loader loader = loaderMap.get(pageObjectCls);
		if (loader == null) {
			onFailure.call(new Pair<String, Throwable>("No Loader for " + pageObjectCls.getName(), null));
			return;
		}
		
		// Continuation if the current page object is loaded successfully:
		// attempts to load the next page object (if any)
		Runnable successContinuation = new Runnable() {
			@Override
			public void run() {
				doLoad(index + 1, onSuccess, onFailure);
			}
		};
		
		// Use Loader to load current page object and, if successful, continue recursively
		loader.load(successContinuation, onFailure);
	}
}
