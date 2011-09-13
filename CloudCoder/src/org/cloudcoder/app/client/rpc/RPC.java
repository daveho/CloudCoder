package org.cloudcoder.app.client.rpc;

import com.google.gwt.core.client.GWT;

public abstract class RPC {
	public static final ConfigurationSettingServiceAsync configurationSettingService = GWT.create(ConfigurationSettingService.class);
}
