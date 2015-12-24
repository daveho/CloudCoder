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
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.User;

public class FindCourseRegistrationsForUser extends AbstractDatabaseRunnableNoAuthException<CourseRegistrationList> {
	private User user;

	public FindCourseRegistrationsForUser(User user) {
		this.user = user;
	}

	@Override
	public String getDescription() {
		return " find course registrations for user";
	}

	@Override
	public CourseRegistrationList run(Connection conn) throws SQLException {
		// Find user
		
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from cc_course_registrations " +
				" where user_id = ?"
		);
		stmt.setInt(1, user.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		CourseRegistrationList regList = new CourseRegistrationList();
		while (resultSet.next()) {
			CourseRegistration reg = new CourseRegistration();
			DBUtil.loadModelObjectFields(reg, CourseRegistration.SCHEMA, resultSet);
			regList.getList().add(reg);
		}
		
		return regList;
	}
}
