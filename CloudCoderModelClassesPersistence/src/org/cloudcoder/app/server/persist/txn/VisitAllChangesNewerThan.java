// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.server.persist.IDatabase.RetrieveChangesMode;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;

/**
 * Visit all {@link Change}s for given {@link User} and {@link Problem}
 * newer than a particular base revision (change event id).
 */
public class VisitAllChangesNewerThan extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final int problemId;
	private final User user;
	private final int baseRev;
	private ICallback<Change> visitor;
	private RetrieveChangesMode mode;

	/**
	 * Constructor.
	 * 
	 * @param problemId the {@link Problem} id
	 * @param user      the {@link User}
	 * @param baseRev   the base revision (change event id)
	 * @param visitor   the visitor (callback) to which the retrieved {@link Change}s
	 *                  should be sent
	 * @param mode      mode specifying whether or not {@link Event}s should be retrieved
	 */
	public VisitAllChangesNewerThan(int problemId, User user, int baseRev, ICallback<Change> visitor, RetrieveChangesMode mode) {
		this.problemId = problemId;
		this.user = user;
		this.baseRev = baseRev;
		this.visitor = visitor;
		this.mode = mode;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		PreparedStatement stmt;
		
		String selectEvent;
		if (mode == IDatabase.RetrieveChangesMode.RETRIEVE_CHANGES_ONLY) {
			selectEvent = "";
		} else if (mode == IDatabase.RetrieveChangesMode.RETRIEVE_CHANGES_AND_EDIT_EVENTS) {
			selectEvent = ", e.*";
		} else {
			throw new IllegalArgumentException("Mode not handled: " + mode);
		}
		stmt = prepareStatement(
				conn,
				"select c.* " + selectEvent + " from " + Change.SCHEMA.getDbTableName() + " as c, " + Event.SCHEMA.getDbTableName() + " as e " +
				" where c.event_id = e.id " +
				"   and e.id > ? " +
				"   and e.user_id = ? " +
				"   and e.problem_id = ? " +
				" order by e.id asc"
		);
		stmt.setInt(1, baseRev);
		stmt.setInt(2, user.getId());
		stmt.setInt(3, problemId);
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			Change change = new Change();
			int index = 1;
			//index = Queries.loadGeneric(change, resultSet, index, Change.SCHEMA);
			index = Queries.load(change, resultSet, index);
			if (mode == IDatabase.RetrieveChangesMode.RETRIEVE_CHANGES_AND_EDIT_EVENTS) {
				Event event = new Event();
				index = Queries.loadGeneric(event, resultSet, index, Event.SCHEMA);
				change.setEvent(event);
			}
			visitor.call(change);
		}
		
		return true;
	}

	@Override
	public String getDescription() {
		return " retrieving most recent text changes";
	}
}
