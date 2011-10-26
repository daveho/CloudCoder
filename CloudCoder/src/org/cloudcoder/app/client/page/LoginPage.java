package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class LoginPage extends CloudCoderPage {
	private LoginPageUI ui;

	@Override
	public void activate() {
		ui = new LoginPageUI();
		ui.setPage(this);
		
		RPC.configurationSettingService.getConfigurationSettingValue(ConfigurationSettingName.PUB_TEXT_INSTITUTION, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(String result) {
				ui.setPubTextInstitution(result);
			}
		});
	}

	@Override
	public void deactivate() {
	}

	@Override
	public Widget getWidget() {
		return ui;
	}

}
