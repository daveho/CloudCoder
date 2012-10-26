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

import javax.servlet.http.HttpServletRequest;

/**
 * Object representing the course, problem, and/or user involved in
 * a request to the {@link Problems} servlet.
 * 
 * @author David Hovemeyer
 */
public class ProblemURLInfo {
	private final int courseId;
	private final int problemId;
	private final int userId;
	
	/**
	 * Constructor.
	 * 
	 * @param courseId   the course id
	 * @param problemId  the problem id
	 * @param userId     the user id (student)
	 */
	public ProblemURLInfo(int courseId, int problemId, int userId) {
		this.courseId = courseId;
		this.problemId = problemId;
		this.userId = userId;
	}
	
	/**
	 * @return the course id
	 */
	public int getCourseId() {
		return courseId;
	}
	
	/**
	 * @return the problem id
	 */
	public int getProblemId() {
		return problemId;
	}
	
	/**
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Parse the path info of a request to the {@link Problems} servlet.
	 * 
	 * @param req a request to the Problems servlet
	 * @return the ProblemURLInfo encoded in the request's path info,
	 *         or null if the path info does not encode a valid request
	 */
	public static ProblemURLInfo fromRequest(HttpServletRequest req) {
		// Problem URLs are of the form:
		//
		// - /cloudcoder/admin/problems/<course id> for all problems in course
		// - /cloudcoder/admin/problems/<course id>/<problem id> for specific problem in course
		// - /cloudcoder/admin/problems/<course id>/<problem id>/<user id> for
		//   specific student's work on specific problem in course
		
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			return null;
		}
		//System.out.println("Problem pathInfo is " + pathInfo);
		
		if (pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		
		String[] parts = pathInfo.split("/");
		if (parts.length < 1 || parts.length > 3) {
			// invalid
			return null;
		}
		
		int courseId;
		int problemId = -1;
		int userId = -1;
		
		try {
			courseId = Integer.parseInt(parts[0]);
			if (parts.length > 1) {
				problemId = Integer.parseInt(parts[1]);
				if (parts.length > 2) {
					userId = Integer.parseInt(parts[2]);
				}
			}
		} catch (NumberFormatException e) {
			// invalid
			return null;
		}
		
		//System.out.println("Parsed: " + courseId + "/" + problemId + "/" + userId);
		
		return new ProblemURLInfo(courseId, problemId, userId);
	}
}
