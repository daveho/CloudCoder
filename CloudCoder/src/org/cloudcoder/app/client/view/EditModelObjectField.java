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

import org.cloudcoder.app.shared.model.ModelObjectField;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Visual editor for a field of a model object.
 * 
 * @author David Hovemeyer
 */
public abstract class EditModelObjectField<ModelObjectType, FieldType> {
	private final String desc;
	private ModelObjectField<? super ModelObjectType, FieldType> field;
	private ModelObjectType modelObj;
	private boolean commitError;
	
	/**
	 * Constructor.
	 * 
	 * @param desc the description that should be used to label the UI widget
	 * @param field the {@link ModelObjectField} being edited
	 */
	public EditModelObjectField(String desc, ModelObjectField<? super ModelObjectType, FieldType> field) {
		this.desc = desc;
		this.field = field;
	}
	
	/**
	 * Get the description that should be used to label the UI widget.
	 * 
	 * @return the description that should be used to label the UI widget
	 */
	public String getDescription() {
		return desc;
	}
	
	/**
	 * Set the model object.
	 * Automatically calls {@link #update()} to force the editor
	 * to sync its contents with the model object's data.
	 * 
	 * @param modelObj the model object to set
	 */
	public void setModelObject(ModelObjectType modelObj) {
		this.modelObj = modelObj;
		update();
	}
	
	/**
	 * Get the model object.
	 * 
	 * @return the model object
	 */
	public ModelObjectType getModelObject() {
		return modelObj;
	}
	
	/**
	 * Set or clear the commit error flag.
	 * 
	 * @param commitError value to set or clear the commit error flag
	 */
	protected void setCommitError(boolean commitError) {
		this.commitError = commitError;
	}
	
	/**
	 * Get a widget for editing the field of the model object.
	 * 
	 * @return a widget for editing the field of the model object
	 */
	public abstract IsWidget getUI();
	
	/**
	 * Commit any changes made in the UI to the model object.
	 * Note that if the editor currently has an invalid value, the commit
	 * will fail.  This can be detected by calling {@link #isCommitError()}.
	 */
	public abstract void commit();
	
	/**
	 * Check whether there was an error committing to the model object.
	 * Typically, an error indicates that the value entered in the
	 * UI is too large or otherwise illegal.
	 */
	public boolean isCommitError() {
		return commitError;
	}
	
	/**
	 * Force the editor to refresh itself by synchronizing its
	 * UI state with the model object's state.  Call this method
	 * when the model object changes.  <em>Danger</em>: if the
	 * editor has local changes that haven't been saved to the
	 * model object, they will be lost!
	 */
	public abstract void update();
	
	/**
	 * Called when the underlying model object has changed.
	 * This should be overridden only in the case where the editor
	 * wants to change its state in response to the value of a
	 * field which it is <em>not</em> editing.
	 */
	public void onModelObjectChange() {
	}
	
	/**
	 * Set the value of the field in the model object.
	 * 
	 * @param value    the field value to set
	 */
	protected final void setField(FieldType value) {
		field.set(modelObj, value);
	}
	
	/**
	 * Get the value of the field in the model object.
	 * 
	 * @return the field value
	 */
	protected final FieldType getField() {
		return field.get(modelObj);
	}
	
	/**
	 * Get the {@link ModelObjectField} that gets/sets the model object field
	 * this object is editing.
	 * 
	 * @return the {@link ModelObjectField}
	 */
	protected ModelObjectField<? super ModelObjectType, FieldType> getModelObjectField() {
		return field;
	}
}
