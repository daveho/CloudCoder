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

import org.cloudcoder.app.server.persist.SnapshotCallback;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction to retrieve snapshots/submissions matching given
 * {@link SnapshotSelectionCriteria}.
 * 
 * @author David Hovemeyer
 */
public class RetrieveSnapshots extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private static final Logger logger = LoggerFactory.getLogger(RetrieveSnapshots.class);

	private SnapshotSelectionCriteria criteria;
	private SnapshotCallback callback;

	public RetrieveSnapshots(SnapshotSelectionCriteria criteria, SnapshotCallback callback) {
		this.criteria = criteria;
		this.callback = callback;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		// FIXME: only supports retrieving explicit submissions, not intermediate snapshots
		StringBuilder sql = new StringBuilder();
		sql.append(
				"select e.*, ch.*, sr.event_id, p.course_id" +
				"  from cc_submission_receipts as sr, cc_events as e, cc_changes as ch, cc_problems as p" +
				" where sr.last_edit_event_id = e.id " +
				"   and e.id = ch.event_id " +
				"   and e.problem_id = p.problem_id"
		);
		if (criteria.getCourseId() != SnapshotSelectionCriteria.ANY) {
			sql.append(" and p.course_id = ?");
		}
		if (criteria.getProblemId() != SnapshotSelectionCriteria.ANY) {
			sql.append(" and e.problem_id = ?");
		}
		if (criteria.getUserId() != SnapshotSelectionCriteria.ANY) {
			sql.append(" and e.user_id = ?");
		}
		
		String query = sql.toString();
		logger.info("Retrieving snapshots: {}", query);
		
		PreparedStatement stmt = prepareStatement(conn, query);
		int place = 1;
		if (criteria.getCourseId() != SnapshotSelectionCriteria.ANY) {
			stmt.setInt(place++, criteria.getCourseId());
		}
		if (criteria.getProblemId() != SnapshotSelectionCriteria.ANY) {
			stmt.setInt(place++, criteria.getProblemId());
		}
		if (criteria.getUserId() != SnapshotSelectionCriteria.ANY) {
			stmt.setInt(place++, criteria.getUserId());
		}
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			Event e = new Event(); // the Event of the full-text Change event
			Change change = new Change(); // the full-text Change
			int index = 1;
			
			// Load the Event for the full-text Change
			index = DBUtil.loadModelObjectFields(e, Event.SCHEMA, resultSet, index);

			// Load the full-text Change: note that loadModelObjectFields doesn't work for Change objects
			Queries.load(change, resultSet, index);
			index += Change.SCHEMA.getNumFields();
			
			// Retrieve the submit event id and the course id.
			int submitEventId = resultSet.getInt(index++);
			int courseId = resultSet.getInt(index++);
			
			if (change.getType() != ChangeType.FULL_TEXT) {
				logger.error("Change event {} doesn't have a full text Change", e.getId());
			} else {
				callback.onSnapshotFound(submitEventId, e.getId(), courseId, e.getProblemId(), e.getUserId(), change.getText());
			}
		}
		
		return true;
	}

	@Override
	public String getDescription() {
		return " retrieving snapshots/submissions";
	}

}
