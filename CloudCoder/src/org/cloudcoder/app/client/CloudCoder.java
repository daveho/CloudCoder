package org.cloudcoder.app.client;

import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.LoginPage;
import org.cloudcoder.app.shared.util.DefaultSubscriptionRegistrar;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CloudCoder implements EntryPoint, Subscriber {
	private Session session;
	private SubscriptionRegistrar subscriptionRegistrar;
	private CloudCoderPage currentPage;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		session = new Session();
		subscriptionRegistrar = new DefaultSubscriptionRegistrar();
		
		// subscribe to session events
		for (Session.Event eventType : Session.Event.values()) {
			session.subscribe(eventType, this, subscriptionRegistrar);
		}

		changePage(new LoginPage());
	}
	
	private void changePage(CloudCoderPage page) {
		if (currentPage != null) {
			currentPage.deactivate();
			RootLayoutPanel.get().remove(page);
		}
		RootLayoutPanel.get().add(page);
		page.setSession(session);
		page.activate();
		currentPage = page;
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.LOGIN) {
			Window.alert("Successful login!");
		}
		
	}
	
	@Override
	public void unsubscribeFromAll() {
		subscriptionRegistrar.unsubscribeAllEventSubscribers();
	}
}
