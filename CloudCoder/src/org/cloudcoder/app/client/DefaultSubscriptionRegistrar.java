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

package org.cloudcoder.app.client;

import java.util.HashSet;
import java.util.Set;

import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

/**
 * Default SubscriptionRegistrar implementation.
 * Uses a HashSet to keep track of Event subscribers.
 * 
 * @author David Hovemeyer
 */
public class DefaultSubscriptionRegistrar implements SubscriptionRegistrar {
	private Set<Subscriber> eventSubscriberSet;
	
	public DefaultSubscriptionRegistrar() {
		this.eventSubscriberSet = new HashSet<Subscriber>();
	}

	@Override
	public void addToSubscriptionRegistry(Subscriber subscriber) {
		eventSubscriberSet.add(subscriber);
	}

	@Override
	public void unsubscribeAllEventSubscribers() {
		for (Subscriber subscriber : eventSubscriberSet) {
			subscriber.unsubscribeFromAll();
		}
	}
}
