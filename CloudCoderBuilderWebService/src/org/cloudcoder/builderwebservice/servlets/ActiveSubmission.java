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

package org.cloudcoder.builderwebservice.servlets;

import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;

/**
 * An active submission to the builder web service: contains an
 * {@link IFutureSubmissionResult} along with a timestamp of when
 * the request entered the system.  The timestamp allows us to periodically
 * scan the active submissions are remove the ones which are too
 * old (where the client did not poll for completion in a timely
 * manner).
 * 
 * @author David Hovemeyer
 */
public class ActiveSubmission {
	private final IFutureSubmissionResult result;
	private final long timestamp;
	
	/**
	 * Constructor.
	 * 
	 * @param result     the {@link IFutureSubmissionResult}
	 * @param timestamp  the timestamp (marking when the request entered the system)
	 */
	public ActiveSubmission(IFutureSubmissionResult result, long timestamp) {
		this.result = result;
		this.timestamp = timestamp;
	}
	
	/**
	 * @return the {@link IFutureSubmissionResult}
	 */
	public IFutureSubmissionResult getResult() {
		return result;
	}
	
	/**
	 * @return the timestamp marking when the request entered the system
	 */
	public long getTimestamp() {
		return timestamp;
	}
}
