// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.healthmonitor;

import org.slf4j.Logger;

public class Util {
	/**
	 * Wait for a thread to complete, logging a warning
	 * if an InterruptedException occurs.
	 * 
	 * @param t           the thread to join
	 * @param logger      logger to log warning to if the thread is interrupted
	 * @param threadDesc  description of thread (for the log message)
	 */
	public static void joinQuietly(Thread t, Logger logger, String threadDesc) {
		try {
			t.join();
		} catch (InterruptedException e) {
			logger.warn("Thread {} interrupted unexpectedly", e);
		}
	}
}
