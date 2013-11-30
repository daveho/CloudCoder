// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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
import org.cloudcoder.app.shared.model.User;

/**
 * Find {@link User} given the user's email address.
 * 
 * @author David Hovemeyer
 */
public class FindUserForEmailAddress extends AbstractDatabaseRunnableNoAuthException<User> {
	private String emailAddress;
	
	/**
	 * Constructor.
	 * 
	 * @param emailAddress the email address of the user to retrieve
	 */
	public FindUserForEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	@Override
	public String getDescription() {
		return "find user for email address";
	}

	@Override
	public User run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from cc_users where email = ?"
		);
		stmt.setString(1, emailAddress);
		
		ResultSet resultSet = executeQuery(stmt);
		
		if (!resultSet.next()) {
			return null;
		}
		
		User user = new User();
		Queries.loadGeneric(user, resultSet, 0, User.SCHEMA);
		return user;
	}
}
