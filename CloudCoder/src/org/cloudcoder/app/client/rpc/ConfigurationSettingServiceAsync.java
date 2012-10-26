package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.ConfigurationSettingName;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationSettingServiceAsync {

	void getConfigurationSettingValue(ConfigurationSettingName name, AsyncCallback<String> callback);

}
