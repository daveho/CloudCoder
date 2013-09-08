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
 * Query to get all {@link Change}s (edits) for all users on a
 * specified problem.
 * 
 * @author David Hovemeyer
 */
public class LoadChangesForAllUsersOnProblem extends AbstractDatabaseRunnableNoAuthException<List<Change>> {
	private int problemId;

	/**
	 * Constructor.
	 * 
	 * @param problemId the problem id
	 */
	public LoadChangesForAllUsersOnProblem(int problemId) {
		this.problemId = problemId;
	}

	@Override
	public String getDescription() {
		return "get all changes for all users for problem";
	}

	@Override
	public List<Change> run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select e.*, c.* " +
				"  from cc_events as e, cc_changes as c " +
				" where e.id = c.event_id " +
				"   and e.problem_id = ? " +
				"order by e.user_id asc, e.id asc"
		);
		stmt.setInt(1, problemId);
		
		List<Change> result = new ArrayList<Change>();
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			Event event = new Event();
			int index = DBUtil.loadModelObjectFields(event, Event.SCHEMA, resultSet);
			Change change = new Change();
			change.setEvent(event);
			Queries.load(change, resultSet, index);
			result.add(change);
		}
		
		return result;
	}
}
