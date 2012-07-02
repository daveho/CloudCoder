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


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Implementation of {@link EditModelObjectField} for editing string values
 * using a single-line TextBox.
 * 
 * @author David Hovemeyer
 */
public abstract class EditStringField<ModelObjectType> extends EditModelObjectField<ModelObjectType, String> {
	
	private class UI extends Composite {
		private TextBox textBox;

		public UI() {
			FlowPanel panel = new FlowPanel();
			
			panel.setStyleName("cc-editStringField");
			
			InlineLabel label = new InlineLabel(getDescription());
			panel.add(label);
			
			textBox = new TextBox();
			textBox.setWidth("300px"); // TODO: allow this to be configurable?
			panel.add(textBox);
			
			initWidget(panel);
		}

		public String getText() {
			return textBox.getText();
		}

		public void setText(String value) {
			textBox.setText(value);
		}
	}
	
	private UI ui;
	
	/**
	 * Constructor.
	 * 
	 * @param desc the description that should be used to label the UI widget
	 */
	public EditStringField(String desc) {
		super(desc);
		ui = new UI();
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.EditModelObjectField#getUI()
	 */
	@Override
	public IsWidget getUI() {
		return ui;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.EditModelObjectField#getHeightPx()
	 */
	@Override
	public double getHeightPx() {
		return 32.0;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.EditModelObjectField#commit()
	 */
	@Override
	public void commit() {
		String updatedValue = ui.getText();
		setField(getModelObject(), updatedValue);
	}
	
	@Override
	protected void onSetModelObject(ModelObjectType modelObj) {
		ui.setText(getField(modelObj));
	}
}
