// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.shared.model;

/**
 * A configuration setting.
 * Represents a value that is customized per-installation
 * (such as the institution name).
 * 
 * @author David Hovemeyer
 */
public class ConfigurationSetting implements IModelObject<ConfigurationSetting> {
	private String name;
	private String value;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<ConfigurationSetting> SCHEMA = new ModelObjectSchema<ConfigurationSetting>("configuration_setting")
		.add(new ModelObjectField<ConfigurationSetting, String>("name", String.class, 60, ModelObjectIndexType.UNIQUE) {
			public void set(ConfigurationSetting obj, String value) { obj.setName(value); }
			public String get(ConfigurationSetting obj) { return obj.getName(); }
		})
		.add(new ModelObjectField<ConfigurationSetting, String>("value", String.class, Integer.MAX_VALUE) {
			public void set(ConfigurationSetting obj, String value) { obj.setValue(value); }
			public String get(ConfigurationSetting obj) { return obj.getValue(); }
		});
	
	public ConfigurationSetting() {
		
	}
	
	@Override
	public ModelObjectSchema<ConfigurationSetting> getSchema() {
		return SCHEMA;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setName(ConfigurationSettingName settingName) {
		this.name = settingName.toString();
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
