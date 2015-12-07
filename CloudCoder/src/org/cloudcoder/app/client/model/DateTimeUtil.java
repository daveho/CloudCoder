// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.model;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Utility methods for dealing with dates and times.
 * Note that all times are local times.
 * 
 * @author David Hovemeyer
 */
public class DateTimeUtil {
	private static final String DATE_FORMAT_STRING = "yyyy MM dd";
	private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(DATE_FORMAT_STRING);

	// 60 seconds per minute, 1000 milliseconds per second
	private static final long MILLIS_PER_MINUTE = 60L * 1000L;
	// 60 minutes per hour 
	private static final long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;

	/**
	 * Convert a {@link Date} into a {@link Date} representing
	 * midnight on the same day.
	 * 
	 * @param d the {@link Date}
	 * @return a {@link Date} representing midnight on the same day
	 */
	public static Date toMidnight(Date d) {
		return DateTimeUtil.DATE_FORMAT.parse(DateTimeUtil.DATE_FORMAT.format(d));
	}

	/**
	 * @param value
	 * @return
	 */
	public static Long parseHourAndMinute(String value) {
		value = value.trim();
		String[] comp = value.split(":");
		if (comp.length != 2) {
			return 0L; // invalid, should report as error...
		}
		try {
			int hours = Integer.parseInt(comp[0]);
			int minutes = Integer.parseInt(comp[1]);
			return (hours * MILLIS_PER_HOUR) + (minutes * MILLIS_PER_MINUTE);
		} catch (NumberFormatException e) {
			return 0L; // invalid, should report as error...
		}
	}
	
	private static final NumberFormat fmt = NumberFormat.getFormat("00");

	/**
	 * Format given time value in HH:MM format.
	 * The time value represents milliseconds since midnight.
	 * 
	 * @param value the time value
	 * @return time value formatted as HH:MM
	 */
	public static String formatHourAndMinute(long value) {
		long minPart = value % MILLIS_PER_HOUR;
		long minutes = minPart / MILLIS_PER_MINUTE;
		long hours = (value - minPart) / MILLIS_PER_HOUR;
		return fmt.format(hours) + ":" + fmt.format(minutes);
	}
}
