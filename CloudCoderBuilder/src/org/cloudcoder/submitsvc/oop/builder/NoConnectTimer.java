// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.submitsvc.oop.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A timer to keep track when the Builder can't connect to the webapp
 * and log appropriately, without producing large numbers of log messages
 * if the webapp is offline for an extended period.
 *
 * @author David Hovemeyer
 */
public class NoConnectTimer {
	private static final Logger logger = LoggerFactory.getLogger(NoConnectTimer.class);
	
	private static final int[] REPORT_NSEC = {5, 10, 60, 300, 3600};

	private long connectionLost;
	private long nextReport;
	private int index;
	
	/**
	 * Constructor.
	 */
	public NoConnectTimer() {
		nextReport = -1;
	}
	
	/**
	 * Called to indicate that the builder is now connected to the webapp.
	 */
	public void connected() {
		nextReport = -1;
	}
	
	/**
	 * Called to indicate that the builder is not connected to the webapp.
	 * Should be called periodically.  At increasing intervals, will write
	 * a log message.
	 */
	public void notConnected() {
		long currentTime = System.currentTimeMillis();
		if (nextReport < 0) {
			connectionLost = currentTime;
			report(currentTime); // initial report
			index = 0;
			scheduleNextReport(currentTime);
		} else if (currentTime >= nextReport) {
			report(currentTime);
			if (index < REPORT_NSEC.length-1) {
				index++;
			}
			scheduleNextReport(currentTime);
		}
	}

	private void scheduleNextReport(long currentTime) {
		nextReport = currentTime + REPORT_NSEC[index]*1000L;
	}

	private void report(long currentTime) {
		if (currentTime == connectionLost) {
			logger.error(
					"Cannot connect to CloudCoder server",
					(currentTime-connectionLost)/1000);
		} else {
			logger.error(
					"Cannot connect to CloudCoder server (last connection about {} seconds ago)",
					(currentTime-connectionLost)/1000);
		}
	}
}
