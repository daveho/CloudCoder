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

import java.util.ArrayList;
import java.util.List;


/**
 * Default SubscriptionRegistrar implementation.
 * 
 * @author David Hovemeyer
 */
public class DefaultSubscriptionRegistrar implements SubscriptionRegistrar {
	private static class Subscription {
		Publisher publisher;
		Subscriber subscriber;
		Object key;
	}
	
	private List<Subscription> subscriptionList = new ArrayList<DefaultSubscriptionRegistrar.Subscription>();

	@Override
	public void addToSubscriptionRegistry(Publisher publisher, Subscriber subscriber, Object key) {
		Subscription s = new Subscription();
		s.publisher = publisher;
		s.subscriber = subscriber;
		s.key = key;
		subscriptionList.add(s);
	}

	@Override
	public void cancelAllSubscriptions() {
		for (Subscription s : subscriptionList) {
			s.publisher.unsubscribe(s.key, s.subscriber);
		}
	}
}
