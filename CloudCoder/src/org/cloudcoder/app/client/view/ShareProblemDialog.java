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

package org.cloudcoder.app.client.view;

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.shared.model.ProblemLicense;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author David Hovemeyer
 */
public class ShareProblemDialog extends DialogBox {
	private Map<Integer, ProblemLicense> indexToLicenseMap;
	private ListBox licenseListBox;
	private Label licenseNameLabel;
	private Label licenseUrlLabel;
	private Button shareButton;
	private Button cancelButton;
	
	public ShareProblemDialog() {
		FlowPanel panel = new FlowPanel();
		
		panel.add(new Label("Choose a license, then click Share to upload this exercise to the exercise repository"));

		this.indexToLicenseMap = new HashMap<Integer, ProblemLicense>();
		this.licenseListBox = new ListBox();
		int count = 0;
		for (ProblemLicense license : ProblemLicense.values()) {
			if (license.isPermissive()) {
				indexToLicenseMap.put(count++, license);
				licenseListBox.addItem(license.name());
			}
		}
		panel.add(licenseListBox);
		
		licenseListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				onLicenseChange();
			}
		});
		
		// As licences are chosen, the license name and URL should be updated
		this.licenseNameLabel = new Label("");
		this.licenseUrlLabel = new Label("");
		panel.add(licenseNameLabel);
		panel.add(licenseUrlLabel);
		
		// TODO: UI for entering repository username and password
		
		this.shareButton = new Button("Share!");
		this.cancelButton = new Button("Cancel");
		this.cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// dismiss dialog
			}
		});
		
		panel.add(shareButton);
		panel.add(cancelButton);
		
		add(panel);
	}

	protected void onLicenseChange() {
		int selIndex = licenseListBox.getSelectedIndex();
		ProblemLicense license = indexToLicenseMap.get(selIndex);
		licenseNameLabel.setText(license.getName());
		licenseUrlLabel.setText(license.getUrl());
	}
	
}
