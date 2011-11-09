package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LogoutHandler implements Runnable {
	private Session session;
	
	public LogoutHandler(Session session) {
		this.session = session;
	}

	@Override
	public void run() {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Could not log out?", caught);

				// well, at least we tried
				clearSessionData();
			}

			@Override
			public void onSuccess(Void result) {
				// server has purged the session
				clearSessionData();
			}

			protected void clearSessionData() {
				// Clear the User object from the session.
				session.remove(User.class);

				// Publish the LOGOUT event.
				session.notifySubscribers(Session.Event.LOGOUT, null);
			}
		};

		RPC.loginService.logout(callback);
	}
}
