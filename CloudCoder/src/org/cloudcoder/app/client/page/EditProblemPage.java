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

package org.cloudcoder.app.client.page;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.EditEnumField;
import org.cloudcoder.app.client.view.EditModelObjectField;
import org.cloudcoder.app.client.view.EditStringField;
import org.cloudcoder.app.client.view.EditStringFieldWithAceEditor;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Page for editing a {@link ProblemAndTestCaseList}.
 * 
 * @author David Hovemeyer
 */
public class EditProblemPage extends CloudCoderPage {
	
	private class UI extends ResizeComposite implements SessionObserver, Subscriber {
		
		private DockLayoutPanel dockLayoutPanel;
		private Label pageLabel;
		private PageNavPanel pageNavPanel;
		private List<EditModelObjectField<Problem, ?>> editProblemFieldList;
		
		public UI() {
			this.dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// At top of page, show name of course and a PageNavPanel
			LayoutPanel northPanel = new LayoutPanel();
			this.pageLabel = new Label("");
			pageLabel.setStyleName("cc-courseLabel");
			northPanel.add(pageLabel);
			northPanel.setWidgetLeftRight(pageLabel, 0.0, Unit.PX, PageNavPanel.WIDTH, PageNavPanel.WIDTH_UNIT);
			northPanel.setWidgetTopBottom(pageLabel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH, Unit.PX);
			northPanel.setWidgetTopBottom(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT);
			
			// Creation of the UI for editing problem/test cases will wait until
			// we know what the Problem is
			
			initWidget(dockLayoutPanel);
		}

		private void createProblemAndTestCaseEditor() {
			// Create a LayoutPanel for the editors for the Problem and its TestCases

			editProblemFieldList = new ArrayList<EditModelObjectField<Problem, ?>>();
			createProblemFieldEditors();
			
			LayoutPanel panel = new LayoutPanel();
			
			// Add editor widgets for Problem fields
			double y = 0.0;
			for (EditModelObjectField<Problem, ?> editor : editProblemFieldList) {
				IsWidget widget = editor.getUI();
				panel.add(widget);
				panel.setWidgetTopHeight(widget, y, Unit.PX, editor.getHeightPx(), Unit.PX);
				panel.setWidgetLeftRight(widget, 0.0, Unit.PX, 0.0, Unit.PX);
				
				y += editor.getHeightPx();
			}
			
			dockLayoutPanel.add(panel);
		}

		private void createProblemFieldEditors() {
			editProblemFieldList.add(new EditEnumField<Problem, ProblemType>("Problem type", ProblemType.class) {
				@Override
				protected void setField(ProblemType value) {
					getModelObject().setProblemType(value);
				}

				@Override
				protected ProblemType getField() {
					return getModelObject().getProblemType();
				}
			});
			editProblemFieldList.add(new EditStringField<Problem>("Problem name") {
				@Override
				protected void setField(String value) {
					getModelObject().setTestName(value);
				}
				
				@Override
				protected String getField() {
					return getModelObject().getTestName();
				}
			});
			editProblemFieldList.add(new EditStringField<Problem>("Brief description") {
				@Override
				protected void setField(String value) {
					getModelObject().setBriefDescription(value);
				}
				
				@Override
				protected String getField() {
					return getModelObject().getBriefDescription();
				}
				
			});
			editProblemFieldList.add(new EditStringFieldWithAceEditor<Problem>("Full description (HTML)", AceEditorMode.HTML, AceEditorTheme.VIBRANT_INK) {
				@Override
				protected void setField(String value) {
					getModelObject().setDescription(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getDescription();
				}
			});
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// Create UI for editing problem and test cases
			createProblemAndTestCaseEditor();
			
			// Activate views
			final Course course = session.get(Course.class);
			pageLabel.setText("Edit problem in " + course.toString());
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					session.notifySubscribers(Session.Event.COURSE_ADMIN, course);
				}
			});
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			
			// The session should contain a ProblemAndTestCaseList.
			ProblemAndTestCaseList problemAndTestCaseList = session.get(ProblemAndTestCaseList.class);
			
			// Set the Problem in all problem field editors.
			for (EditModelObjectField<Problem, ?> editor : editProblemFieldList) {
				editor.setModelObject(problemAndTestCaseList.getProblem());
			}
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
		}
		
	}
	
	private UI ui;

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		ui = new UI();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#deactivate()
	 */
	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#getWidget()
	 */
	@Override
	public IsWidget getWidget() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
	 */
	@Override
	public boolean isActivity() {
		//return true;
		return false;
	}
	
}
