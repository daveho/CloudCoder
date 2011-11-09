package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginPage extends CloudCoderPage {
	private LoginPageUI ui;

	@Override
	public void createWidget() {
		ui = new LoginPageUI();
		ui.setPage(this);
	}
	
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
	}

	@Override
	public CloudCoderPageUI getWidget() {
		return ui;
	}
}
