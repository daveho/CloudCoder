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

package org.cloudcoder.app.server.persist;

import org.cloudcoder.app.shared.model.SubmissionReceipt;

/**
 * Callback interface for retrieving submissions/snapshots from the database.
 * 
 * @author David Hovemeyer
 */
public interface SnapshotCallback {
	/**
	 * Called on retrieval of a snapshot.
	 * 
	 * @param eventId      the submission event id (which also identifies the {@link SubmissionReceipt}
	 * @param fullTextChangeId the event id id of the full text {@link Change} event
	 * @param courseId     the course id
	 * @param problemId    the problem id
	 * @param userId       the user id
	 * @param programText  the program text
	 */
	public void onSnapshotFound(int submitEventId, int fullTextChangeId, int courseId, int problemId, int userId, String programText);
}
