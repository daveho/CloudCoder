// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.server.persist.PasswordUtil;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Anonymization;
import org.cloudcoder.app.shared.model.User;

/**
 * Destructively anonymize all user account data.
 * 
 * @author David Hovemeyer
 */
public class AnonymizeUserData extends
		AbstractDatabaseRunnableNoAuthException<List<Anonymization>> {

	private String genPasswd;
	private Runnable progressCallback;

	/**
	 * Constructor.
	 * 
	 * @param genPasswd plaintext password to use for all anonymized user accounts
	 * @param progressCallback callback to execute as accounts are anonymized
	 */
	public AnonymizeUserData(String genPasswd, Runnable progressCallback) {
		this.genPasswd = genPasswd;
		this.progressCallback = progressCallback;
	}

	@Override
	public List<Anonymization> run(Connection conn) throws SQLException {
		List<Anonymization> anonymizationList = new ArrayList<Anonymization>();
		
		// Get all users
		PreparedStatement getUsers = prepareStatement(conn, "select * from cc_users");
		ResultSet resultSet = executeQuery(getUsers);
		while (resultSet.next()) {
			User user = new User();
			DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
			
			Anonymization a = new Anonymization(
					user.getId(), "x", "x", user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getWebsite());
			anonymizationList.add(a);
		}
//		System.out.print("[" + anonymizationList.size() + " users]");
		
		// Generate fake usernames and change each user to have
		// the same password
		for (Anonymization a : anonymizationList) {
			a.setAnonUsername(String.format("u%05d", a.getUserId()));
			a.setGenPassword(genPasswd);
		}
		
		// Anonymize!
		PreparedStatement update = prepareStatement(
				conn,
				"update cc_users " +
				"   set username = ?, password_hash = ?, firstname = ?, lastname = ?, email = ?, website = ? " +
				" where id = ?" 
		);
		int numBatched = 0;
		for (Anonymization a : anonymizationList) {
			update.setString(1, a.getAnonUsername());
			String passwordHash = PasswordUtil.hashPassword(a.getGenPassword());
			update.setString(2, passwordHash);
			update.setString(3, a.getAnonUsername());
			update.setString(4, a.getAnonUsername());
			update.setString(5, a.getAnonUsername() + "@anon.edu");
			update.setString(6, "x");
			update.setInt(7, a.getUserId());
			
			update.addBatch();
			
			numBatched++;
			
			if (numBatched >= 20) {
				update.executeBatch();
				numBatched = 0;
				progressCallback.run();
			}
		}
		
		if (numBatched > 0) {
			update.executeBatch();
			progressCallback.run();
		}
		
		return anonymizationList;
	}

	@Override
	public String getDescription() {
		return " anonymizing user data";
	}

}
