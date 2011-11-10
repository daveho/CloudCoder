package org.cloudcoder.app.client.page;

import org.cloudcoder.app.shared.model.TestResult;

public class DevelopmentPage extends CloudCoderPage {
	private DevelopmentPageUI ui;
	
	public DevelopmentPage() {
	}

	@Override
	public void createWidget() {
		ui = new DevelopmentPageUI();
		ui.setPage(this);
	}
	
	@Override
	public void activate() {
		addSessionObject(new ChangeList());
		addSessionObject(new TestResult[0]);
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	@Override
	public CloudCoderPageUI getWidget() {
		return ui;
	}

}
