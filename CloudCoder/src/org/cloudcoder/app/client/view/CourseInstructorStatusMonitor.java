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
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;

/**
 * Object that monitors the session to keep track of whether
 * or not the user is an instructor in the currently-selected
 * course, and updates a {@link CourseInstructorUI}
 * object in response.
 * 
 * @author David Hovemeyer
 */
public class CourseInstructorStatusMonitor implements SessionObserver, Subscriber {
	private CourseInstructorUI ui;
	private Session session;
	
	/**
	 * Constructor.
	 */
	public CourseInstructorStatusMonitor(CourseInstructorUI ui) {
		this.ui = ui;
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		ui.setEnabled(false);
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
		CourseSelection sel = session.get(CourseSelection.class);
		CourseAndCourseRegistration[] regList = session.get(CourseAndCourseRegistration[].class);

		// Allow subclasses to keep track of course selection changes
		if (sel != null) {
			ui.onCourseChange(sel.getCourse());
		}
		
		// Enable or disable this UI depending on whether the
		// user is an instructor in the selected course
		if (sel != null && regList != null) {
			boolean isInstructor = CourseAndCourseRegistration.isInstructor(regList, sel.getCourse());
			GWT.log("CourseInstructorUI: isInstructor=" + isInstructor);
			ui.setEnabled(isInstructor);
		}
	}
}
