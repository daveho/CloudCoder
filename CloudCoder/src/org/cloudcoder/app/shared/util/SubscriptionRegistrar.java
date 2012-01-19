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

package org.cloudcoder.app.shared.util;

/**
 * A SubscriptionRegistrar object keeps track of a set of
 * subscriptions.  The unsubscribe() method cancels
 * all of the subscriptions that the SubscriptionRegistrar has
 * recorded.
 */
public interface SubscriptionRegistrar {
	/**
	 * Record a subscription.
	 * 
	 * @param publisher  a Publisher
	 * @param subscriber a Subscriber
	 * @param key        object indicating the type of event the Subscriber is interested in
	 */
	public void addToSubscriptionRegistry(Publisher publisher, Subscriber subscriber, Object key);

	/**
	 * Cancel all recorded subscriptions.
	 */
	public void cancelAllSubscriptions();
}
