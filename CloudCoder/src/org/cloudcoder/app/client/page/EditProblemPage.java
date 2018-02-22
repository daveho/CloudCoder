// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012,2018 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ChoiceDialogBox;
import org.cloudcoder.app.client.view.EditBooleanField;
import org.cloudcoder.app.client.view.EditDateField;
import org.cloudcoder.app.client.view.EditDateTimeField;
import org.cloudcoder.app.client.view.EditEnumField;
import org.cloudcoder.app.client.view.EditModelObjectField;
import org.cloudcoder.app.client.view.EditStringField;
import org.cloudcoder.app.client.view.EditStringFieldWithAceEditor;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestCaseEditor;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.EditProblemAdapter;
import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.ProblemData;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
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
	
	private enum Confirm {
		OK, CANCEL,
	}
	
	private class UI extends ResizeComposite implements SessionObserver {
		private static final double CENTER_PANEL_V_SEP_PX = 10.0;
		private static final double SAVE_BUTTON_HEIGHT_PX = 32.0;
		private static final double SAVE_BUTTON_WIDTH_PX = 160.0;

		private DockLayoutPanel dockLayoutPanel;
		private Label pageLabel;
		private PageNavPanel pageNavPanel;
		private StatusMessageView statusMessageView;
		private FlowPanel centerPanel;
		private List<EditModelObjectField<IProblem, ?>> problemFieldEditorList;
		private List<TestCaseEditor> testCaseEditorList;
		private Button addTestCaseButton;
		private FlowPanel addTestCaseButtonPanel;
		private ProblemAndTestCaseList problemAndTestCaseListOrig;
		private EditStringFieldWithAceEditor<IProblem> equalityPredicateEditor;
		
		public UI() {
			this.dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// At top of page, show name of course, a PageNavPanel,
			// and a button for saving the edited problem/testcases.
			LayoutPanel northPanel = new LayoutPanel();
			this.pageLabel = new Label("");
			pageLabel.setStyleName("cc-courseLabel");
			northPanel.add(pageLabel);
			northPanel.setWidgetLeftRight(pageLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopBottom(pageLabel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopBottom(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			Button saveButton = new Button("Save problem!");
			saveButton.setStyleName("cc-emphButton");
			saveButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleSaveProblem(new Runnable() { public void run() {} });
				}
			});
			northPanel.add(saveButton);
			northPanel.setWidgetLeftWidth(saveButton, 0.0, Unit.PX, SAVE_BUTTON_WIDTH_PX, Unit.PX);
			northPanel.setWidgetBottomHeight(saveButton, CENTER_PANEL_V_SEP_PX, Unit.PX, SAVE_BUTTON_HEIGHT_PX, Unit.PX);
			
			Button saveAndTestButton = new Button("Save and test");
			saveAndTestButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleSaveProblem(new Runnable() {
						@Override
						public void run() {
							// After saving the problem, go to DevelopmentPage
							getSession().get(PageStack.class).push(PageId.DEVELOPMENT);
						}
					});
				}
			});
			northPanel.add(saveAndTestButton);
			northPanel.setWidgetLeftWidth(saveAndTestButton, SAVE_BUTTON_WIDTH_PX + 8.0, Unit.PX, SAVE_BUTTON_WIDTH_PX, Unit.PX);
			northPanel.setWidgetBottomHeight(saveAndTestButton, CENTER_PANEL_V_SEP_PX, Unit.PX, SAVE_BUTTON_HEIGHT_PX, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + SAVE_BUTTON_HEIGHT_PX + CENTER_PANEL_V_SEP_PX);
			
			// At bottom of page, show a StatusMessageView.
			// Put it in a LayoutPanel so we can add a bit of vertical space to
			// separate it from the center panel.
			this.statusMessageView = new StatusMessageView();
			LayoutPanel southPanel = new LayoutPanel();
			southPanel.add(statusMessageView);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			southPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			dockLayoutPanel.addSouth(southPanel, StatusMessageView.HEIGHT_PX + CENTER_PANEL_V_SEP_PX);
			
			// Create UI for editing problem and test cases
			problemFieldEditorList = new ArrayList<EditModelObjectField<IProblem, ?>>();
			createProblemFieldEditors();

			this.centerPanel = new FlowPanel();
			
			// Add editor widgets for Problem fields
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				centerPanel.add(editor.getUI());
			}
			
			ScrollPanel scrollPanel = new ScrollPanel(centerPanel);
			scrollPanel.setStyleName("cc-editProblemPanel", true);
			dockLayoutPanel.add(scrollPanel);
			
			initWidget(dockLayoutPanel);
		}

		protected void handleSaveProblem(Runnable afterSave) {
			// Commit the contents of all editors
			if (!commitAll()) {
				getSession().add(StatusMessage.error("One or more field values is invalid"));
				return;
			}
			
			// Create a pending operation message
			getSession().add(StatusMessage.pending("Sending problem data to server..."));
			
			// Attempt to store the problem and its test cases in the database
			final ProblemAndTestCaseList problemAndTestCaseList = getSession().get(ProblemAndTestCaseList.class);
			final Course course = getCurrentCourse();

			// imported exercises which are modified become imported_and_modified
			// original exercises that are edited, and imported and modified exercises stay in the same state
			if (problemAndTestCaseList.getProblem().getProblemAuthorship()==ProblemAuthorship.IMPORTED) {
                problemAndTestCaseList.getProblem().setProblemAuthorship(ProblemAuthorship.IMPORTED_AND_MODIFIED);
			}
			// Edited problems are no longer shared
			problemAndTestCaseList.getProblem().setShared(false);
			
			saveProblem(problemAndTestCaseList, course, afterSave);
		}

		protected void saveProblem(
				final ProblemAndTestCaseList problemAndTestCaseList,
				final Course course, final Runnable afterSave) {
			RPC.getCoursesAndProblemsService.storeProblemAndTestCaseList(problemAndTestCaseList, course, new AsyncCallback<ProblemAndTestCaseList>() {
				@Override
				public void onSuccess(ProblemAndTestCaseList result) {
					getSession().add(StatusMessage.goodNews("Problem saved successfully"));
					
					// Make the returned ProblemAndTestCaseList current
					problemAndTestCaseListOrig.copyFrom(result);
					problemAndTestCaseList.copyFrom(result);
					
					// The TestCaseEditors must be updated, because the TestCase objects
					// they are editing have changed.
					int count = 0;
					TestCase[] currentTestCases = problemAndTestCaseList.getTestCaseList();
					for (TestCaseEditor editor : testCaseEditorList) {
						editor.setTestCase(currentTestCases[count++]);
					}
					
					// Make the Problem in the session the one in the
					// ProblemAndTestCaseList.  This is important in case the
					// afterSave callback is going to switch to the DevelopmentPage
					// (i.e., if the "Save and test" button was clicked),
					// and we want to make sure the problem in the session is the
					// one we're editing.
					getSession().add(problemAndTestCaseList.getProblem());
					
					// Call the afterSave callback
					afterSave.run();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								saveProblem(problemAndTestCaseList, course, afterSave);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not save problem: " + caught.getMessage()));
					}
				}
			});
		}

		private void createProblemFieldEditors() {
			problemFieldEditorList.add(new EditEnumField<IProblem, ProblemType>("Problem type", ProblemType.class, ProblemData.PROBLEM_TYPE));
			problemFieldEditorList.add(new EditStringField<IProblem>("Problem name", ProblemData.TESTNAME));
			problemFieldEditorList.add(new EditStringField<IProblem>("Brief description", ProblemData.BRIEF_DESCRIPTION));
			
			EditStringFieldWithAceEditor<IProblem> descriptionEditor =
					new EditStringFieldWithAceEditor<IProblem>("Full description (HTML)", ProblemData.DESCRIPTION);
			descriptionEditor.setEditorMode(AceEditorMode.HTML);
			descriptionEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			problemFieldEditorList.add(descriptionEditor);
			
			// In the editor for the skeleton, we keep the editor mode in sync
			// with the problem type.  (I.e., for a Java problem we want Java
			// mode, for Python we want Python mode, etc.)
			problemFieldEditorList.add(createProblemSourceEditor("Skeleton code", ProblemData.SKELETON));
			
			// We don't need an editor for schema version - problems/testcases are
			// automatically converted to the latest version when they are imported.
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Author name", ProblemData.AUTHOR_NAME));
			problemFieldEditorList.add(new EditStringField<IProblem>("Author email", ProblemData.AUTHOR_EMAIL));
			problemFieldEditorList.add(new EditStringField<IProblem>("Author website", ProblemData.AUTHOR_WEBSITE));
			problemFieldEditorList.add(new EditDateField<IProblem>("Creation date", ProblemData.TIMESTAMP_UTC));
			problemFieldEditorList.add(new EditEnumField<IProblem, ProblemLicense>("License", ProblemLicense.class, ProblemData.LICENSE));
			problemFieldEditorList.add(new EditStringField<IProblem>("URL of required external library", ProblemData.EXTERNAL_LIBRARY_URL));
			problemFieldEditorList.add(new EditStringField<IProblem>("MD5 checksum of required external library", ProblemData.EXTERNAL_LIBRARY_MD5));
			
			// Also keep equality predicate editor language in sync with Problem
			this.equalityPredicateEditor = createProblemSourceEditor("Equality predicate", ProblemData.EQUALITY_PREDICATE);
			problemFieldEditorList.add(equalityPredicateEditor);
			
			problemFieldEditorList.add(new EditDateTimeField<IProblem>("When assigned", Problem.WHEN_ASSIGNED));
			problemFieldEditorList.add(new EditDateTimeField<IProblem>("When due", Problem.WHEN_DUE));
			problemFieldEditorList.add(new EditBooleanField<IProblem>(
					"Problem visible to students",
					"Check to make problem visible to students",
					Problem.VISIBLE));
		}
		
		// Create a field editor which automatically synchronizes its source
		// language with the Problem's language.
		private EditStringFieldWithAceEditor<IProblem> createProblemSourceEditor(String desc, ModelObjectField<? super IProblem, String> field) {
			EditStringFieldWithAceEditor<IProblem> editor;
			editor = new EditStringFieldWithAceEditor<IProblem>(desc, field) {
				@Override
				public void update() {
					setLanguage();
					super.update();
				}
				@Override
				public void onModelObjectChange() {
					setLanguage();
				}
				private void setLanguage() {
					AceEditorMode editorMode = ViewUtil.getModeForLanguage(getModelObject().getProblemType().getLanguage());
					setEditorMode(editorMode);
				}
			};
			editor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			return editor;
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// The session should contain a ProblemAndTestCaseList.
			ProblemAndTestCaseList problemAndTestCaseList = session.get(ProblemAndTestCaseList.class);

			// Make a copy of the ProblemAndTestCaseList being edited.
			// This will allow us to detect whether or not it has been changed
			// by the user.
			this.problemAndTestCaseListOrig = new ProblemAndTestCaseList();
			problemAndTestCaseListOrig.copyFrom(problemAndTestCaseList);
			
			// Activate views
			final Course course = getCurrentCourse();
			pageLabel.setText(
					(problemAndTestCaseList.getProblem().getProblemId()== null ? "Create new" : "Edit") +
					" problem in " + course.toString());
			
			// The nested Runnable objects here are due to the strange way DialogBoxes
			// work in GWT - show() and center() return immediately rather than waiting
			// for the dialog to be dismissed.  Thus, it is necessary to use a callback
			// to capture a choice made in a dialog box.  Bleh.
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					leavePage(new Runnable() {
						public void run() {
							// Purge the list of ProblemAndSubmissionReceipts, in case a
							// problem was edited by this page.  That will force CourseAdminPage to
							// reload the problem list for the Course.
							getSession().remove(ProblemAndSubmissionReceipt[].class);
							
							// Go back to previous page.
							session.get(PageStack.class).pop();
						}
					});
				}
			});
			pageNavPanel.setLogoutHandler(new Runnable() {
				@Override
				public void run() {
					leavePage(new Runnable(){
						@Override
						public void run() {
							new LogoutHandler(session).run();
						}
					});
				}
			});
			statusMessageView.activate(session, subscriptionRegistrar);
			
			// Create a ProblemAdapter to serve as the IProblem edited by the problem editors.
			// Override the onChange() method to notify editors that the model object has changed
			// in some way.  Also handle the case where the problem type has been changed and
			// the equality predicate editor should be hidden or shown, depending on whether
			// or not the new problem type supports an equality predicate.
			EditProblemAdapter problemAdapter = new EditProblemAdapter(problemAndTestCaseList.getProblem()) {
				@Override
				protected void onChange() {
					for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
						editor.onModelObjectChange();
					}
					checkProblemTypeChange(this);
				}
			};
			
			// Set the Problem in all problem field editors.
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				editor.setModelObject(problemAdapter);
			}
			
			// Add TestCaseEditors for test cases.
			testCaseEditorList = new ArrayList<TestCaseEditor>();
			for (TestCase testCase : problemAndTestCaseList.getTestCaseList()) {
				GWT.log("Making TestCaseEditor for test case " + testCase.getTestCaseName());
				final TestCaseEditor testCaseEditor = new TestCaseEditor();
				// Add the TestCase editor to the DOM early,
				// since associating it with a TestCase may result in
				// access to UI elements
				centerPanel.add(testCaseEditor.getUI());
				GWT.log("TestCase editor added to panel");
				testCaseEditor.setDeleteHandler(new Runnable() {
					@Override
					public void run() {
						handleDeleteTestCase(testCaseEditor);
					}
				});
				testCaseEditorList.add(testCaseEditor);
				GWT.log("About to set the TestCase...");
				testCaseEditor.setTestCase(testCase);
				GWT.log("TestCase set");
			}
			
			// Add a button to create a new TestCase and TestCaseEditor.
			// Put it in a FlowPanel to ensure that it's in its own div.
			// (Could also use this to style/position the button.)
			this.addTestCaseButtonPanel = new FlowPanel();
			addTestCaseButton = new Button("Add Test Case");
			addTestCaseButton.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					handleAddTestCase();
				}
			});
			addTestCaseButtonPanel.add(addTestCaseButton);
			centerPanel.add(addTestCaseButtonPanel);
			
			// Based on whatever the initial (default) problem type is,
			// either show or hide the equality predicate editor.
			checkProblemTypeChange(problemAdapter);
		}
		
		private ProblemType lastProblemType = null;
		
		/**
		 * Called when the problem data changes in order to check
		 * whether the problem type has changed so that we can selectively
		 * show/hide the equality predicate editor.
		 * 
		 * @param problemAdapter the {@link EditProblemAdapter} for the problem
		 */
		protected void checkProblemTypeChange(EditProblemAdapter problemAdapter) {
			if (lastProblemType == null || lastProblemType != problemAdapter.getProblemType()) {
				// Problem type has changed
				if (problemAdapter.getProblemType().supportsEqualityPredicate()) {
					logPrint("Enabling equality predicate, is currently " + problemAdapter.getEqualityPredicate());
					equalityPredicateEditor.getUI().asWidget().setVisible(true);
				} else {
					equalityPredicateEditor.getUI().asWidget().setVisible(false);
					
					// Since we're switching to a problem type that doesn't
					// support an equality predicate, clear the equality predicate
					// field.  This has the downside that switching from
					// a problem type with an equality predicate to one without
					// abandons the current equality predicate, but I don't
					// think that's really a big deal.  Generally the user
					// will (hopefully?) select the problem type first and then
					// make edits to it.
					logPrint("Clearing equality predicate");
					problemAdapter.getDelegate().setEqualityPredicate("");
					logPrint("Equality predicate is now " + problemAdapter.getEqualityPredicate());
					equalityPredicateEditor.update();
				}
			}
			lastProblemType = problemAdapter.getProblemType();
		}

		private void leavePage(final Runnable action) {
			// Commit all changes made in the editors to the model objects.
			boolean successfulCommit = commitAll();
			boolean problemModified = isProblemModified();
			
			// If the Problem has not been modified, then it's fine to leave the page
			// without a prompt.
			if (successfulCommit && !problemModified) {
				action.run();
				return;
			}
			
			if (!successfulCommit) {
				getSession().add(StatusMessage.error("One or more values is invalid"));
			}
			GWT.log("Problem " + (problemModified ? "has" : "has not") + " been modified");
			
			// Prompt user to confirm leaving page (and abandoning changes to Problem)
			ChoiceDialogBox<Confirm> confirmDialog = new ChoiceDialogBox<Confirm>(
					"Save changes to problem?",
					"The problem has been modified: are you sure you want to abandon the changes?",
					new ChoiceDialogBox.ChoiceHandler<Confirm>() {
						public void handleChoice(Confirm choice) {
							if (choice == Confirm.OK) {
								action.run();
							}
						}
					});
			confirmDialog.addChoice("Abandon changes", Confirm.OK);
			confirmDialog.addChoice("Don't abandon changes", Confirm.CANCEL);
			confirmDialog.center();
		}
		
		/**
		 * Commit all changes in the UI to the underlying ProblemAndTestCaseList model object.
		 * 
		 * @return true if all committed values were valid, false if at least
		 *         one editor contains an invalid value
		 */
		private boolean commitAll() {
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				editor.commit();
			}
			for (TestCaseEditor editor : testCaseEditorList) {
				editor.commit();
			}
			boolean success = true;
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				if (editor.isCommitError()) {
					success = false;
				}
			}
			return success;
		}
		
		/**
		 * @return true if the ProblemAndTestCaseList has been modified, false otherwise
		 */
		private boolean isProblemModified() {
			return !getSession().get(ProblemAndTestCaseList.class).equals(problemAndTestCaseListOrig);
		}

		/**
		 * Called when the user clicks the "Delete" button in a TestCaseEditor.
		 * Removes the editor from the UI and removes the test case from
		 * the underlying ProblemAndTestCaseList.
		 * 
		 * @param testCaseEditor the TestCaseEditor
		 */
		protected void handleDeleteTestCase(TestCaseEditor testCaseEditor) {
			//getSession().get(ProblemAndTestCaseList.class).removeTestCase(testCaseEditor.getTestCase());
			int index = -1;
			for (int i = 0; i < testCaseEditorList.size(); i++) {
				if (testCaseEditor == testCaseEditorList.get(i)) {
					index = i;
					break;
				}
			}
			
			if (index < 0) {
				GWT.log("Could not find TestCaseEditor?");
				return;
			}
			
			GWT.log("Deleting test case " + index);
			
			getSession().get(ProblemAndTestCaseList.class).removeTestCase(index);
			
			centerPanel.remove(testCaseEditor.getUI());
			testCaseEditorList.remove(testCaseEditor);
		}

		protected void handleAddTestCase() {
			// Add the TestCase to the ProblemAndTestCaseList
			TestCase testCase = TestCase.createEmpty();
			int numTests = testCaseEditorList.size();
			testCase.setTestCaseName("t"+numTests);
			getSession().get(ProblemAndTestCaseList.class).addTestCase(testCase);

			// Add a new TestCase editor and its UI widget
			final TestCaseEditor testCaseEditor = new TestCaseEditor();
			testCaseEditorList.add(testCaseEditor);
			centerPanel.insert(testCaseEditor.getUI(), centerPanel.getWidgetIndex(addTestCaseButtonPanel));
			testCaseEditor.setTestCase(testCase);
			testCaseEditor.setDeleteHandler(new Runnable() {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					handleDeleteTestCase(testCaseEditor);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		setWidget(new UI());
	}
	
	@Override
	public Class<?>[] getRequiredPageObjects() {
		return new Class<?>[]{CourseSelection.class, Problem.class, ProblemAndTestCaseList.class};
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}
	
	@Override
	public PageId getPageId() {
		return PageId.EDIT_PROBLEM;
	}
	
	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
		pageStack.push(PageId.PROBLEM_ADMIN);
	}
}
