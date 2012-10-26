package org.cloudcoder.app.server.rpc;

import org.cloudcoder.app.client.rpc.ConfigurationSettingService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigurationSettingServiceImpl extends RemoteServiceServlet
		implements ConfigurationSettingService {
	private static final long serialVersionUID = 1L;

	@Override
	public String getConfigurationSettingValue(ConfigurationSettingName name) {
		ConfigurationSetting configurationSetting = Database.getInstance().getConfigurationSetting(name);
		return configurationSetting != null ? configurationSetting.getValue() : null;
	}

}
