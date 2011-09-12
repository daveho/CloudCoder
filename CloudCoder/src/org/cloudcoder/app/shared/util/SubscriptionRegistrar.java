// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.app.shared.util;

/**
 * A SubscriptionRegistrar object keeps track of a set of objects
 * that have subscribed to events.
 */
public interface SubscriptionRegistrar {
	/**
	 * Subscribe a subscriber to an event type published by given publisher.
	 * 
	 * @param publisher  a Publisher
	 * @param subscriber a Subscriber
	 * @param key        object indicating the type of event the Subscriber is interested in
	 */
	public void addToSubscriptionRegistry(Subscriber subscriber);

	/**
	 * Unsubscribe all event Subscribers from all events.
	 */
	public void unsubscribeAllEventSubscribers();
}
