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
import java.util.Date;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.EditEnumField;
import org.cloudcoder.app.client.view.EditModelObjectField;
import org.cloudcoder.app.client.view.EditStringField;
import org.cloudcoder.app.client.view.EditStringFieldWithAceEditor;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Page for editing a {@link ProblemAndTestCaseList}.
 * 
 * @author David Hovemeyer
 */
public class EditProblemPage extends CloudCoderPage {
	
	/**
	 * Adapter class for {@link Problem}.
	 * Implements {@link Publisher} so that editors can learn
	 * about state changes made by other editors.
	 */
	private static abstract class ProblemAdapter implements IProblem {
		private final IProblem delegate;
		
		/**
		 * Constructor.
		 * 
		 * @param delegate an IProblem object to which getter/setter calls will be delegated
		 */
		public ProblemAdapter(IProblem delegate) {
			this.delegate = delegate;
		}
		
		/**
		 * Downcall method: is called whenever a setter method has been called
		 * and the IProblem object to which the adapter is delegating
		 * might have changed.
		 */
		protected abstract void onChange();

		@Override
		public void setProblemType(ProblemType problemType) {
			delegate.setProblemType(problemType);
			onChange();
		}

		@Override
		public void setProblemType(int problemType) {
			delegate.setProblemType(problemType);
			onChange();
		}

		@Override
		public ProblemType getProblemType() {
			return delegate.getProblemType();
		}

		@Override
		public String getTestName() {
			return delegate.getTestName();
		}

		@Override
		public void setTestName(String testName) {
			delegate.setTestName(testName);
			onChange();
		}

		@Override
		public void setBriefDescription(String briefDescription) {
			delegate.setBriefDescription(briefDescription);
			onChange();
		}

		@Override
		public String getBriefDescription() {
			return delegate.getBriefDescription();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

		@Override
		public void setDescription(String description) {
			delegate.setDescription(description);
			onChange();
		}

		@Override
		public void setSkeleton(String skeleton) {
			delegate.setSkeleton(skeleton);
			onChange();
		}

		@Override
		public String getSkeleton() {
			return delegate.getSkeleton();
		}

		@Override
		public void setSchemaVersion(int schemaVersion) {
			delegate.setSchemaVersion(schemaVersion);
			onChange();
		}

		@Override
		public int getSchemaVersion() {
			return delegate.getSchemaVersion();
		}

		@Override
		public void setAuthorName(String authorName) {
			delegate.setAuthorName(authorName);
			onChange();
		}

		@Override
		public String getAuthorName() {
			return delegate.getAuthorName();
		}

		@Override
		public void setAuthorEmail(String authorEmail) {
			delegate.setAuthorEmail(authorEmail);
			onChange();
		}

		@Override
		public String getAuthorEmail() {
			return delegate.getAuthorEmail();
		}

		@Override
		public void setAuthorWebsite(String authorWebsite) {
			delegate.setAuthorWebsite(authorWebsite);
			onChange();
		}

		@Override
		public String getAuthorWebsite() {
			return delegate.getAuthorWebsite();
		}

		@Override
		public void setTimestampUTC(long timestampUTC) {
			delegate.setTimestampUTC(timestampUTC);
			onChange();
		}

		@Override
		public long getTimestampUTC() {
			return delegate.getTimestampUTC();
		}

		@Override
		public void setLicense(ProblemLicense license) {
			delegate.setLicense(license);
			onChange();
		}

		@Override
		public ProblemLicense getLicense() {
			return delegate.getLicense();
		}

		@Override
		public Integer getProblemId() {
			return delegate.getProblemId();
		}

		@Override
		public void setProblemId(Integer id) {
			delegate.setProblemId(id);
			onChange();
		}

		@Override
		public Integer getCourseId() {
			return delegate.getCourseId();
		}

		@Override
		public void setCourseId(Integer courseId) {
			delegate.setCourseId(courseId);
			onChange();
		}

		@Override
		public long getWhenAssigned() {
			return delegate.getWhenAssigned();
		}

		@Override
		public Date getWhenAssignedAsDate() {
			return delegate.getWhenAssignedAsDate();
		}

		@Override
		public void setWhenAssigned(long whenAssigned) {
			delegate.setWhenAssigned(whenAssigned);
			onChange();
		}

		@Override
		public long getWhenDue() {
			return delegate.getWhenDue();
		}

		@Override
		public Date getWhenDueAsDate() {
			return delegate.getWhenDueAsDate();
		}

		@Override
		public void setWhenDue(long whenDue) {
			delegate.setWhenDue(whenDue);
			onChange();
		}

		@Override
		public void setVisible(boolean visible) {
			delegate.setVisible(visible);
			onChange();
		}

		@Override
		public boolean isVisible() {
			return delegate.isVisible();
		}
		
	}
	
