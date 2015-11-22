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
import java.sql.SQLException;
import java.util.Arrays;

import org.cloudcoder.app.server.persist.CreateCourse;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.shared.model.CourseCreationSpec;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to create a {@link Course} from a {@link CourseCreationSpec}.
 * Creates a {@link CourseRegistration} for the course's initial instructor.
 * 
 * @author David Hovemeyer
 */
public class CreateCourseFromSpec extends AbstractDatabaseRunnableNoAuthException<OperationResult> {
	private CourseCreationSpec spec;

	public CreateCourseFromSpec(CourseCreationSpec spec) {
		this.spec = spec;
	}

	@Override
	public String getDescription() {
		return " create course";
	}

	@Override
	public OperationResult run(Connection conn) throws SQLException {
		// Find the User for the instructor
		User user = ConfigurationUtil.findUser(conn, spec.getUsername());
		if (user == null) {
			return new OperationResult(false, "Unknown user: " + spec.getUsername());
		}
		
		// Use CreateCourse to actually create the course
		CreateCourse.Instructor instructor = new CreateCourse.Instructor(user, spec.getSection());
		return CreateCourse.createCourse(conn, spec.getCourse(), Arrays.asList(instructor));
	}
}
