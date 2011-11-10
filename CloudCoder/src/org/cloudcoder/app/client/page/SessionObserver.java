package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

public interface SessionObserver {

	public void activate(Session session,
			SubscriptionRegistrar subscriptionRegistrar);

}