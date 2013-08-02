// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Event;

/**
 * Load a sequence of {@link Change}s representing a user's work on a problem
 * from the database.
 * 
 * @author David Hovemeyer
 */
public class LoadChanges extends AbstractDatabaseRunnableNoAuthException<List<Change>> {

	private int userId;
	private int problemId;
	private int minEventId;
	private int maxEventId;

	/**
	 * Constructor.
	 * 
	 * @param userId      user id
	 * @param problemId   problem id
	 * @param minEventId  minimum event id (inclusive)
	 * @param maxEventId  maximum event id (inclusive)
	 */
	public LoadChanges(int userId, int problemId, int minEventId, int maxEventId) {
		this.userId = userId;
		this.problemId = problemId;
		this.minEventId = minEventId;
		this.maxEventId = maxEventId;
	}

	@Override
	public String getDescription() {
		return "loading changes";
	}

	@Override
	public List<Change> run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select e.*, c.* from cc_events as e, cc_changes as c " +
				" where e.id = c.event_id " +
				"  and e.user_id = ? " +
				"  and e.problem_id = ? " +
				"  and e.id >= ? " +
				"  and e.id <= ? " +
				"order by e.id asc"
		);
		stmt.setInt(1, userId);
		stmt.setInt(2, problemId);
		stmt.setInt(3, minEventId);
		stmt.setInt(4, maxEventId);
		
		List<Change> result = new ArrayList<Change>();
		ResultSet resultSet = executeQuery(stmt);
		
		while (resultSet.next()) {
			Event event = new Event();
			Change change = new Change();
			
			int index = DBUtil.loadModelObjectFields(event, Event.SCHEMA, resultSet);
			
			// Change objects require special code to load, due to the way the change
			// text is stored at the database level
			Queries.load(change, resultSet, index);
			
			change.setEvent(event);
			
			result.add(change);
		}
		
		return result;
	}

}
