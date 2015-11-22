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
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.User;

/**
 * Query to suggest {@link User}s given a username prefix.
 * 
 * @author David Hovemeyer
 */
public class SuggestUsernames extends AbstractDatabaseRunnableNoAuthException<User[]> {

	private String prefix;

	public SuggestUsernames(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getDescription() {
		return " suggest usernames";
	}

	@Override
	public User[] run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from cc_users where username like ?");
		stmt.setString(1, prefix + "%");
		
		ResultSet resultSet = executeQuery(stmt);
		List<User> result = new ArrayList<User>();
		while (resultSet.next()) {
			User user = new User();
			DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
			result.add(user);
		}
		
		return result.toArray(new User[result.size()]);
	}

}
