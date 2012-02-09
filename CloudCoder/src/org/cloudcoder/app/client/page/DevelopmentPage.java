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

import org.cloudcoder.app.client.model.ChangeFromAceOnChangeEvent;
import org.cloudcoder.app.client.model.ChangeList;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.CompilerDiagnosticListView;
import org.cloudcoder.app.client.view.DevActionsPanel;
import org.cloudcoder.app.client.view.IResultsTabPanelWidget;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestResultListView;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Page for development (editing code, submitting, viewing results.)
 * 
 * @author David Hovemeyer
 */
public class DevelopmentPage extends CloudCoderPage {
	
	private enum Mode {
		/** Loading problem and current text - editing not allowed. */
		LOADING,
		
		/** Normal state - user is allowed to edit the program text. */
		EDITING,
		
		/**
		 * A submit has been requested, but it must wait until
		 * the change list is clean.
		 * Editing disallowed until server response is received.
		 */
		SUBMIT_PENDING_CLEAN_CHANGE_LIST,
		
		/**
		 * A submit has been initiated.
		 * Editing disallowed until server response is received.
		 */
		SUBMIT_IN_PROGRESS,
		
		/**
		 * Logging out.
		 */
		LOGOUT,
	}

	/**
	 * UI class for DevelopmentPage.
	 */
	private class UI extends Composite implements Subscriber {
		public static final double NORTH_PANEL_HEIGHT_PX = ProblemDescriptionView.HEIGHT_PX;
		public static final double SOUTH_PANEL_HEIGHT_PX = 200.0;
		public static final double BUTTONS_PANEL_WIDTH_PX = 200.0;

		public static final int FLUSH_CHANGES_INTERVAL_MS = 2000;

		private LayoutPanel northLayoutPanel;
		private ProblemDescriptionView problemDescriptionView;
		private PageNavPanel pageNavPanel;
		private DevActionsPanel devActionsPanel;
		private LayoutPanel southLayoutPanel;
		private LayoutPanel centerLayoutPanel;
		private LayoutPanel buttonsLayoutPanel;
		private StatusMessageView statusMessageView;
		private TabLayoutPanel resultsTabPanel;
		private TestResultListView testResultListView;
		private CompilerDiagnosticListView compilerDiagnosticListView;
		private List<IResultsTabPanelWidget> resultsTabPanelWidgetList;

		private AceEditor aceEditor;
		private Timer flushPendingChangeEventsTimer;
		private Mode mode;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);

			northLayoutPanel = new LayoutPanel();
			dockLayoutPanel.addNorth(northLayoutPanel, NORTH_PANEL_HEIGHT_PX);
			problemDescriptionView = new ProblemDescriptionView();
			northLayoutPanel.add(problemDescriptionView);
			northLayoutPanel.setWidgetLeftRight(problemDescriptionView, 0.0, Unit.PX, BUTTONS_PANEL_WIDTH_PX, Unit.PX);
			northLayoutPanel.setWidgetTopBottom(problemDescriptionView, 0.0, Unit.PX, 10.0, Unit.PX);
			buttonsLayoutPanel = new LayoutPanel();
			pageNavPanel = new PageNavPanel();
			buttonsLayoutPanel.add(pageNavPanel);
			buttonsLayoutPanel.setWidgetLeftRight(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			buttonsLayoutPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT);
			devActionsPanel = new DevActionsPanel();
			buttonsLayoutPanel.add(devActionsPanel);
			buttonsLayoutPanel.setWidgetLeftRight(devActionsPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			buttonsLayoutPanel.setWidgetTopBottom(devActionsPanel, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT, 0.0, Unit.PX);
			
			northLayoutPanel.add(buttonsLayoutPanel);
			northLayoutPanel.setWidgetRightWidth(buttonsLayoutPanel, 0.0, Unit.PX, BUTTONS_PANEL_WIDTH_PX, Unit.PX);
			northLayoutPanel.setWidgetTopHeight(buttonsLayoutPanel, 0.0, Unit.PX, NORTH_PANEL_HEIGHT_PX, Unit.PX);

			southLayoutPanel = new LayoutPanel();
			dockLayoutPanel.addSouth(southLayoutPanel, SOUTH_PANEL_HEIGHT_PX);
			
			this.statusMessageView = new StatusMessageView();
			southLayoutPanel.add(statusMessageView);
			southLayoutPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);

