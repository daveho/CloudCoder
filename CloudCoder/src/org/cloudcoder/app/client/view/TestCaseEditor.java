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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.TestCase;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Editor for a {@link TestCase}.
 * 
 * @author David Hovemeyer
 */
public class TestCaseEditor {
	private class UI extends Composite {
		public UI() {
			FlowPanel panel = new FlowPanel();
			panel.setStyleName("cc-testCaseEditor", true);
			panel.setStyleName("cc-fieldEditor", true);
			
			// Create editors
			fieldEditorList.add(new EditStringField<TestCase>("Test case name") {
				@Override
				protected void setField(String value) {
					getModelObject().setTestCaseName(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getTestCaseName();
				}
			});
			fieldEditorList.add(new EditStringField<TestCase>("Test input") {
				@Override
				protected void setField(String value) {
					getModelObject().setInput(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getInput();
				}
				
			});
			fieldEditorList.add(new EditStringField<TestCase>("Test output") {
				@Override
				protected void setField(String value) {
					getModelObject().setOutput(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getOutput();
				}
			});
			
			// TODO: need editor for secret boolean field
			
			// Add editors to panel
			for (EditModelObjectField<TestCase, ?> editor: fieldEditorList) {
				panel.add(editor.getUI());
			}
			
			initWidget(panel);
		}
	}
	
	private UI ui;
	private List<EditModelObjectField<TestCase, ?>> fieldEditorList;
	
	public TestCaseEditor() {
		fieldEditorList = new ArrayList<EditModelObjectField<TestCase, ?>>();
		ui = new UI();
	}
	
	public IsWidget getUI() {
		return ui;
	}

	/**
	 * Set the {@link TestCase} to edit.
	 * 
	 * @param testCase the TestCase to edit
	 */
	public void setTestCase(TestCase testCase) {
		for (EditModelObjectField<TestCase, ?> editor : fieldEditorList) {
			editor.setModelObject(testCase);
		}
	}
}
