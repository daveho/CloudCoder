package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.ConfigurationSettingName;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("configurationSetting")
public interface ConfigurationSettingService extends RemoteService {
	public String getConfigurationSettingValue(ConfigurationSettingName name);
}
