// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2019, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.Triple;
import org.cloudcoder.app.shared.model.User;

/**
 * Query to retrieve events in a "unified" way for a specific
 * course/problem/user.
 */
public class RetrieveEvents extends AbstractDatabaseRunnableNoAuthException<List<Triple<Event, Change, SubmissionReceipt>>> {

	private Problem problem;
	private User user;

	public RetrieveEvents(Problem problem, User user) {
		this.problem = problem;
		this.user = user;
	}

	@Override
	public List<Triple<Event, Change, SubmissionReceipt>> run(Connection conn) throws SQLException {
		// By doing left outer joins on both cc_changes and cc_submission_receipts
		// we get the associated Change or SubmissionReceipt data as appropriate.
		PreparedStatement stmt = prepareStatement(
				conn,
				"select e.*, c.*, r.* " +
				"  from cc_events as e" +
				"  left outer join cc_changes as c on e.id = c.event_id " +
				"  left outer join cc_submission_receipts as r on e.id = r.event_id " +
				" where " +
				"       e.user_id = ?" +
				"   and e.problem_id = ?" +
				" order by e.timestamp asc, e.id asc"
				);
		stmt.setInt(1, user.getId());
		stmt.setInt(2, problem.getProblemId());
		
		ResultSet resultSet = executeQuery(stmt);
		
		List<Triple<Event, Change, SubmissionReceipt>> result = new ArrayList<Triple<Event,Change,SubmissionReceipt>>();
		
		while (resultSet.next()) {
			Triple<Event, Change, SubmissionReceipt> triple = new Triple<Event, Change, SubmissionReceipt>();
			
			int index;
			Event event = new Event();
			index = Queries.loadGeneric(event, resultSet, 1, Event.SCHEMA);
			triple.setFirst(event);
			
			// See if there is a Change
			if (resultSet.getObject(index) != null) {
				Change change = new Change();
				Queries.loadGeneric(change, resultSet, index, Change.SCHEMA);
				change.setEvent(event); // link to Event
				triple.setSecond(change);
			}
			
			index += Change.SCHEMA.getNumFields();
			
			// See if there is a SubmissionReceipt
			if (resultSet.getObject(index) != null) {
				SubmissionReceipt receipt = new SubmissionReceipt();
				Queries.loadGeneric(receipt, resultSet, index, SubmissionReceipt.SCHEMA);
				receipt.setEvent(event);
				triple.setThird(receipt);
			}
			
			result.add(triple);
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return " retrieve events for problem/user";
	}
}
