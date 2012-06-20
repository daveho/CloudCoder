package org.cloudcoder.app.shared.model;

public class ConfigurationSetting {
	private String name;
	private String value;
	
	public ConfigurationSetting() {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
