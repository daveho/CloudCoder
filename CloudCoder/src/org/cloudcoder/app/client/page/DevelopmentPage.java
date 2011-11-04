package org.cloudcoder.app.client.page;

import com.google.gwt.user.client.ui.Widget;

public class DevelopmentPage extends CloudCoderPage {
	private DevelopmentPageUI ui;
	
	public DevelopmentPage() {
	}

	@Override
	public void createWidget() {
		ui = new DevelopmentPageUI();
	}
	
	@Override
	public void activate() {
		addSessionObject(new ChangeList());
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	@Override
	public Widget getWidget() {
		return ui;
	}

}
