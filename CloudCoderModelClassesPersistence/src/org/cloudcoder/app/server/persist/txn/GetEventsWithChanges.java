// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.Pair;

/**
 * Transaction to get all {@link Event}s in a range of event
 * ids for a particular problem/user.  Also retrieves
 * {@link Change}s for all change events in the range.
 * 
 * @author David Hovemeyer
 */
public class GetEventsWithChanges extends AbstractDatabaseRunnableNoAuthException<List<Pair<Event, Change>>> {
	private int userId;
	private int problemId;
	private int startEventId;
	private int endEventId;

	public GetEventsWithChanges(int userId, int problemId, int startEventId, int endEventId) {
		this.userId = userId;
		this.problemId = problemId;
		this.startEventId = startEventId;
		this.endEventId = endEventId;
	}

	@Override
	public String getDescription() {
		return " getting events and changes";
	}

	@Override
	public List<Pair<Event, Change>> run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select e.*, c.* " +
				" from cc_events as e " +
				" left outer join cc_changes as c " +
				"   on e.id = c.event_id " +
				" where e.user_id = ? " +
				"   and e.problem_id = ? " +
				"   and e.id >= ? " +
				"   and e.id <= ? "
		);
		stmt.setInt(1, userId);
		stmt.setInt(2, problemId);
		stmt.setInt(3, startEventId);
		stmt.setInt(4, endEventId);
		
		List<Pair<Event, Change>> result = new ArrayList<Pair<Event, Change>>();
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			Event e = new Event();
			int index = Queries.loadGeneric(e, resultSet, 1, Event.SCHEMA);
			Change c = null;
			if (resultSet.getObject(index) != null) {
				c = new Change();
				Queries.load(c, resultSet, index);
				c.setEvent(e);
			}
			Pair<Event, Change> pair = new Pair<Event, Change>(e, c);
			result.add(pair);
		}
		
		return result;
	}

}