	private class UI extends ResizeComposite implements SessionObserver, Subscriber {
		
		private DockLayoutPanel dockLayoutPanel;
		private Label pageLabel;
		private PageNavPanel pageNavPanel;
		private List<EditModelObjectField<IProblem, ?>> editProblemFieldList;
		
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
			
			// Create UI for editing problem and test cases
			editProblemFieldList = new ArrayList<EditModelObjectField<IProblem, ?>>();
			createProblemFieldEditors();

			FlowPanel panel = new FlowPanel();
			
			// Add editor widgets for Problem fields
			for (EditModelObjectField<IProblem, ?> editor : editProblemFieldList) {
				IsWidget widget = editor.getUI();
				panel.add(widget);
			}
			
			dockLayoutPanel.add(new ScrollPanel(panel));
			
			initWidget(dockLayoutPanel);
		}

		private void createProblemFieldEditors() {
			editProblemFieldList.add(new EditEnumField<IProblem, ProblemType>("Problem type", ProblemType.class) {
				@Override
				protected void setField(ProblemType value) {
					getModelObject().setProblemType(value);
				}

				@Override
				protected ProblemType getField() {
					return getModelObject().getProblemType();
				}
			});
			
			editProblemFieldList.add(new EditStringField<IProblem>("Problem name") {
				@Override
				protected void setField(String value) {
					getModelObject().setTestName(value);
				}
				
				@Override
				protected String getField() {
					return getModelObject().getTestName();
				}
			});
			
			editProblemFieldList.add(new EditStringField<IProblem>("Brief description") {
				@Override
				protected void setField(String value) {
					getModelObject().setBriefDescription(value);
				}
				
				@Override
				protected String getField() {
					return getModelObject().getBriefDescription();
				}
				
			});
			
			EditStringFieldWithAceEditor<IProblem> descriptionEditor =
					new EditStringFieldWithAceEditor<IProblem>("Full description (HTML)") {
						@Override
						protected void setField(String value) {
							getModelObject().setDescription(value);
						}
						@Override
						protected String getField() {
							return getModelObject().getDescription();
						}
					};
			descriptionEditor.setEditorMode(AceEditorMode.HTML);
			descriptionEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			editProblemFieldList.add(descriptionEditor);
			
			EditStringFieldWithAceEditor<IProblem> skeletonEditor =
					new EditStringFieldWithAceEditor<IProblem>("Skeleton code") {
						@Override
						public void update() {
							// Set the editor mode to match the ProblemType
							AceEditorMode editorMode = ViewUtil.getModeForLanguage(getModelObject().getProblemType().getLanguage());
							setEditorMode(editorMode);
							super.update();
						}
						@Override
						protected void setField(String value) {
							getModelObject().setSkeleton(value);
						}
						@Override
						protected String getField() {
							return getModelObject().getSkeleton();
						}
					};
			skeletonEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			editProblemFieldList.add(skeletonEditor);
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
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
			
			// Create a ProblemAdapter to serve as the IProblem edited by the problem editors.
			// Override the onChange() method to update all editors whenever the state of
			// of the underlying Problem changes.
			ProblemAdapter problemAdapter = new ProblemAdapter(problemAndTestCaseList.getProblem()) {
				@Override
				protected void onChange() {
					for (EditModelObjectField<IProblem, ?> editor : editProblemFieldList) {
						editor.update();
					}
				}
			};
			
			// Set the Problem in all problem field editors.
			for (EditModelObjectField<IProblem, ?> editor : editProblemFieldList) {
				editor.setModelObject(problemAdapter);
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
