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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.cloudcoder.app.shared.model.WorkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find all {@link WorkSession}s in a course.
 * 
 * @author David Hovemeyer
 */
public class FindWorkSessions extends AbstractDatabaseRunnableNoAuthException<List<WorkSession>> {
	private static final Logger logger = LoggerFactory.getLogger(FindWorkSessions.class);
	
	private SnapshotSelectionCriteria criteria;
	private int separationSeconds;

	public FindWorkSessions(SnapshotSelectionCriteria criteria, int separationSeconds) {
		this.criteria = criteria;
		this.separationSeconds = separationSeconds;
	}
	
	private static class EventInCourse {
		Event event;
		int courseId;
		public EventInCourse(Event e, int courseId) {
			this.event = e;
			this.courseId = courseId;
		}
	}

	@Override
	public List<WorkSession> run(Connection conn) throws SQLException {
		// Select all events, ordered first by user id, then by timestamp
		PreparedStatement stmt = prepareStatement(
				conn,
				"select e.*, p.course_id from cc_events as e, cc_problems as p " +
				" where e.problem_id = p.problem_id " +
				"   and (? < 0 or p.course_id = ?) " +
				"   and (? < 0 or e.user_id = ?) " +
				"   and (? < 0 or p.problem_id = ?) " +
				" order by e.user_id, e.timestamp"
		);
		stmt.setInt(1, criteria.getCourseId());
		stmt.setInt(2, criteria.getCourseId());
		stmt.setInt(3, criteria.getUserId());
		stmt.setInt(4, criteria.getUserId());
		stmt.setInt(5, criteria.getProblemId());
		stmt.setInt(6, criteria.getProblemId());
		
		List<WorkSession> workSessions = new ArrayList<WorkSession>();
		ResultSet resultSet = executeQuery(stmt);

		// Scan results to find sequences of events representing work on the same
		// problem by the same user, not separated by more than the maximum
		// separation in time.
		EventInCourse start = null;
		EventInCourse end = null;
		
		int count = 0;
		while (resultSet.next()) {
			count++;
			Event e = new Event();
			int index = DBUtil.loadModelObjectFields(e, Event.SCHEMA, resultSet);
			int courseId = resultSet.getInt(index);
			
			EventInCourse eventInCourse = new EventInCourse(e, courseId);
			
			if (start == null) {
				start = eventInCourse;
				end = eventInCourse;
			} else if (isDifferentSession(start, eventInCourse)) {
				workSessions.add(createSession(start, end));
				start = eventInCourse;
				end = eventInCourse;
			} else {
				end = eventInCourse;
			}
		}
		if (start != null) {
			workSessions.add(createSession(start, end));
		}
		logger.info("FindWorkSessions: processed {} events\n", count);
		
		return workSessions;
	}

	private boolean isDifferentSession(EventInCourse start, EventInCourse e) {
		return start.event.getUserId() != e.event.getUserId()
				|| start.event.getProblemId() != e.event.getProblemId()
				|| start.courseId != e.courseId
				|| e.event.getTimestamp() - start.event.getTimestamp() > separationSeconds * 1000L;
	}

	private WorkSession createSession(EventInCourse start, EventInCourse end) {
		WorkSession session = new WorkSession();
		session.setCourseId(start.courseId);
		session.setProblemId(start.event.getProblemId());
		session.setUserId(start.event.getUserId());
		session.setStartEventId(start.event.getId());
		session.setEndEventId(end.event.getId());
		session.setStartTime(start.event.getTimestamp());
		session.setEndTime(end.event.getTimestamp());
		return session;
	}

	@Override
	public String getDescription() {
		return " finding work sessions in course";
	}

}