			this.resultsTabPanel = new TabLayoutPanel(24, Unit.PX);
			southLayoutPanel.add(resultsTabPanel);
			southLayoutPanel.setWidgetTopBottom(resultsTabPanel, StatusMessageView.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			southLayoutPanel.setWidgetLeftRight(resultsTabPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			this.resultsTabPanelWidgetList = new ArrayList<IResultsTabPanelWidget>();
			
			this.testResultListView = new TestResultListView();
			addResultsTab(this.testResultListView, "Test results");
			
			this.compilerDiagnosticListView = new CompilerDiagnosticListView();
			addResultsTab(this.compilerDiagnosticListView, "Compiler errors");

			// Workaround for http://code.google.com/p/google-web-toolkit/issues/detail?id=7065
			resultsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
				@Override
				public void onSelection(SelectionEvent<Integer> event) {
					int tabIndex = event.getSelectedItem();
					resultsTabPanelWidgetList.get(tabIndex).setSelected();
				}
			});
			
			centerLayoutPanel = new LayoutPanel();
			dockLayoutPanel.add(centerLayoutPanel);

			initWidget(dockLayoutPanel);
		}

		private void addResultsTab(IResultsTabPanelWidget w, String title) {
			resultsTabPanel.add(w, title);
			resultsTabPanelWidgetList.add(w); // keep track of all results tab panel widgets
		}

		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			final Problem problem = session.get(Problem.class);

			mode = Mode.LOADING;
			
			// Activate views
			problemDescriptionView.activate(session, subscriptionRegistrar);
			testResultListView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);
			compilerDiagnosticListView.activate(session, subscriptionRegistrar);
			
			// Subscribe to ChangeList events
			session.get(ChangeList.class).subscribe(ChangeList.State.CLEAN, this, subscriptionRegistrar);

			// Create AceEditor instance
			createEditor(problem.getProblemType().getLanguage());

			// editor will be readonly until problem text is loaded
			aceEditor.setReadOnly(true);

			// add a handler for editor change events
			addEditorChangeEventHandler(session, problem);
			
			// Add logout and back handlers
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			pageNavPanel.setBackHandler(new BackHomeHandler(session));
			
			// Add submit handler
			devActionsPanel.setSubmitHandler(new Runnable() {
				@Override
				public void run() {
					ChangeList changeList = session.get(ChangeList.class);
					
					 if (changeList.getState() == ChangeList.State.CLEAN) {
						mode = Mode.SUBMIT_IN_PROGRESS;
						doSubmit();
					 } else {
						 // As soon as the change list is clean, we'll be able
						 // to submit
						 addSessionObject(new StatusMessage(StatusMessage.Category.PENDING, "Saving your code..."));
						 mode = Mode.SUBMIT_PENDING_CLEAN_CHANGE_LIST;
					 }
					 
					// No editing is allowed until a response is received from the server
					aceEditor.setReadOnly(true);
				}
			});
			
			// Tell the server which problem we want to work on
			setProblem(session, problem);
		}
		
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == ChangeList.State.CLEAN && mode == Mode.SUBMIT_PENDING_CLEAN_CHANGE_LIST) {
				// The change list just became clean, and an attempt to
				// submit is pending. Initiate the submit.
				mode = Mode.SUBMIT_IN_PROGRESS;
				doSubmit();
			}
		}

		public void doSubmit() {
			// Full text of submission has arrived at server,
			// and because the editor is read-only, we know that the
			// local text is in-sync.  So, submit the code!
			
			addSessionObject(new StatusMessage(StatusMessage.Category.PENDING, "Testing your code, please wait..."));
			
			Problem problem = getSession().get(Problem.class);
			String text = aceEditor.getText();

			RPC.submitService.submit(problem.getProblemId(), text, new AsyncCallback<SubmissionResult>() {
				@Override
				public void onFailure(Throwable caught) {
					final String msg = "Error sending submission to server for compilation"; 
					GWT.log(msg, caught);
					addSessionObject(new StatusMessage(StatusMessage.Category.ERROR, msg));
					// TODO: should set editor back to read/write?
				}

				@Override
				public void onSuccess(SubmissionResult result) {
					if (result==null){
						addSessionObject(new StatusMessage(StatusMessage.Category.ERROR, "Results from Builder are empty"));
						addSessionObject(new TestResult[0]);
						addSessionObject(new CompilerDiagnostic[0]);

					} else {
						// Add compiler diagnostics.
						// We will just assume that, in general, there might be some
						// messages from the compiler, even if the code was compiled
						// successfully.
						CompilerDiagnostic[] compilerDiagnosticList = result.getCompilationResult().getCompilerDiagnosticList();
						if (compilerDiagnosticList == null) {
							compilerDiagnosticList = new CompilerDiagnostic[0]; // paranoia
						}
						GWT.log("Adding " + compilerDiagnosticList.length + " compiler diagnostics");
						addSessionObject(compilerDiagnosticList);
						
						// See what the result of the submission was.
						if (result.getCompilationResult().getOutcome()==CompilationOutcome.UNEXPECTED_COMPILER_ERROR ||
								result.getCompilationResult().getOutcome()==CompilationOutcome.BUILDER_ERROR)
						{
							// ?
							addSessionObject(new StatusMessage(StatusMessage.Category.ERROR, "Error testing submission"));
							addSessionObject(new TestResult[0]);
						} else if (result.getCompilationResult().getOutcome()==CompilationOutcome.FAILURE) {
							// Code did not compile
							addSessionObject(new StatusMessage(StatusMessage.Category.ERROR, "Error compiling submission"));
							addSessionObject(new TestResult[0]);
						} else {
							// Code compiled, and test results were sent back.

							TestResult[] results=result.getTestResults();
							// Great, got results back from server!
							addSessionObject(results);

							// Add a status message about the results
							if (result.isAllTestsPassed()) {
								addSessionObject(new StatusMessage(StatusMessage.Category.GOOD_NEWS, "All tests passed! You rock."));
							} else {
								addSessionObject(new StatusMessage(StatusMessage.Category.ERROR, "At least one test failed: check test results"));
							}
						}
						
						if (compilerDiagnosticList.length > 0) {
							// show the compiler diagnostics
							resultsTabPanel.selectTab(compilerDiagnosticListView);
						} else {
							// show the test results tab
							resultsTabPanel.selectTab(testResultListView);
						}
						
						// Can resume editing now
						mode = Mode.EDITING;
						aceEditor.setReadOnly(false);
					}
				}
			});
		}

		public void createEditor(Language language) {
			aceEditor = new AceEditor(true);
			aceEditor.setSize("100%", "100%");
			centerLayoutPanel.add(aceEditor);
			aceEditor.startEditor();
			aceEditor.setFontSize("14px");
			
			// based on programming language used in the Problem,
			// choose an editor mode
			AceEditorMode editorMode;
			switch (language) {
			case JAVA:
				editorMode = AceEditorMode.JAVA; break;
			case PYTHON:
				editorMode = AceEditorMode.PYTHON; break;
			case C:
			case CPLUSPLUS:
				editorMode = AceEditorMode.C_CPP; break;
			default:
				getSession().add(new StatusMessage(
						StatusMessage.Category.ERROR,
						"Warning: unknown programming language " + language));
				editorMode = AceEditorMode.JAVA; break;
			}
			aceEditor.setMode(editorMode);
			
			aceEditor.setTheme(AceEditorTheme.VIBRANT_INK);
			aceEditor.setShowPrintMargin(false);
		}

		public void addEditorChangeEventHandler(final Session session, final Problem problem) {
			final User user = session.get(User.class);
			final ChangeList changeList = session.get(ChangeList.class);
			aceEditor.addOnChangeHandler(new AceEditorCallback() {
				@Override
				public void invokeAceCallback(JavaScriptObject obj) {
					try {
						// If the initial problem text hasn't been loaded yet,
						// then don't send any changes.  Otherwise we will send the
						// initial text as a change, which is definitely not what
						// we want.
						if (mode == Mode.LOADING) {
							return;
						}
						
						Change change = ChangeFromAceOnChangeEvent.convert(obj, user.getId(), problem.getProblemId());
						changeList.addChange(change);
					} catch (Exception e) {
						Window.alert("Caught exception! " + e.getMessage());
					}
				}
			});
		}

		public void setProblem(final Session session, final Problem problem) {
			RPC.editCodeService.setProblem(problem.getProblemId(), new AsyncCallback<Problem>() {
				@Override
				public void onFailure(Throwable caught) {
					GWT.log("Could not set problem", caught);
					session.add(new StatusMessage(StatusMessage.Category.ERROR, "Error loading problem on server: " + caught.getMessage()));
				}
				
				@Override
				public void onSuccess(Problem result) {
					// Awesome - the server has approved us to work on this problem.
					// Get the UI ready for some coding!

					// initiate loading of current problem text
					asyncLoadCurrentProblemText();

					// start a timer to periodically transmit pending changes to the server
					startTransmitPendingChangeTimer(session);
				}
			});
		}

		public void asyncLoadCurrentProblemText() {
			RPC.editCodeService.loadCurrentText(new AsyncCallback<ProblemText>() {
				@Override
				public void onSuccess(ProblemText result) {
					// If the ProblemText is new (meaning that this is the first time the
					// user is working on this problem), and if the problem text is
					// non-empty, insert the initial problem text
					// into the change list as a full-text change.  This handles the
					// case where the problem has a skeleton.
					if (result.isNew() && !result.getText().equals("")) {
						User user = getSession().get(User.class);
						Problem problem = getSession().get(Problem.class);
						
						Change initialChange = new Change(
								ChangeType.FULL_TEXT,
								0, 0, 0, 0,
								System.currentTimeMillis(),
								user.getId(),
								problem.getProblemId(),
								result.getText()
								);
						
						getSession().get(ChangeList.class).addChange(initialChange);
					}
					
					// Now we can start editing
					aceEditor.setText(result.getText());
					aceEditor.setReadOnly(false);
					mode = Mode.EDITING;

					// Force a redisplay: work around weirdness when an AceEditor
					// is embedded in a LayoutPanel (or in this case,
					// a DockLayoutPanel).
					// Update 1/24/2012 DHH - this no longer seems necessary, but I'm leaving it
					// in on the theory that it causes no harm, and if the bug was actually a
					// browser bug, there might be some users out there with old browsers
					// who could still possibly be affected by it.
					aceEditor.redisplay();
				}

				@Override
				public void onFailure(Throwable caught) {
					GWT.log("Couldn't get current text for problem", caught);
					getSession().add(new StatusMessage(StatusMessage.Category.ERROR, "Could not get problem text: " + caught.getMessage()));
				}
			});
		}

		public void startTransmitPendingChangeTimer(final Session session) {
			// Create timer to flush unsent change events periodically.
			this.flushPendingChangeEventsTimer = new Timer() {
				@Override
				public void run() {
					final ChangeList changeList = session.get(ChangeList.class);

					if (changeList == null) {
						// paranoia
						return;
					}

					if (changeList.getState() == ChangeList.State.UNSENT) {
						Change[] changeBatch = changeList.beginTransmit();

						AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								changeList.endTransmit(false);
								GWT.log("Failed to send change batch to server");
								session.add(new StatusMessage(StatusMessage.Category.ERROR, "Could not save code to server!"));
							}

							@Override
							public void onSuccess(Boolean result) {
								changeList.endTransmit(true);
							}
						};

						RPC.editCodeService.logChange(changeBatch, System.currentTimeMillis(), callback);
					}
				}
			};
			flushPendingChangeEventsTimer.scheduleRepeating(FLUSH_CHANGES_INTERVAL_MS);
		}
	}

	
	private UI ui;
	
	public DevelopmentPage() {
	}

	@Override
	public void createWidget() {
		ui = new UI();
	}
	
	@Override
	public void activate() {
		addSessionObject(new ChangeList());
		addSessionObject(new TestResult[0]);
		addSessionObject(new CompilerDiagnostic[0]);
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
		removeAllSessionObjects();
	}

	@Override
	public IsWidget getWidget() {
		return ui;
	}

}
