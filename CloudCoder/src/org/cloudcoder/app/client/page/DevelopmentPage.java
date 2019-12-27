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
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.QuizInProgress;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ChoiceDialogBox;
import org.cloudcoder.app.client.view.CompilerDiagnosticListView;
import org.cloudcoder.app.client.view.DevActionsPanel2;
import org.cloudcoder.app.client.view.IDevActionsPanel;
import org.cloudcoder.app.client.view.IResultsTabPanelWidget;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.ProblemNameAndBriefDescriptionView;
import org.cloudcoder.app.client.view.PythonTutorView;
import org.cloudcoder.app.client.view.QuizIndicatorView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestOutcomeSummaryView;
import org.cloudcoder.app.client.view.TestResultListView;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.event.dom.client.*; 
import com.gwtext.client.widgets.Editor;

import edu.ycp.cs.dh.acegwt.client.ace.AceAnnotationType;
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
		 * A onclean callback (such as a submission) has been requested,
		 * but the changelist is dirty.  Editing disallowed until
		 * (1) the changelist is clean, and
		 * (2) the callback has had a chance to run.
		 */
		ONCLEAN_CALLBACK_PENDING_CLEAN_CHANGE_LIST,
		
		/**
		 * An onclean callback is in progress.  Editing will be allowed
		 * when it completes. 
		 */
		ONCLEAN_CALLBACK_IN_PROGRESS,
		
		/**
		 * Editing is disabled because a quiz has ended, or some other
		 * error has occurred.
		 */
		PREVENT_EDITS,
	}
	
	private enum ResetChoice {
		CANCEL,
		CONFIRM,
	}

	/**
	 * UI class for DevelopmentPage.
	 */
	private class UI extends Composite implements Subscriber {
		public static final double SOUTH_PANEL_HEIGHT_PX = 200.0;
		public static final double BUTTONS_PANEL_WIDTH_PX = 200.0;
		public static final double VISUALIZE_BUTTON_WIDTH_PX = 100.0;

		public static final int FLUSH_CHANGES_INTERVAL_MS = 2000;
		private static final int POLL_SUBMISSION_RESULT_INTERVAL_MS = 1000;

		private ProblemNameAndBriefDescriptionView problemNameAndBriefDescriptionView;
		private ProblemDescriptionView problemDescriptionView;
		private PageNavPanel pageNavPanel;
		private IDevActionsPanel devActionsPanel;
		private LayoutPanel southLayoutPanel;
		private LayoutPanel editorLayoutPanel;
		private StatusMessageView statusMessageView;
		private QuizIndicatorView quizIndicatorView;
		private TestOutcomeSummaryView testOutcomeSummaryView;
		private double statusMessageViewRightEdgeOffset;
		private TabLayoutPanel resultsTabPanel;
		private TestResultListView testResultListView;
		private CompilerDiagnosticListView compilerDiagnosticListView;
		private List<IResultsTabPanelWidget> resultsTabPanelWidgetList;
		private Button visualizeButton;
		private ListBox listBox;

		private AceEditor aceEditor;
		private Timer flushPendingChangeEventsTimer;
		private Mode mode;
		private Timer checkPendingSubmissionTimer;
		private Runnable onCleanCallback;
  		private String[] testCaseNames;

		public UI() {
			// The top-level panel holds just the north panel (which is fixed height)
			// and the dock layout panel (which holds all of the resizable parts of the UI).
			LayoutPanel topLevelPanel = new LayoutPanel();

			// North panel is just the ProblemNameAndBriefDescriptionView and PageNavPanel
			LayoutPanel northLayoutPanel = new LayoutPanel();

			problemNameAndBriefDescriptionView = new ProblemNameAndBriefDescriptionView();
			northLayoutPanel.add(problemNameAndBriefDescriptionView);
			northLayoutPanel.setWidgetTopHeight(problemNameAndBriefDescriptionView, 0.0, Unit.PX, ProblemNameAndBriefDescriptionView.HEIGHT_PX, Unit.PX);
			northLayoutPanel.setWidgetLeftRight(problemNameAndBriefDescriptionView, 0.0, Unit.PX, BUTTONS_PANEL_WIDTH_PX, Unit.PX);
			listBox = new ListBox();
			listBox.addItem("AMBIANCE");
            listBox.addItem("CHAOS");
            listBox.addItem("CHROME");
            listBox.addItem("CLOUDS");
            listBox.addItem("CLOUDS_MIDNIGHT");
            listBox.addItem("COBALT");
            listBox.addItem("CRIMSON_EDITOR");
            listBox.addItem("DAWN");
            listBox.addItem("DREAMWEAVER");
            listBox.addItem("ECLIPSE");
            listBox.addItem("GITHUB");
            listBox.addItem("IDLE_FINGERS");
            listBox.addItem("KATZENMILCH");
            listBox.addItem("KR_THEME");
            listBox.addItem("KR");
            listBox.addItem("KUROIR");
            listBox.addItem("MERBIVORE");
            listBox.addItem("MERBIVORE_SOFT");
            listBox.addItem("MONO_INDUSTRIAL");
            listBox.addItem("MONOKAI");
            listBox.addItem("PASTEL_ON_DARK");
            listBox.addItem("SOLARIZED_DARK");
            listBox.addItem("SOLARIZED_LIGHT");
            listBox.addItem("TERMINAL");
            listBox.addItem("TEXTMATE");
            listBox.addItem("TOMORROW_NIGHT_BLUE");
            listBox.addItem("TOMORROW_NIGHT_BRIGHT");
            listBox.addItem("TOMORROW_NIGHT_EIGHTIES");
            listBox.addItem("TOMORROW_NIGHT");
            listBox.addItem("TOMORROW");
            listBox.addItem("TWILIGHT");
            listBox.addItem("VIBRANT_INK");
            listBox.addItem("XCODE");
			northLayoutPanel.add(listBox);
			northLayoutPanel.setWidgetTopHeight(listBox,0.0,Unit.PX,30.0,Unit.PX);
			northLayoutPanel.setWidgetRightWidth(listBox,180.0,Unit.PX,120.0,Unit.PX);
			
			pageNavPanel = new PageNavPanel();
			northLayoutPanel.add(pageNavPanel);
			northLayoutPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, ProblemNameAndBriefDescriptionView.HEIGHT_PX, Unit.PX);
			northLayoutPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, BUTTONS_PANEL_WIDTH_PX, Unit.PX);

			topLevelPanel.add(northLayoutPanel);
			topLevelPanel.setWidgetTopHeight(northLayoutPanel, 0.0, Unit.PX, ProblemNameAndBriefDescriptionView.HEIGHT_PX, Unit.PX);
			topLevelPanel.setWidgetLeftRight(northLayoutPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			SplitLayoutPanel dockLayoutPanel = new SplitLayoutPanel();

			// South layout panel is the status message view, quiz indicator, test outcome summary view,
			// dev actions panel (reset and submit buttons), and results tab panel.
			southLayoutPanel = new LayoutPanel();
			dockLayoutPanel.addSouth(southLayoutPanel, SOUTH_PANEL_HEIGHT_PX);

			double rightOffset = 0.0;
			this.devActionsPanel = new DevActionsPanel2();
			devActionsPanel.asWidget().setStylePrimaryName("cc-devActionsPanel");
			southLayoutPanel.add(devActionsPanel);
			southLayoutPanel.setWidgetRightWidth(devActionsPanel, rightOffset, Unit.PX, DevActionsPanel2.WIDTH_PX, Unit.PX);
			southLayoutPanel.setWidgetTopHeight(devActionsPanel, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			rightOffset += (DevActionsPanel2.WIDTH_PX + 5.0);
			
			this.testOutcomeSummaryView = new TestOutcomeSummaryView();
			southLayoutPanel.add(testOutcomeSummaryView);
			southLayoutPanel.setWidgetTopHeight(testOutcomeSummaryView, 4.0, Unit.PX, TestOutcomeSummaryView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetRightWidth(testOutcomeSummaryView, rightOffset, Unit.PX, TestOutcomeSummaryView.WIDTH_PX, Unit.PX);
			rightOffset += (TestOutcomeSummaryView.WIDTH_PX + 5.0);

			this.quizIndicatorView = new QuizIndicatorView();
			southLayoutPanel.add(quizIndicatorView);
			southLayoutPanel.setWidgetTopHeight(quizIndicatorView, 0.0, Unit.PX, QuizIndicatorView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetRightWidth(quizIndicatorView, rightOffset, Unit.PX, QuizIndicatorView.WIDTH_PX, Unit.PX);
			rightOffset += (QuizIndicatorView.WIDTH_PX + 5.0);
			
			this.statusMessageViewRightEdgeOffset = rightOffset;

			this.statusMessageView = new StatusMessageView();
			southLayoutPanel.add(statusMessageView);
			southLayoutPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, rightOffset, Unit.PX);

			this.resultsTabPanel = new TabLayoutPanel(24, Unit.PX);
			southLayoutPanel.add(resultsTabPanel);
			southLayoutPanel.setWidgetTopBottom(resultsTabPanel, StatusMessageView.HEIGHT_PX+2.0, Unit.PX, 0.0, Unit.PX);
			southLayoutPanel.setWidgetLeftRight(resultsTabPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			this.resultsTabPanelWidgetList = new ArrayList<IResultsTabPanelWidget>();
			this.testResultListView=new TestResultListView();
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
			
			// The center panel is the ProblemDescriptionView (on the left) and the editor (on the right).
			SplitLayoutPanel centerPanel = new SplitLayoutPanel();
			
			// Left is ProblemDescriptionView, right is a LayoutPanel containing the editor layout panel.
			problemDescriptionView = new ProblemDescriptionView();
			centerPanel.addWest(problemDescriptionView, ProblemDescriptionView.DEFAULT_WIDTH_PX);
			editorLayoutPanel = new LayoutPanel();
			centerPanel.add(editorLayoutPanel);
			
			dockLayoutPanel.add(centerPanel);
			
			topLevelPanel.add(dockLayoutPanel);
			topLevelPanel.setWidgetTopBottom(dockLayoutPanel, ProblemNameAndBriefDescriptionView.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			topLevelPanel.setWidgetLeftRight(dockLayoutPanel, 0.0, Unit.PX, 0.0, Unit.PX);

			initWidget(topLevelPanel);
		}

		private void addResultsTab(IResultsTabPanelWidget w, String title) {
			resultsTabPanel.add(w, title);
			resultsTabPanelWidgetList.add(w); // keep track of all results tab panel widgets
		}

		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			final Problem problem = session.get(Problem.class);
			
			// If this is a Python problem add the Visualize! button
			if (problem.getProblemType() == ProblemType.JAVA_PROGRAM || problem.getProblemType() == ProblemType.PYTHON_FUNCTION) {
				createVisualizeButton();
			}

			mode = Mode.LOADING;
			
			// Activate views
			problemNameAndBriefDescriptionView.activate(session, subscriptionRegistrar);
			problemDescriptionView.activate(session, subscriptionRegistrar);
			testResultListView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);
			quizIndicatorView.activate(session, subscriptionRegistrar);
			testOutcomeSummaryView.activate(session, subscriptionRegistrar);
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
			pageNavPanel.setBackHandler(new PageBackHandler(session) {
				@Override
				public void run() {
					// If the Session has a QuizInProgress, remove it and
					// also the Problem.
					// This prevents issues such as a quiz problem that
					// should no longer be accessible lingering in the
					// user's client-side session.
					if (session.get(QuizInProgress.class) != null) {
						session.remove(Problem.class);
						session.remove(QuizInProgress.class);
					}
					
					super.run();
				}
			});
			
			// Add submit handler
			devActionsPanel.setSubmitHandler(new Runnable() {
				@Override
				public void run() {
					if (mode == Mode.PREVENT_EDITS) {
						// Submitting is prevented if edits are prevented
						return;
					}
					runWhenClean(new Runnable() {
						@Override
						public void run() {
							doSubmit();
						}
					});
				}
			});
			
			// Add reset handler
			devActionsPanel.setResetHandler(new Runnable() {
				@Override
				public void run() {
					GWT.log("Executing reset handler...");
					
					// Do not allow reset if edits are disallowed
					if (mode == Mode.PREVENT_EDITS) {
						return;
					}

					// Require user to confirm the reset with a dialog.
					ChoiceDialogBox<ResetChoice> confirmResetDialog = new ChoiceDialogBox<ResetChoice>(
							"Really reset the problem?",
							"Do you really want to reset the problem, abandoning your work?",
							new ChoiceDialogBox.ChoiceHandler<ResetChoice>() {
								public void handleChoice(ResetChoice choice) {
									if (choice == ResetChoice.CONFIRM) {
										runWhenClean(new Runnable() {
											@Override
											public void run() {
												doResetProblem();
											}
										});
									}
								}
							});
					
					confirmResetDialog.addChoice("Cancel", ResetChoice.CANCEL);
					confirmResetDialog.addChoice("Reset the problem", ResetChoice.CONFIRM);
					
					confirmResetDialog.center();
				}
			});
			
			// Tell the server which problem we want to work on
			setProblem(session, problem);
		}

		private void createVisualizeButton() {
			// Resize the status message view to make some room
			southLayoutPanel.setWidgetLeftRight(
					statusMessageView,
					0.0, Unit.PX,
					this.statusMessageViewRightEdgeOffset - VISUALIZE_BUTTON_WIDTH_PX - 5.0,
					Unit.PX);
			
			visualizeButton = new Button("Visualize!");
			southLayoutPanel.add(visualizeButton);
			southLayoutPanel.setWidgetTopHeight(visualizeButton, 4.0, Unit.PX, 28.0, Unit.PX);
			southLayoutPanel.setWidgetRightWidth(
					visualizeButton,
					this.statusMessageViewRightEdgeOffset, Unit.PX,
					VISUALIZE_BUTTON_WIDTH_PX, Unit.PX);
			
			visualizeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onVisualize();
				}
			});
		}
		
		protected void onVisualize() {
			TestCase[] testCases = getSession().get(TestCase[].class);
			if (testCases != null) {
				launchPythonTutor(testCases);
			} else {
				RPC.getCoursesAndProblemsService.getNonSecretTestCasesForProblem(getSession().get(Problem.class).getProblemId(), new AsyncCallback<TestCase[]>() {
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							recoverFromServerSessionTimeout(new Runnable() {
								@Override
								public void run() {
									onVisualize();
								}
							});
						} else {
							getSession().add(StatusMessage.error("Could not get test cases", caught));
						}
					}
					
					@Override
					public void onSuccess(TestCase[] result) {
						//getSession().add(result);
						addSessionObject(result);
						launchPythonTutor(result);
					}
				});
			}
		}
		
		private void launchPythonTutor(TestCase[] nonSecretTestCases) {
			// Find usable client area
			int clientWidth = Window.getClientWidth();
			int clientHeight = Window.getClientHeight();

			// The PythonTutorView will be allowed to take up most of the client area
			int pythonTutorWidth = clientWidth - 100;
			int pythonTutorHeight = clientHeight - 140;
			
			// Create a dialog
			final DialogBox dialog = new DialogBox();
			
			// Create dialog client area (to contain the PythonTutorView and a dismiss button
			LayoutPanel clientArea = new LayoutPanel();
			clientArea.setWidth(pythonTutorWidth + "px");
			clientArea.setHeight((pythonTutorHeight+28+10) + "px");
			
			// Dismiss button
			Button dismissButton = new Button("Dismiss");
			clientArea.add(dismissButton);
			clientArea.setWidgetBottomHeight(dismissButton, 5.0, Unit.PX, 28.0, Unit.PX);
			clientArea.setWidgetRightWidth(dismissButton, 5.0, Unit.PX, 100.0, Unit.PX);
			dismissButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dialog.hide();
				}
			});
			
			// The PythonTutorView
			PythonTutorView pythonTutorView = new PythonTutorView(aceEditor.getText(), getSession().get(Problem.class), nonSecretTestCases, pythonTutorWidth, pythonTutorHeight);
			clientArea.add(pythonTutorView);
			clientArea.setWidgetLeftWidth(pythonTutorView, 0.0, Unit.PX, pythonTutorWidth, Unit.PX);
			clientArea.setWidgetTopHeight(pythonTutorView, 0.0, Unit.PX, pythonTutorHeight, Unit.PX);
			
			// Show the dialog!
			dialog.add(clientArea);
			dialog.setGlassEnabled(true);
			dialog.center();
		}

		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == ChangeList.State.CLEAN && mode == Mode.ONCLEAN_CALLBACK_PENDING_CLEAN_CHANGE_LIST) {
				// The change list just became clean, and an on-clean callback is pending.
				// Run the callback.
				mode = Mode.ONCLEAN_CALLBACK_IN_PROGRESS;
				onCleanCallback.run();
			}
		}
		
		/**
		 * Run a callback when the editor is clean (no changes pending
		 * to be sent to the server.)  The callback must
		 * result in a call to {@link #doneWithOnCleanCallback()}
		 * at some point in the future.
		 *  
		 * Note: if a callback is already waiting
		 * for the editor to be clean, then we do nothing.
		 * (For example, the user might have clicked the Submit!
		 * button twice, and we should ignore the second click.)
		 * 
		 * @param callback the callback to run when the editor is clean
		 */
		private void runWhenClean(Runnable callback) {
			if (onCleanCallback != null) {
				// Do nothing: a callback is already installed
				return;
			}
			
			onCleanCallback = callback;
			
			// No editing is allowed until the callback has a chance to complete
			aceEditor.setReadOnly(true);
			
			// See if the ChangeList is clean
			ChangeList changeList = getSession().get(ChangeList.class);
			if (changeList.getState() == ChangeList.State.CLEAN) {
				// We can run the callback immediately
				mode = Mode.ONCLEAN_CALLBACK_IN_PROGRESS;
				onCleanCallback.run();
			} else {
				// Must wait until editor is clean
				addSessionObject(StatusMessage.pending("Saving your code..."));
				mode = Mode.ONCLEAN_CALLBACK_PENDING_CLEAN_CHANGE_LIST;
			}
		}
		
		/**
		 * This method should be called when an on-clean callback has
		 * finished, and we can make the editor read/write again.
		 */
		private void doneWithOnCleanCallback() {
			if (onCleanCallback == null) {
				GWT.log("doneWithOnCleanCallback() called, but there is no onCleanCallback");
			}
			mode = Mode.EDITING;
			aceEditor.setReadOnly(false);
			onCleanCallback = null;
			GWT.log("Done with onClean callback");
		}

		private void doSubmit() {
			// Full text of submission has arrived at server,
			// and because the editor is read-only, we know that the
			// local text is in-sync.  So, submit the code!
			
			addSessionObject(StatusMessage.pending("Testing your code, please wait..."));
			// clear any annotations we set from compiler errors
			Problem problem = getSession().get(Problem.class);
			String text = aceEditor.getText();

			doSubmitRPC(problem, text);
		}

		protected void doSubmitRPC(final Problem problem, final String text) {
			// Do not allow submit if edits are disallowed
			if (mode == Mode.PREVENT_EDITS) {
				return;
			}

			RPC.submitService.submit(problem.getProblemId(), text, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable(){
							public void run() {
								// If the session timed out, we need to attempt
								// to set the Problem again (since the submit RPC
								// service expects that the Problem is in the session).
								RPC.editCodeService.setProblem(problem.getProblemId(), new AsyncCallback<Problem>() {
									@Override
									public void onFailure(Throwable caught) {
										// We're fairly hosed at this point
										getSession().add(StatusMessage.error("Could not set problem on session recovery", caught));
									}
									
									@Override
									public void onSuccess(Problem result) {
										// Try to submit again!
										doSubmitRPC(problem, text);
									}
								});
							}
						});
					} else if (caught instanceof QuizEndedException) {
						// Quiz ended
						doEndQuiz();
					} else {
					    //TODO: Is this where a better message should come if we can't
					    // find a C/C++ compiler?
						addSessionObject(StatusMessage.error("Error: " + caught.getMessage()));
						// FIXME: restore editor to read/write
					}
				}

				@Override
				public void onSuccess(Void result) {
					// Start polling for the SubmissionResult
					checkPendingSubmissionTimer.scheduleRepeating(POLL_SUBMISSION_RESULT_INTERVAL_MS);
				}
			});
		}
		
		private void doResetProblem() {
			// Do not allow reset if edits are disallowed
			if (mode == Mode.PREVENT_EDITS) {
				return;
			}
			
			GWT.log("Resetting problem");
			
			// The Problem object should contain the skeleton code
			Problem problem = getSession().get(Problem.class);

			// Temporarily block the editor's onChange handler from
			// adding changes to the ChangeList.
			mode = Mode.LOADING;
			
			// Reset the editor contents.
			aceEditor.setReadOnly(false);
			String skeleton = problem.getSkeleton() != null ? problem.getSkeleton() : "";
			aceEditor.setText(skeleton);
			aceEditor.setReadOnly(true);
			
			// Create a full-text change,
			// and add it to the ChangeList.
			Change fullTextChange = new Change(
					ChangeType.FULL_TEXT,
					0, 0, 0, 0,
					System.currentTimeMillis(),
					getSession().get(User.class).getId(),
					problem.getProblemId(),
					skeleton);
			getSession().get(ChangeList.class).addChange(fullTextChange);
			
			// At this point we can finish the on-clean callback,
			// which will restore the mode to EDITING and make the editor
			// read/write again.
			doneWithOnCleanCallback();
		}

		private void createEditor(Language language) {
			aceEditor = new AceEditor();
			aceEditor.setSize("100%", "100%");
			editorLayoutPanel.add(aceEditor);
			aceEditor.startEditor();
			aceEditor.setFontSize("14px");
			// based on programming language used in the Problem,
			// choose an editor mode
			AceEditorMode editorMode = ViewUtil.getModeForLanguage(language);
			if (editorMode == null) {
				addSessionObject(StatusMessage.error("Warning: unknown programming language " + language));
				editorMode = AceEditorMode.JAVA;
			}
			aceEditor.setMode(editorMode);
			listBox.addChangeHandler(new ChangeHandler()
			{
				public void onChange(ChangeEvent ce)
				{
					int i = listBox.getSelectedIndex();
					String name =listBox.getValue(i);
					if(i>=0)
					{
						aceEditor.setTheme(AceEditorTheme.valueOf(name));
						Window.alert("Theme: " + listBox.getValue(i));
					}
					
				}
				
			});
			aceEditor.setShowPrintMargin(false);
		}
		private void addEditorChangeEventHandler(final Session session, final Problem problem) {
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
						
						int userId = user.getId();
						Integer problemId = problem.getProblemId();
						Change change = ChangeFromAceOnChangeEvent.convert(obj, userId, problemId);
						changeList.addChange(change);
					} catch (Exception e) {
						GWT.log("Exception adding change", e);
						Window.alert("Caught exception! " + e.getMessage());
					}
				}
			});
		}

		private void setProblem(final Session session, final Problem problem) {
			RPC.editCodeService.setProblem(problem.getProblemId(), new AsyncCallback<Problem>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								setProblem(session, problem);
							}
						});
					} else {
						GWT.log("Could not set problem", caught);
						addSessionObject(StatusMessage.error("Error loading exercise", caught));
					}
				}
				
				@Override
				public void onSuccess(Problem result) {
					if (result == null) {
						// The server did not approve us to work on the problem.
						// One possibility is that the user has an active quiz in
						// progress.  When a student starts working on a quiz,
						// working on other problems is not allowed.
						session.add(StatusMessage.error("This exercise is not available (did you start a quiz?)"));
						mode = Mode.PREVENT_EDITS;
					} else {
						// Awesome - the server has approved us to work on this problem.
						// Get the UI ready for some coding!
	
						// initiate loading of current problem text
						asyncLoadCurrentProblemText();
	
						// start a timer to periodically transmit pending changes to the server
						startTransmitPendingChangeTimer(session);
						
						// create (but do not start) a timer to periodically poll to check
						// if a submission has completed compilation/testing
						createCheckPendingSubmissionTimer();
					}
				}
			});
		}

		private void asyncLoadCurrentProblemText() {
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
					
					// Check to see if this problem is a quiz.
					if (result.isQuiz()) {
						getSession().add(new QuizInProgress());
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
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								asyncLoadCurrentProblemText();
							}
						});
					} else {
						GWT.log("Couldn't get current text for problem", caught);
						addSessionObject(StatusMessage.error("Could not get problem text", caught));
					}
				}
			});
		}

		private void startTransmitPendingChangeTimer(final Session session) {
			// Create timer to flush unsent change events periodically.
			this.flushPendingChangeEventsTimer = new Timer() {
				@Override
				public void run() {
					if (mode == Mode.PREVENT_EDITS) {
						return; // no further edits are allowed
					}
					
					final ChangeList changeList = session.get(ChangeList.class);

					if (changeList == null) {
						// paranoia
						return;
					}

					if (changeList.getState() == ChangeList.State.UNSENT) {
						Change[] changeBatch = changeList.beginTransmit();
						logChangeRPC(changeList, changeBatch);
					}
				}

				protected void logChangeRPC(final ChangeList changeList, final Change[] changeBatch) {
					if (mode == Mode.PREVENT_EDITS) {
						return; // no further edits are allowed
					}
					RPC.editCodeService.logChange(changeBatch, System.currentTimeMillis(), new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Error sending change batch", caught);
							if (caught instanceof CloudCoderAuthenticationException) {
								GWT.log("Starting recovery from server session timeout...");
								recoverFromServerSessionTimeout(new Runnable(){
									public void run() {
										// Try again!
										logChangeRPC(changeList, changeBatch);
									}
								});
							} else if (caught instanceof QuizEndedException) {
								// Quiz ended
								doEndQuiz();
							} else {
								changeList.endTransmit(false);
								addSessionObject(StatusMessage.error("Could not save code to server", caught));
							}
						}

						@Override
						public void onSuccess(Boolean result) {
							changeList.endTransmit(true);
						}
					});
				}
			};
			flushPendingChangeEventsTimer.scheduleRepeating(FLUSH_CHANGES_INTERVAL_MS);
		}
		
		private void createCheckPendingSubmissionTimer() {
			// Create, but do not start, the timer that we will be used to
			// poll for a pending SubmissionResult.
			checkPendingSubmissionTimer = new Timer() {
				private boolean checking;
				
				@Override
				public void run() {
					if (!checking) {
						checking = true;
						checkSubmissionRPC();
					}
				}

				protected void checkSubmissionRPC() {
					RPC.submitService.checkSubmission(new AsyncCallback<SubmissionResult>() {
						/* (non-Javadoc)
						 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
						 */
						@Override
						public void onFailure(Throwable caught) {
							if (caught instanceof CloudCoderAuthenticationException) {
								recoverFromServerSessionTimeout(new Runnable() {
									@Override
									public void run() {
										// Try again!
										checkSubmissionRPC();
									}
								});
							} else {
								checking = false;
								addSessionObject(StatusMessage.error("Error checking pending submission", caught));
								checkPendingSubmissionTimer.cancel();
							}
						}
						
						/* (non-Javadoc)
						 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
						 */
						@Override
						public void onSuccess(SubmissionResult result) {
							checking = false;
							if (result != null) {
								// Received the SubmissionResult, yay
								onReceiveSubmissionResult(result);
								checkPendingSubmissionTimer.cancel();
							}
						}
					});
				}
			};
		}
		
		private void onReceiveSubmissionResult(SubmissionResult result) {
			// clear any annotations from the editor
		    aceEditor.clearAnnotations();
		    if (result==null){
				addSessionObject(StatusMessage.error("Results from Builder are empty"));
				addSessionObject(new NamedTestResult[0]);
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
					addSessionObject(StatusMessage.error("Error testing submission"));
					addSessionObject(new NamedTestResult[0]);
				} else if (result.getCompilationResult().getOutcome()==CompilationOutcome.FAILURE) {
					// Code did not compile
					addSessionObject(StatusMessage.error("Error compiling submission"));
					addSessionObject(new NamedTestResult[0]);
					// mark the ACE editor with compiler errors
					for (CompilerDiagnostic d : compilerDiagnosticList) {
					    aceEditor.addAnnotation((int)d.getStartLine()-1, (int)d.getStartColumn()-1, d.getMessage(), AceAnnotationType.ERROR);
					}
					aceEditor.setAnnotations();
				} else {
					// Code compiled, and test results were sent back.

					// Display the test results
					displayTestResults(result);

					// Add a status message about the results
					if (result.isAllTestsPassed()) {
						addSessionObject(StatusMessage.goodNews("All tests passed! You rock."));
					} else {
						addSessionObject(StatusMessage.error("At least one test failed: check test results"));
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
				doneWithOnCleanCallback();
			}

		}

		/**
		 * Display the test results from given {@link SubmissionResult}.
		 * This is complicated slightly by the requirement to display
		 * test case names, which must be loaded via RPC.
		 * 
		 * @param submissionResult the {@link SubmissionResult} containing the
		 *        test results to display
		 */
		private void displayTestResults(final SubmissionResult submissionResult) {
			if (this.testCaseNames == null || this.testCaseNames.length != submissionResult.getTestResults().length) {
				// Need to load test case names via RPC
				RPC.getCoursesAndProblemsService.getTestCaseNamesForProblem(getSession().get(Problem.class).getProblemId(), new AsyncCallback<String[]>() {
					@Override
					public void onFailure(Throwable caught) {
						// Hmm, couldn't get the test case names
						getSession().add(StatusMessage.error("Could not get test case names", caught));
						
						// Add fake test case names
						createFakeTestCaseNames(submissionResult.getTestResults());
						displayNamedTestResults(submissionResult.getTestResults());
					}
					@Override
					public void onSuccess(String[] result) {
						if (result.length != submissionResult.getTestResults().length) {
							// It is possible to receive an empty array if the user is
							// not authorized to see this problem
							createFakeTestCaseNames(submissionResult.getTestResults());
						} else {
							// Great, we have the test case names
							testCaseNames = result;
						}
						displayNamedTestResults(submissionResult.getTestResults());
					}
				});
			} else {
				displayNamedTestResults(submissionResult.getTestResults());
			}
			
		}

		/**
		 * @param results
		 */
		private void displayNamedTestResults(TestResult[] results) {
			NamedTestResult[] namedTestResults = new NamedTestResult[results.length];
			for (int i = 0; i < results.length; i++) {
				namedTestResults[i] = new NamedTestResult(testCaseNames[i], results[i]);
			}
			addSessionObject(namedTestResults);
		}

		private void createFakeTestCaseNames(TestResult[] testResults) {
			testCaseNames = new String[testResults.length];
			int count = 0;
			for (int i = 0; i < testResults.length; i++) {
				testCaseNames[i] = "t" + (count++);
			}
		}

		private void doEndQuiz() {
			QuizInProgress quizInProgress = getSession().get(QuizInProgress.class);
			if (quizInProgress == null) {
				// Should not happen
				addSessionObject(StatusMessage.error("Unexpected QuizEndedException"));
			} else {
				quizInProgress.setEnded(true);
				addSessionObject(StatusMessage.information("The quiz has ended"));
			}
			mode = Mode.PREVENT_EDITS;
			aceEditor.setReadOnly(true);
			flushPendingChangeEventsTimer.cancel();
		}
	}

	
	public DevelopmentPage() {
	}

	@Override
	public void createWidget() {
		setWidget(new UI());
	}
	
	@Override
	public Class<?>[] getRequiredPageObjects() {
		return new Class<?>[]{CourseSelection.class, Problem.class};
	}
	
	@Override
	public void activate() {
		addSessionObject(new ChangeList());
		addSessionObject(new NamedTestResult[0]);
		addSessionObject(new CompilerDiagnostic[0]);
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		super.deactivate();
		removeAllSessionObjects();
	}
	
	@Override
	public PageId getPageId() {
		return PageId.DEVELOPMENT;
	}
	
	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
	}
}
