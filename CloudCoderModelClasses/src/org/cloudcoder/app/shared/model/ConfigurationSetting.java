package org.cloudcoder.app.shared.model;

import java.util.Arrays;

public class ConfigurationSetting {
	private String name;
	private String value;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema SCHEMA = new ModelObjectSchema(Arrays.asList(
			new ModelObjectField("name", String.class, 60, ModelObjectIndexType.UNIQUE),
			new ModelObjectField("value", String.class, Integer.MAX_VALUE)
	));
	
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
