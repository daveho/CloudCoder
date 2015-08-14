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

package org.cloudcoder.app.server.rpc;

import java.util.List;

import org.cloudcoder.app.client.rpc.UserService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * RPC services to access and edit user information.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class UserServiceImpl extends RemoteServiceServlet implements UserService
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public User[] getUsers(int courseId, int sectionNumber)
			throws CloudCoderAuthenticationException
	{
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), GetCoursesAndProblemsServiceImpl.class);
		logger.debug(authenticatedUser.getUsername() + " listing all users");

		// Make sure requesting user is an instructor
		if (!checkInstructorStatus(authenticatedUser, courseId, "load users in course")) {
			return new User[0];
		}

		List<User> resultList = Database.getInstance().getUsersInCourse(courseId, sectionNumber);

		User[] userArr=new User[resultList.size()];
		return resultList.toArray(userArr);
	}

	@Override
	public Boolean addUserToCourse(EditedUser editedUser, int courseId) throws CloudCoderAuthenticationException {
		// make sure user is logged in
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), GetCoursesAndProblemsServiceImpl.class);

		// Make sure requesting user is an instructor
		if (!checkInstructorStatus(authenticatedUser, courseId, "add user to course")) {
			return false;
		}

		// Add the EditedUser to the database
		User user = editedUser.getUser();
		logger.info("Adding "+user.getUsername()+" to courseId "+courseId);
		Database.getInstance().addUserToCourse(authenticatedUser, courseId, editedUser);

		return true;
	}

	@Override
	public Boolean editUser(User user)
			throws CloudCoderAuthenticationException
	{
		logger.warn("Editing userid "+user.getId()+", username "+user.getUsername());
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), GetCoursesAndProblemsServiceImpl.class);

		// Make sure requesting user has permission to edit
		// the user account
		if (!checkEditUser(authenticatedUser, user)) {
			return false;
		}

		Database.getInstance().editUser(user);
		return true;
	}

	@Override
	public Boolean editUser(EditedUser editedUser, Course course) throws CloudCoderAuthenticationException {
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), GetCoursesAndProblemsServiceImpl.class);

		// Make sure requesting user is an instructor
		if (!checkInstructorStatus(authenticatedUser, course.getId(), "edit user in course")) {
			return false;
		}

		if (editedUser.getUser() == null) {
			logger.warn("EditedUser object doesn't seem to have a User in it");
			return false;
		}

		// Edit user information
		Database.getInstance().editUser(editedUser.getUser());

		// This IDatabase method isn't actually implemented yet:
		//Database.getInstance().editRegistrationType(editedUser.getUser().getId(), course.getId(), editedUser.getRegistrationType());
		return true;
	}    

	@Override
	public void editCourseRegistrationType(int userId, int courseId, CourseRegistrationType type)
			throws CloudCoderAuthenticationException
	{
		logger.warn("Editing registration type of "+userId+" in course "+courseId);
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), GetCoursesAndProblemsServiceImpl.class);

		// Make sure requesting user is an instructor
		if (!checkInstructorStatus(authenticatedUser, courseId, "edit course registration type")) {
			// Hmm...perhaps we should throw an exception?
			return;
		}
		
		Database.getInstance().editRegistrationType(userId, courseId, type);
	}

	@Override
	public CourseRegistrationList getUserCourseRegistrationList(Course course, User user) throws CloudCoderAuthenticationException {
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), GetCoursesAndProblemsServiceImpl.class);

		// Make sure requesting user is an instructor
		if (!checkInstructorStatus(authenticatedUser, course.getId(), "get course registration list")) {
			// This will just return an empty course registration list
			return new CourseRegistrationList();
		}

		return Database.getInstance().findCourseRegistrations(user, course);
	}

	/**
	 * Check whether the given user is an instructor in the
	 * specified course.
	 * 
	 * @param user      the user
	 * @param courseId  the course id
	 * @param operation the operation being attempted
	 * @return true if the user is an instructor in the course, false otherwise
	 */
	private boolean checkInstructorStatus(User user, int courseId, String operation) throws CloudCoderAuthenticationException {
		// The requesting user must be an instructor in the course
		CourseRegistrationList regList = Database.getInstance().findCourseRegistrations(user, courseId);
		if (!regList.isInstructor()) {
			logger.warn("User {} is not an instructor in course {} (attempting " + operation + ")",
					user.getId(), courseId);
			return false;
		}
		return true;
	}


	/**
	 * Check whether the logged-in user has permission to edit
	 * a specified user account.
	 * 
	 * @param authenticatedUser the logged-in user
	 * @param editedUser        the user account being edited
	 * @return true if the logged-in user has permission to edit given user account,
	 *         false otherwise
	 */
	private boolean checkEditUser(User authenticatedUser, User editedUser) throws CloudCoderAuthenticationException {
		// Make sure either
		//   (1) the requesting user is editing his/her own account info, or
		//   (2) the requesting user is an instructor in a course in which
		//       the edited user is registered
		if (authenticatedUser.getId() == editedUser.getId()) {
			return true;
		}
		return Database.getInstance().isInstructorFor(authenticatedUser, editedUser);
	}
}
