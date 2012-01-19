// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.app.shared.model;

/**
 * Interface for classes which "contain" and event.
 * Really, these are classes (such as Change and SubmissionReceipt)
 * that are "subtypes" of Event, in the sense that each record
 * in the database is linked with a corresponding event object.
 * However, we don't use actual Java inheritance.
 * 
 * @author David Hovemeyer
 */
public interface IContainsEvent {

	public abstract void setEventId(int eventId);

	public abstract int getEventId();

	public abstract void setEvent(Event event);

	public abstract Event getEvent();

}