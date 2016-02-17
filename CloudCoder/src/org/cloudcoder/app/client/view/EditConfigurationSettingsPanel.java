// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.view;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.page.SessionUtil;
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.client.validator.TextBoxNonemptyValidator;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Admin UI for editing {@link ConfigurationSetting}s.
 * This is useful for setting, e.g., the institution name.
 * 
 * @author David Hovemeyer
 */
public class EditConfigurationSettingsPanel extends ValidatedFormUI implements SessionObserver, Subscriber {
	private CloudCoderPage page;
	private ConfigurationSettingName[] names;
	private List<String> currentValues;
	private List<TextBox> textBoxes;
	private Button updateButton;
	private Runnable onUpdateCallback;
	
	public EditConfigurationSettingsPanel(CloudCoderPage page) {
		this.page = page;
		
		setWidth("100%");
		setHeight("200px");
		
		double y = 10.0;
		
		this.names = ConfigurationSettingName.values();
		this.currentValues = new ArrayList<String>();
		this.textBoxes = new ArrayList<TextBox>();
		for (ConfigurationSettingName name : names) {
			TextBox textBox = new TextBox();
			textBoxes.add(textBox);
			y = addWidget(y, textBox, name.toString(), new TextBoxNonemptyValidator());
			
			// All textboxes will be disabled until the current values
			// are loaded successfully
			textBox.setEnabled(false);
		}
		
		this.updateButton = new Button("Update settings");
		y = addWidget(y, updateButton, "", new NoopFieldValidator());
		this.updateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (onUpdateCallback != null) {
					onUpdateCallback.run();
				}
			}
		});
	}
	
	/**
	 * Set the callback to run when the "Update settings" button is clicked.
	 * 
	 * @param onUpdateCallback the onUpdateCallback to set
	 */
	public void setOnUpdateCallback(Runnable onUpdateCallback) {
		this.onUpdateCallback = onUpdateCallback;
	}
	
	/**
	 * Get modified {@link ConfigurationSetting}s.
	 * 
	 * @return modified {@link ConfigurationSetting}s
	 */
	public List<ConfigurationSetting> getModifiedConfigurationSettings() {
		List<ConfigurationSetting> result = new ArrayList<ConfigurationSetting>();
		for (int i = 0; i < names.length; i++) {
			TextBox textBox = textBoxes.get(i);
			if (textBox.isEnabled() && !textBox.getText().equals(currentValues.get(i))) {
				ConfigurationSetting modifiedSetting = new ConfigurationSetting();
				modifiedSetting.setName(names[i]);
				modifiedSetting.setValue(textBox.getText());
				result.add(modifiedSetting);
			}
		}
		return result;
	}

	/**
	 * Mark all modified {@link ConfigurationSetting}s as being up to date.
	 */
	public void markUpToDate() {
		for (int i = 0; i < names.length; i++) {
			currentValues.set(i, textBoxes.get(i).getText());
		}
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		GWT.log(">>> Activating EditConfigurationSettingsPanel... <<<");
		
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

		// Force configuration settings to be loaded
		SessionUtil.loadAllConfigurationSettings(page);
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (hint instanceof ConfigurationSetting[]) {
			currentValues.clear();
			// These are guaranteed to be ordered in the order of
			// the ConfigurationSettingName enum, which is how the
			// textboxes are ordered as well.
			ConfigurationSetting[] settings = (ConfigurationSetting[]) hint;
			for (int i = 0; i < names.length; i++) {
				boolean valid = settings[i] != null;
				String value = valid ? settings[i].getValue() : "";
				TextBox textBox = textBoxes.get(i);
				currentValues.add(value);
				textBox.setText(value);
				textBox.setEnabled(valid);
			}
		}
	}

	@Override
	public void clear() {
		// Do nothing - in general, there is no harm in leaving the values
		// in the UI
	}

}
