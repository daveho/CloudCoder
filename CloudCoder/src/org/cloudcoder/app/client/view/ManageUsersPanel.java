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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Composite;


/**
 * UI for managing users in a course, for the "Manage course" tab.
 * Currently, there are just two things you can do here:
 * edit a user and view progress for a user.
 * This implements the remaining functionality in the
 * UserAdminPage that isn't addressed by the other
 * UIs in the "Manage course" tab.
 * 
 * @author David Hovemeyer
 */
public class ManageUsersPanel extends Composite implements CourseInstructorUI, SessionObserver {
	public ManageUsersPanel() {
		
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		// Keep track of changes to instructor status
		new CourseInstructorStatusMonitor(this).activate(session, subscriptionRegistrar);
	}

	@Override
	public void setEnabled(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCourseChange(Course course) {
		// TODO Auto-generated method stub
		
	}
}
