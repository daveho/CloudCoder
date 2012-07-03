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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Edit a string field of a model object using an {@link AceEditor}.
 * 
 * @author David Hovemeyer
 */
public abstract class EditStringFieldWithAceEditor<ModelObjectType>
		extends EditModelObjectField<ModelObjectType, String> {
	
	public static final int HEIGHT_PX = 320;
	public static final int LABEL_HEIGHT_PX = 20;
	
	private class UI extends ResizeComposite {
		private AceEditor editor;
		private boolean editorStarted;

		public UI() {
			LayoutPanel panel = new LayoutPanel();
			
			panel.setWidth("600px");
			panel.setHeight(HEIGHT_PX + "px");
			
			Label label = new Label(getDescription());
			panel.add(label);
			panel.setWidgetLeftRight(label, 0.0, Unit.PX, 0.0, Unit.PX);
			panel.setWidgetTopHeight(label, 0.0, Unit.PX, 32.0, Unit.PX);

			editor = new AceEditor(true);
			panel.add(editor);
			panel.setWidgetLeftRight(editor, 0.0, Unit.PX, 0.0, Unit.PX);
			panel.setWidgetBottomHeight(editor, 0.0, Unit.PX, (HEIGHT_PX - (LABEL_HEIGHT_PX + 8)), Unit.PX);
			editorStarted = false;
			
			initWidget(panel);
		}

		public boolean isEditorStarted() {
			return editorStarted;
		}

		public void startEditor() {
			editor.startEditor();
			if (editorMode != null) {
				editor.setMode(editorMode);
			}
			editor.setTheme(editorTheme);
			editor.setFontSize("14px");
			editorStarted = true;
		}
		
		public void setText(String text) {
			editor.setText(text);
		}

		public String getText() {
			return editor.getText();
		}

		public void resetEditorMode() {
			if (editorMode != null) {
				editor.setMode(editorMode);
			}
		}

		public void resetEditorTheme() {
			if (editorTheme != null) {
				editor.setTheme(editorTheme);
			}
		}
	}

	private AceEditorMode editorMode;
	private AceEditorTheme editorTheme;
	private UI ui;

	public EditStringFieldWithAceEditor(String desc) {
		super(desc);
		this.ui = new UI();
	}
	
	/**
	 * Set the editor mode.
	 * 
	 * @param editorMode the editorMode to set
	 */
	public void setEditorMode(AceEditorMode editorMode) {
		this.editorMode = editorMode;
		if (ui.isEditorStarted()) {
			ui.resetEditorMode();
		}
	}
	
	/**
	 * Set the editor theme.
	 * 
	 * @param editorTheme the editorTheme to set
	 */
	public void setEditorTheme(AceEditorTheme editorTheme) {
		this.editorTheme = editorTheme;
		if (ui.isEditorStarted()) {
			ui.resetEditorTheme();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#getUI()
	 */
	@Override
	public IsWidget getUI() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#getHeightPx()
	 */
	@Override
	public double getHeightPx() {
		return (double) HEIGHT_PX;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#commit()
	 */
	@Override
	public void commit() {
		setField(ui.getText());
	}

	@Override
	public void update() {
		if (!ui.isEditorStarted()) {
			// At this point, we'll assume that the UI has been added to the page DOM,
			// so it's safe to start the AceEditor.
			ui.startEditor();
		}
		
		ui.setText(getField());
	}
}
