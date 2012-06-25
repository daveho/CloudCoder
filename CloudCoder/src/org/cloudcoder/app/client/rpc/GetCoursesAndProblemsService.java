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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionReceipt;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * RPC service for getting {@link Course}s and {@link Problem}s
 * for the currently-authenticated client.
 */
@RemoteServiceRelativePath("getCoursesAndProblems")
public interface GetCoursesAndProblemsService extends RemoteService {
	/**
	 * Get the courses the client user is registered for.
	 * 
	 * @return list of courses the client user is registered for
	 * @throws NetCoderAuthenticationException if the client is not authenticated
	 */
	public Course[] getCourses() throws NetCoderAuthenticationException;
	
	/**
	 * Get {@link CourseAndCourseRegistration} objects representing
	 * all of the courses the client is registered for, along with
	 * registration information (such as instructor status) for each
	 * course.
	 * 
	 * @return list of {@link CourseAndCourseRegistration} objects for client
	 * @throws NetCoderAuthenticationException if the client is not authenticated
	 */
	public CourseAndCourseRegistration[] getCourseAndCourseRegistrations() throws NetCoderAuthenticationException;
	
	/**
	 * Get {@link Problem}s available in given {@link Course}.
	 * The client must be registered in the Course.
	 * 
	 * @param course the Course
	 * @return the Problems available in the course
	 * @throws NetCoderAuthenticationException if the client is not authenticated,
	 *         or is not regsitered in the course 
	 */
	public Problem[] getProblems(Course course) throws NetCoderAuthenticationException;
	
	/**
	 * Get {@link ProblemAndSubmissionReceipt}s for given {@link Course}.
	 * This allows the client to get not only the Problems in the Course,
	 * but also the most recent {@link SubmissionReceipt} for each Problem.
	 * The client must be registered in the Course.
	 * 
	 * @param course the Course
	 * @return the ProblemAndSubmissionReceipts for the client's work in the Course
	 * @throws NetCoderAuthenticationException if the client is not authenticated,
	 *         or is not regsitered in the course 
	 */
	public ProblemAndSubmissionReceipt[] getProblemAndSubscriptionReceipts(Course course) throws NetCoderAuthenticationException;
}
