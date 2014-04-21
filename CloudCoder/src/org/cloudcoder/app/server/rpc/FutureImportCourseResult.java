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

package org.cloudcoder.app.server.rpc;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.User;


/**
 * Future representing the (eventual) result of importing all
 * problems from an existing course.  Yields an {@link OperationResult}
 * describing the success or failure of the operation.
 * 
 * @author David Hovemeyer
 */
public class FutureImportCourseResult {
	private Object lock;
	private OperationResult result;
	
	/**
	 * Constructor.
	 */
	public FutureImportCourseResult() {
		lock = new Object();
	}
	
	/**
	 * Poll to see if the {@link OperationResult} is available yet.
	 * 
	 * @return the {@link OperationResult}, or null if the OperationResult isn't ready yet
	 */
	public OperationResult poll() {
		synchronized (lock) {
			return result;
		}
	}
	
	/**
	 * Set the {@link OperationResult}.
	 * 
	 * @param result the {@link OperationResult} to set
	 */
	public void set(OperationResult result) {
		synchronized (lock) {
			this.result = result;
		}
	}
	
	/**
	 * Start the import of problems from given source {@link Course}
	 * to given destination Course.
	 * 
	 * @param source the source {@link Course}
	 * @param dest   the destination {@link Course}
	 * @param instructor a {@link User} who is an instructor in both courses
	 */
	public void start(final Course source, final Course dest, final User instructor) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OperationResult result = Database.getInstance().importAllProblemsFromCourse(source, dest, instructor);
				set(result);
			}
		}).start();
	}
}
