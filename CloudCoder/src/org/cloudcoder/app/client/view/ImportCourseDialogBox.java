// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.ICallback;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Dialog box for importing all problems from another course.
 * Invokes a callback when the user selects a course.
 * 
 * @author David Hovemeyer
 */
public class ImportCourseDialogBox extends DialogBox {
	public static final double BORDER_PX = 20.0;
	public static final double TITLE_LABEL_HEIGHT_PX = 32.0;
	public static final double WIDTH_PX = 480.0;
	
	
	public static final double INFO_LABEL_HEIGHT_PX = 24.0;
	
	public static final double BUTTON_WIDTH_PX = 160.0;
	public static final double BUTTON_HEIGHT_PX = 28.0;
	
	public static final double ERROR_LABEL_HEIGHT_PX = 16.0;
	
	public static final double HEIGHT_PX =
			BORDER_PX + TITLE_LABEL_HEIGHT_PX + 10.0 +
			INFO_LABEL_HEIGHT_PX + 10.0 +
			ImportCourseSelectionView.HEIGHT_PX + 10.0 + ERROR_LABEL_HEIGHT_PX + 10.0 + BUTTON_HEIGHT_PX + 10.0;
	
	private ImportCourseSelectionView selectionView;
	private Button importButton;
	private Button cancelButton;
	private InlineLabel errorLabel;
	private ICallback<CourseAndCourseRegistration> selectCourseCallback;

	public ImportCourseDialogBox(Session session) {
		LayoutPanel panel = new LayoutPanel();
		panel.setWidth(WIDTH_PX + "px");
		panel.setHeight(HEIGHT_PX + "px");
		
		InlineLabel title = new InlineLabel("Import all exercises from course");
		title.setStylePrimaryName("cc-pageTitle");
		panel.add(title);
		panel.setWidgetLeftRight(title, BORDER_PX, Unit.PX, BORDER_PX, Unit.PX);
		panel.setWidgetTopHeight(title, BORDER_PX, Unit.PX, TITLE_LABEL_HEIGHT_PX, Unit.PX);
		
		double infoLabelTop = BORDER_PX + TITLE_LABEL_HEIGHT_PX + 10.0;
		
		InlineLabel info = new InlineLabel("Select a course to import exercises from:");
		panel.add(info);
		panel.setWidgetLeftRight(info, BORDER_PX, Unit.PX, BORDER_PX, Unit.PX);
		panel.setWidgetTopHeight(info, infoLabelTop, Unit.PX, INFO_LABEL_HEIGHT_PX, Unit.PX);
		
		double selectionViewTop = infoLabelTop + INFO_LABEL_HEIGHT_PX + 10.0;

		selectionView = new ImportCourseSelectionView(session);
		panel.add(selectionView);
		panel.setWidgetLeftRight(selectionView, BORDER_PX, Unit.PX, BORDER_PX, Unit.PX);
		panel.setWidgetTopHeight(selectionView, selectionViewTop, Unit.PX, ImportCourseSelectionView.HEIGHT_PX, Unit.PX);
		
		double errorLabelTop = selectionViewTop + ImportCourseSelectionView.HEIGHT_PX + 10.0;
		
		errorLabel = new InlineLabel("");
		errorLabel.setStyleDependentName("cc-errorText", true);
		panel.add(errorLabel);
		panel.setWidgetLeftRight(errorLabel, BORDER_PX, Unit.PX, BORDER_PX, Unit.PX);
		panel.setWidgetTopHeight(errorLabel, errorLabelTop, Unit.PX, ERROR_LABEL_HEIGHT_PX, Unit.PX);
		
		double buttonTop = errorLabelTop + ERROR_LABEL_HEIGHT_PX + 10.0;

		importButton = new Button("Import problems");
		panel.add(importButton);
		panel.setWidgetLeftWidth(importButton, BORDER_PX * 2, Unit.PX, BUTTON_WIDTH_PX, Unit.PX);
		panel.setWidgetTopHeight(importButton, buttonTop, Unit.PX, BUTTON_HEIGHT_PX, Unit.PX);
		importButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleImport();
			}
		});
		
		cancelButton = new Button("Cancel");
		panel.add(cancelButton);
		panel.setWidgetLeftWidth(cancelButton, BORDER_PX * 2 + BUTTON_WIDTH_PX + 10.0, Unit.PX, BUTTON_WIDTH_PX, Unit.PX);
		panel.setWidgetTopHeight(cancelButton, buttonTop, Unit.PX, BUTTON_HEIGHT_PX, Unit.PX);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		setGlassEnabled(true);
		
		setWidget(panel);
	}
	
	/**
	 * Set the callback to invoke when a {@link CourseAndCourseRegistration} is selected.
	 * 
	 * @param selectCourseCallback the selectCourseCallback to set
	 */
	public void setSelectCourseCallback(
			ICallback<CourseAndCourseRegistration> selectCourseCallback) {
		this.selectCourseCallback = selectCourseCallback;
	}

	protected void handleImport() {
		CourseAndCourseRegistration selectedCourse = selectionView.getSelected();
		if (selectedCourse == null) {
			errorLabel.setText("Please select a course");
		}
		
		if (selectCourseCallback != null) {
			selectCourseCallback.call(selectedCourse);
		}
		
		hide();
	}
}
