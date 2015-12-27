// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.validator.NoopFieldValidator;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SubmitButton;

/**
 * New bulk student registration UI for the "Manage course" tab.
 * 
 * @author David Hovemeyer
 */
public class BulkRegistrationPanel extends ValidatedFormUI
		implements CourseInstructorUI, SessionObserver {
	private LayoutPanel layoutPanel;
	private FileUpload fileUpload;
	private Hidden courseId;
	private SubmitButton submitButton;

	/**
	 * Constructor.
	 */
	public BulkRegistrationPanel(final CloudCoderPage page) {
		super(new FormPanel());
		
		FormPanel formPanel = (FormPanel) getPanel();
		formPanel.setWidth("100%");
		formPanel.setHeight("144px");
		
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.setAction(GWT.getModuleBaseURL()+"registerStudents");
		
		this.layoutPanel = new LayoutPanel();
		formPanel.add(layoutPanel);
		
		double y = 10.0;
		
		// Add widgets
		InlineHTML fileFormatMsg = new InlineHTML("File should be tab-delimited in format:<br>" +
				"<tt>username firstname lastname email password</tt>");
		y = addWidget(y, fileFormatMsg, "", new NoopFieldValidator(), 36.0);
		
		this.fileUpload = new FileUpload();
		fileUpload.setName("fileupload");
		y = addWidget(y, fileUpload, "Filename:", new NoopFieldValidator());
		
		this.submitButton = new SubmitButton("Register students");
		y = addWidget(y, submitButton, "", new NoopFieldValidator());
		
		this.courseId = new Hidden();
		courseId.setName("courseId");
		layoutPanel.add(courseId);

		formPanel.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				page.getSession().add(StatusMessage.pending("Uploading student data..."));
			}
		});
		formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String results = event.getResults();
				if (results == null) {
					page.getSession().add(StatusMessage.error("Error communicating with server"));
				} else {
					if (results.startsWith("Error: ")) {
						results = results.substring("Error: ".length());
						page.getSession().add(StatusMessage.error(results));
					} else {
						page.getSession().add(StatusMessage.goodNews(results));
						clear();
					}
				}
			}
		});
	}

	@Override
	public LayoutPanel getLayoutPanel() {
		return this.layoutPanel;
	}
	
	@Override
	public void clear() {
		InputElement inputElt = fileUpload.getElement().cast();
		inputElt.setValue("");
	}
	
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		// Keep track of changes to instructor status
		new CourseInstructorStatusMonitor(this).activate(session, subscriptionRegistrar);
	}

	@Override
	public void setEnabled(boolean b) {
		fileUpload.setEnabled(b);
		submitButton.setEnabled(b);
	}
	
	@Override
	public void onCourseChange(Course course) {
		GWT.log("BulkRegistrationPanel: selected courseId=" + course.getId());
		courseId.setValue(String.valueOf(course.getId()));
	}
}
