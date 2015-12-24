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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Panel;

/**
 * Superclass for form UIs that should only be enabled
 * if the user is an instructor in the currently-selected
 * course.
 * 
 * @author David Hovemeyer
 */
public abstract class CourseInstructorFormUI
		extends ValidatedFormUI
		implements SessionObserver, Subscriber {

	private CloudCoderPage page;

	/**
	 * Constructor.
	 * 
	 * @param page the {@link CloudCoderPage}
	 */
	public CourseInstructorFormUI(CloudCoderPage page) {
		this.page = page;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param page  the {@link CloudCoderPage}
	 * @param panel the {@link Panel} to be used as the top-level widget
	 */
	public CourseInstructorFormUI(CloudCoderPage page, Panel panel) {
		super(panel);
		this.page = page;
	}
	
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		setEnabled(false);
		onCourseOrCourseRegistrationsUpdate();
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT &&
				(hint instanceof CourseSelection || hint instanceof CourseAndCourseRegistration[])) {
			onCourseOrCourseRegistrationsUpdate();
		}
	}

	private void onCourseOrCourseRegistrationsUpdate() {
		// See what course is selected, and what the user's list
		// of courses/course registrations contains
		CourseSelection sel = page.getSession().get(CourseSelection.class);
		CourseAndCourseRegistration[] regList = page.getSession().get(CourseAndCourseRegistration[].class);

		// Allow subclasses to keep track of course selection changes
		if (sel != null) {
			onCourseChange(sel.getCourse());
		}
		
		// Enable or disable this UI depending on whether the
		// user is an instructor in the selected course
		if (sel != null && regList != null) {
			boolean isInstructor = CourseAndCourseRegistration.isInstructor(regList, sel.getCourse());
			GWT.log("CourseInstructorUI: isInstructor=" + isInstructor);
			setEnabled(isInstructor);
		}
	}
	
	/**
	 * Downcall method for enabling/disabling the form's UI widgets.
	 * 
	 * @param b true if the widgets should be enabled, false if
	 *          they should be disabled
	 */
	protected abstract void setEnabled(boolean b);
	
	/**
	 * Downcall method called when course changes.
	 * 
	 * @param course the newly-selected {@link Course}
	 */
	protected abstract void onCourseChange(Course course); 
}
