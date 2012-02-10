// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for admin authorization filters which ensure that only course instructors
 * are permitted access.  Subclasses must override the getCourseId() method.
 * 
 * @author David Hovemeyer
 */
public abstract class CourseAdminAuthorizationFilter extends AdminAuthorizationFilter {
	private static final Logger logger = LoggerFactory.getLogger(CourseAdminAuthorizationFilter.class);

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.admin.AdminAuthorizationFilter#checkAuthorization(org.cloudcoder.app.shared.model.User, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void checkAuthorization(User user, HttpServletRequest req,
			HttpServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		int courseId = getCourseId(req);
		if (courseId < 1) {
			badRequest(resp);
			return;
		}
		
		List<? extends Object[]> triples = Database.getInstance().getCoursesForUser(user);

		boolean isInstructorInCourse = false;
		for (Object[] triple : triples) {
			//System.out.println("Check course=" + ((Course)triple[0]).getId());
			// The third element of each triple returned by getCoursesForUser
			// is the CourseRegistration, which will indicate the registration type.
			CourseRegistration reg = (CourseRegistration) triple[2];
			//System.out.println("  reg courseid=" + reg.getCourseId() + ", regtype=" + reg.getRegistrationType());
			if (reg.getCourseId() == courseId && reg.getRegistrationType() == CourseRegistrationType.INSTRUCTOR) {
				isInstructorInCourse = true;
				break;
			}
		}
		
		if (!isInstructorInCourse) {
			// User is not an instructor in this course
			logger.info("Admin auth: user " + user.getUserName() + " not an instructor in course " + courseId);
			unauthorized(resp);
			return;
		}
		
		// Successfully authorized!  Proceed with servlet.
		chain.doFilter(req, resp);
	}
	
	/**
	 * Extract the course id from the HttpServletRequest.
	 * 
	 * @param req the HttpServletRequest
	 * @return the course id, or -1 if the request does not encode a course id
	 */
	protected abstract int getCourseId(HttpServletRequest req);

}
