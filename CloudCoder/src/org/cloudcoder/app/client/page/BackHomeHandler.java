package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.Session;

public class BackHomeHandler implements Runnable {

	private Session session;
	
	public BackHomeHandler(Session session) {
		this.session = session;
	}
	
	@Override
	public void run() {
		session.notifySubscribers(Session.Event.BACK_HOME, null);
	}

}
