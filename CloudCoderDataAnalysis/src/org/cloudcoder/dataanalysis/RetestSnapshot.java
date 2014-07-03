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

package org.cloudcoder.dataanalysis;

/**
 * A snapshot to be retested by {@link Retest}.
 * 
 * @author David Hovemeyer
 */
public class RetestSnapshot {
	/** The course id. */
	public final int courseId;
	/** The problem id. */
	public final int problemId;
	/** The user id. */
	public final int userId;
	/** The submit event id. */
	public final int submitEventId;
	/** The full text change event id. */
	public final int fullTextChangeId;
	/** The program text. */
	public final String programText;
	
	/**
	 * Constructor.
	 * 
	 * @param courseId         course id
	 * @param problemId        program id
	 * @param userId           the user id
	 * @param submitEventId    event id of the submission event
	 * @param fullTextChangeId event id of the full text change event
	 * @param programText      program text
	 */
	public RetestSnapshot(int courseId, int problemId, int userId, int submitEventId, int fullTextChangeId, String programText) {
		this.courseId = courseId;
		this.problemId = problemId;
		this.userId = userId;
		this.submitEventId = submitEventId;
		this.fullTextChangeId = fullTextChangeId;
		this.programText = programText;
	}
}