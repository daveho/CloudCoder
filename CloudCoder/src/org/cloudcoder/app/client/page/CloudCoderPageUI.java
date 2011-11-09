package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.IsWidget;

public interface CloudCoderPageUI extends IsWidget {
	public void setPage(CloudCoderPage page);
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar);
}
