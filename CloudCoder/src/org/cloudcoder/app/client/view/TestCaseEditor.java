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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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
			
			// Create a delete button.
			// We embed it in a FlowPanel (div) so that it can be floated and positioned.
			FlowPanel deleteButtonPanel = new FlowPanel();
			deleteButtonPanel.setStyleName("cc-testCaseDeleteButton", true);
			Button deleteButton = new Button("Delete");
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (deleteHandler != null) {
						deleteHandler.run();
					}
				}
			});
			deleteButtonPanel.add(deleteButton);
			panel.add(deleteButtonPanel);
			
			// Create editors
			fieldEditorList.add(new EditStringField<TestCase>("Test case name", TestCase.TEST_CASE_NAME));
			fieldEditorList.add(new EditStringField<TestCase>("Test input", TestCase.INPUT));
			fieldEditorList.add(new EditStringField<TestCase>("Test output", TestCase.OUTPUT));
			fieldEditorList.add(new EditBooleanField<TestCase>(
					"Secret",
					"If checked, the test is secret (not revealed to students)",
					TestCase.SECRET));
			
			// Add editors to panel
			for (EditModelObjectField<TestCase, ?> editor: fieldEditorList) {
				panel.add(editor.getUI());
			}
			
			initWidget(panel);
		}
	}
	
	private UI ui;
	private List<EditModelObjectField<TestCase, ?>> fieldEditorList;
	private Runnable deleteHandler;
	private TestCase testCase;
	
	/**
	 * Constructor.
	 */
	public TestCaseEditor() {
		fieldEditorList = new ArrayList<EditModelObjectField<TestCase, ?>>();
		ui = new UI();
	}
	
	/**
	 * Set the callback to be invoked when the Delete button is clicked. 
	 * 
	 * @param deleteHandler the delete handler to set
	 */
	public void setDeleteHandler(Runnable deleteHandler) {
		this.deleteHandler = deleteHandler;
	}
	
	/**
	 * Get the UI widget.
	 * 
	 * @return the UI widget
	 */
	public IsWidget getUI() {
		return ui;
	}

	/**
	 * Set the {@link TestCase} to edit.
	 * 
	 * @param testCase the TestCase to edit
	 */
	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
		for (EditModelObjectField<TestCase, ?> editor : fieldEditorList) {
			editor.setModelObject(testCase);
		}
	}

	/**
	 * @return the TestCase being edited
	 */
	public TestCase getTestCase() {
		return testCase;
	}

	/**
	 * Commit all changes in the field editor UIs to the underlying
	 * {@link TestCase} object.
	 */
	public void commit() {
		for (EditModelObjectField<TestCase, ?> editor : fieldEditorList) {
			editor.commit();
		}
	}
}
