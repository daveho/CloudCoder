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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Superclass for objects that are event publishers.
 */
public abstract class Publisher {
	private static class Registration {
		Object key;
		Subscriber subscriber;
		
		Registration(Object key, Subscriber subscriber) {
			this.key = key;
			this.subscriber = subscriber;
		}
	}
	
	private List<Registration> registrationList;
	
	/**
	 * Constructor.
	 */
	protected Publisher() {
		registrationList = new ArrayList<Registration>();
	}
	
	/**
	 * Called by a Subscriber to subscribe to a particular type of event.
	 * 
	 * @param key        key indicating type of event Subscriber wants to be notified of
	 * @param subscriber the Subscriber
	 * @param registrar  the SubscriptionRegistrar that will keep track of subscriptions
	 */
	public void subscribe(Object key, Subscriber subscriber, SubscriptionRegistrar registrar) {
		registrationList.add(new Registration(key, subscriber));
		registrar.addToSubscriptionRegistry(this, subscriber, key);
	}
	
	/**
	 * Called by a Subscriber to subscribe to several types of events published by a Publisher.
	 * 
	 * @param key        array of keys indicating types of events Subscriber wants to be notified of
	 * @param subscriber the Subscriber
	 * @param registrar  the SubscriptionRegistrar that will keep track of subscriptions
	 */
	public void subscribeToAll(Object[] keyList, Subscriber subscriber, SubscriptionRegistrar registrar) {
		for (Object key : keyList) {
			registrationList.add(new Registration(key, subscriber));
			registrar.addToSubscriptionRegistry(this, subscriber, key);
		}
	}
	
	/**
	 * Called by a Subscriber to unsubscribe from a particular type of event.
	 * 
	 * @param key        key indicating type of event Subscriber no longer wants to be notified of
	 * @param subscriber the Subscriber
	 */
	public void unsubscribe(Object key, Subscriber subscriber) {
		for (Iterator<Registration> i = registrationList.iterator(); i.hasNext(); ) {
			Registration reg = i.next();
			if (reg.key.equals(key) && reg.subscriber == subscriber) {
				i.remove();
				return;
			}
		}
	}
	
	/**
	 * Called by a Subscriber to unsubscribe from all events published by this Publisher.
	 * 
	 * @param subscriber the Subscriber
	 */
	public void unsubscribeFromAll(Subscriber subscriber) {
		for (Iterator<Registration> i = registrationList.iterator(); i.hasNext(); ) {
			Registration reg = i.next();
			if (reg.subscriber == subscriber) {
				i.remove();
			}
		}
	}
	
	/**
	 * Publish an event.
	 * 
	 * @param key   key indicating the type of the event
	 * @param hint  object with additional information about the event
	 */
	public void notifySubscribers(Object key, Object hint) {
		// protect against concurrent modification exceptions
		ArrayList<Registration> registrationListCopy = new ArrayList<Registration>(registrationList);
		
		// notify all subscribers subscribed for events with this key
		for (Registration reg : registrationListCopy) {
			if (reg.key.equals(key)) {
				reg.subscriber.eventOccurred(key, this, hint);
			}
		}
	}
}
