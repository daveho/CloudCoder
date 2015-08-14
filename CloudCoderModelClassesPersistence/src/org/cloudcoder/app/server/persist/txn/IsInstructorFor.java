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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.User;

/**
 * Check whether a logged-in user is an instructor for a course
 * in which another user is registered.
 *  
 * @author David Hovemeyer
 */
public class IsInstructorFor extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private User authenticatedUser;
	private User editedUser;

	public IsInstructorFor(User authenticatedUser, User editedUser) {
		this.authenticatedUser = authenticatedUser;
		this.editedUser = editedUser;
	}

	@Override
	public String getDescription() {
		return " checking whether user is an instructor for specified user";
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		// Count the number of cases where the authenticated user
		// is an instructor in a course in which the edited user is
		// registered
		PreparedStatement stmt = prepareStatement(
				conn,
				"select count(*) " +
				"  from cc_course_registrations as r1, cc_course_registrations as r2 " +
				// r1 is a registration of the authenticated user
				" where r1.user_id = ? " +
				// r1 must be a registration where the authenticated user is an instructor
				"   and r1.registration_type >= ? " +
				// r2 is a registration of the edited user
				"   and r2.user_id = ? " +
				// r1 and r2 must be registrations for the same course
				"   and r1.course_id = r2.course_id"
		);
		stmt.setInt(1, authenticatedUser.getId());
		stmt.setInt(2, CourseRegistrationType.INSTRUCTOR.ordinal());
		stmt.setInt(3, editedUser.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		if (!resultSet.next()) {
			// This should not happen
			return false;
		}
		
		// If the query proves that there is at
		// least one case where the authenticated user is an instructor
		// for a course in which the edited user is registered,
		// return true, otherwise return false.
		int count = resultSet.getInt(1);
		return count > 1;
	}
}
