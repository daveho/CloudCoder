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

import org.cloudcoder.app.shared.model.Course;

/**
 * Interface to be implemented by UIs that should be selectively
 * enabled and disabled depending on whether or not the
 * user is an instructor in the currently-selected course.
 * 
 * @author David Hovemeyer
 * @see {@link CourseInstructorStatusMonitor}
 */
public interface CourseInstructorUI {
	/**
	 * Downcall method for enabling/disabling the form's UI widgets.
	 * 
	 * @param b true if the widgets should be enabled, false if
	 *          they should be disabled
	 */
	void setEnabled(boolean b);

	/**
	 * Downcall method called when course changes.
	 * 
	 * @param course the newly-selected {@link Course}
	 */
	void onCourseChange(Course course);

}
