package org.cloudcoder.app.shared.model;

public enum ConfigurationSettingName {
	PUB_TEXT_INSTITUTION
	;

	private final String name;
	
	private ConfigurationSettingName() {
		name = super.toString().toLowerCase().replace('_', '.');
	}
	
	@Override
	public String toString() {
		return name;
	}
}
