// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import org.cloudcoder.app.client.model.ChangeFromAceOnChangeEvent;
import org.cloudcoder.app.client.model.CodeState;
import org.cloudcoder.app.client.model.CodeStateManager;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.QuizInProgress;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.CompilerDiagnosticListView;
import org.cloudcoder.app.client.view.DevActionsPanel;
import org.cloudcoder.app.client.view.IResultsTabPanelWidget;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.QuizIndicatorView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestOutcomeSummaryView;
import org.cloudcoder.app.client.view.TestResultListView;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Reimplementation of the development page (where students edit
 * code, submit, see test results, etc.)
 * 
 * @author David Hovemeyer
 */
public class DevelopmentPage2 extends CloudCoderPage {
	public static final double NORTH_PANEL_HEIGHT_PX = 300.0;
	public static final double SOUTH_PANEL_HEIGHT_PX = 200.0;
	public static final double BUTTONS_PANEL_WIDTH_PX = 200.0;
	
	// Flush pending changes (edits) every 5 seconds.
	private static int FLUSH_PENDING_CHANGES_INTERVAL_MS = 5000;
	
	private class UI extends Composite implements SessionObserver {
		private LayoutPanel northLayoutPanel;
		private ProblemDescriptionView problemDescriptionView;
		private LayoutPanel buttonsLayoutPanel;
		private PageNavPanel pageNavPanel;
		private DevActionsPanel devActionsPanel;
		private LayoutPanel southLayoutPanel;
		private StatusMessageView statusMessageView;
		private QuizIndicatorView quizIndicatorView;
		private TestOutcomeSummaryView testOutcomeSummaryView;
		private TabLayoutPanel resultsTabPanel;
		private ArrayList<IResultsTabPanelWidget> resultsTabPanelWidgetList;
		private TestResultListView testResultListView ;
		private CompilerDiagnosticListView compilerDiagnosticListView;
		private LayoutPanel centerLayoutPanel;
		
		private CodeStateManager codeStateManager;
		private AceEditor aceEditor;
		private Timer flushPendingChangesTimer;

		public UI() {
			SplitLayoutPanel dockLayoutPanel = new SplitLayoutPanel();

			northLayoutPanel = new LayoutPanel();
			dockLayoutPanel.addNorth(northLayoutPanel, NORTH_PANEL_HEIGHT_PX);
			problemDescriptionView = new ProblemDescriptionView();
			northLayoutPanel.add(problemDescriptionView);
			northLayoutPanel.setWidgetLeftRight(problemDescriptionView, 0.0, Unit.PX, BUTTONS_PANEL_WIDTH_PX, Unit.PX);
			northLayoutPanel.setWidgetTopBottom(problemDescriptionView, 0.0, Unit.PX, 10.0, Unit.PX);
			buttonsLayoutPanel = new LayoutPanel(); // contains PageNavPanel and DevActionsPanel
			pageNavPanel = new PageNavPanel();
			buttonsLayoutPanel.add(pageNavPanel);
			buttonsLayoutPanel.setWidgetLeftRight(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			buttonsLayoutPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
			devActionsPanel = new DevActionsPanel();
			buttonsLayoutPanel.add(devActionsPanel);
			buttonsLayoutPanel.setWidgetLeftRight(devActionsPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			buttonsLayoutPanel.setWidgetTopBottom(devActionsPanel, PageNavPanel.HEIGHT_PX, Style.Unit.PX, 0.0, Unit.PX);
			
			northLayoutPanel.add(buttonsLayoutPanel);
			northLayoutPanel.setWidgetRightWidth(buttonsLayoutPanel, 0.0, Unit.PX, BUTTONS_PANEL_WIDTH_PX, Unit.PX);
			northLayoutPanel.setWidgetTopBottom(buttonsLayoutPanel, 0.0, Unit.PX, 0.0, Unit.PX);

			southLayoutPanel = new LayoutPanel();
			dockLayoutPanel.addSouth(southLayoutPanel, SOUTH_PANEL_HEIGHT_PX);
			
			this.statusMessageView = new StatusMessageView();
			southLayoutPanel.add(statusMessageView);
			southLayoutPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, TestOutcomeSummaryView.WIDTH_PX + QuizIndicatorView.WIDTH_PX + 16.0, Unit.PX);
			this.quizIndicatorView = new QuizIndicatorView();
			southLayoutPanel.add(quizIndicatorView);
			southLayoutPanel.setWidgetTopHeight(quizIndicatorView, 0.0, Unit.PX, QuizIndicatorView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetRightWidth(quizIndicatorView, TestOutcomeSummaryView.WIDTH_PX + 8.0, Unit.PX, QuizIndicatorView.WIDTH_PX, Unit.PX);
			this.testOutcomeSummaryView = new TestOutcomeSummaryView();
			southLayoutPanel.add(testOutcomeSummaryView);
			southLayoutPanel.setWidgetTopHeight(testOutcomeSummaryView, 2.0, Unit.PX, TestOutcomeSummaryView.HEIGHT_PX, Unit.PX);
			southLayoutPanel.setWidgetRightWidth(testOutcomeSummaryView, 0.0, Unit.PX, TestOutcomeSummaryView.WIDTH_PX, Unit.PX);

			this.resultsTabPanel = new TabLayoutPanel(24, Unit.PX);
			southLayoutPanel.add(resultsTabPanel);
			southLayoutPanel.setWidgetTopBottom(resultsTabPanel, StatusMessageView.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			southLayoutPanel.setWidgetLeftRight(resultsTabPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			// Create results tab widgets
			this.resultsTabPanelWidgetList = new ArrayList<IResultsTabPanelWidget>();

			// Test result list view
			this.testResultListView = new TestResultListView();
			addResultsTab(this.testResultListView, "Test results");
			
			// Compiler diagnostic list view
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
		
		@Override
		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			// Activate views
			problemDescriptionView.activate(session, subscriptionRegistrar);
			testResultListView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);
			quizIndicatorView.activate(session, subscriptionRegistrar);
			testOutcomeSummaryView.activate(session, subscriptionRegistrar);
			compilerDiagnosticListView.activate(session, subscriptionRegistrar);
			
			// Create CodeStateManager
			Problem problem = session.get(Problem.class);
			codeStateManager = new CodeStateManager();
			
			// Create AceEditor
			createEditor(problem.getProblemType().getLanguage());
			
			// Add logout and back handlers, taking care that all code
			// is saved and pending operations are complete.
			pageNavPanel.setLogoutHandler(new Runnable() {
				@Override
				public void run() {
					leavePage(new LogoutHandler(getSession()));
				}
			});
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					leavePage(new PageBackHandler(getSession()));
				}
			});
			
			// Set problem in server session, load program text, etc.
			beginProblem();
		}

		/**
		 * Execute a runnable that causes navigation away from this page.
		 * Ensures that changes are saved, pending operations are completed,
		 * etc.
		 * 
		 * @param action the action to run to navigate away from the page
		 */
		private void leavePage(Runnable action) {
			if (!codeStateManager.startedEditing()) {
				// We never reached a state where the user was actually editing
				// the code, so we can navigate away now.
				action.run();
			} else {
				// Ensure all code has been saved before navigating away.
				aceEditor.setReadOnly(true);
				flushPendingChanges();
				codeStateManager.runWhen(CodeStateManager.NOT_DIRTY_AND_NO_PENDING_OPERATION, action);
			}
		}
		
		public void deactivate() {
			if (flushPendingChangesTimer != null) {
				flushPendingChangesTimer.cancel();
			}
		}

		private void createEditor(Language language) {
			aceEditor = new AceEditor();
			aceEditor.setSize("100%", "100%");
			centerLayoutPanel.add(aceEditor);
			aceEditor.startEditor();
			
			aceEditor.setReadOnly(true); // Editor starts read-only
			
			aceEditor.setFontSize("14px");
			
			// based on programming language used in the Problem,
			// choose an editor mode
			AceEditorMode editorMode = ViewUtil.getModeForLanguage(language);
			if (editorMode == null) {
				addSessionObject(StatusMessage.error("Warning: unknown programming language " + language));
				editorMode = AceEditorMode.TEXT;
			}
			aceEditor.setMode(editorMode);
			
			aceEditor.setTheme(AceEditorTheme.VIBRANT_INK);
			aceEditor.setShowPrintMargin(false);
		}

		/**
		 * Begin working on the problem in the {@link Session}.
		 */
		private void beginProblem() {
			final Problem problem = getProblem();
			RPC.editCodeService.setProblem(problem.getProblemId(), new AsyncCallback<Problem>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								beginProblem();
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
						getSession().add(StatusMessage.error("This exercise is not available (did you start a quiz?)"));
						codeStateManager.preventEdits();
					} else {
						// Problem has been set
						codeStateManager.setProblem(getProblem());
						
						// Initiate loading of program text
						loadProgramText();
					}
				}
			});
		}
		
		/**
		 * Load the program text.
		 */
		private void loadProgramText() {
			RPC.editCodeService.loadCurrentText(new AsyncCallback<ProblemText>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								loadProgramText();
							}
						});
					} else {
						GWT.log("Couldn't get current text for exercise", caught);
						addSessionObject(StatusMessage.error("Could not get program text", caught));
					}
				}

				@Override
				public void onSuccess(ProblemText result) {
					// Problem has been set, and program text is here, so we're
					// ready to allow the user to work on the exercise.
					// Get things set up.
					
					// We're now at the point that the code should be editable
					codeStateManager.setProgramText();
					
					// Set the text in the editor
					aceEditor.setText(result.getText());
					
					// If program text is new, schedule the insertion of the skeleton to be
					// sent as the first Change
					if (result.isNew()) {
						GWT.log("Scheduling initial change for new program text");
						Change initialChange = new Change(
								ChangeType.FULL_TEXT,
								0, 0, 0, 0,
								System.currentTimeMillis(),
								getUser().getId(),
								getProblem().getProblemId(),
								result.getText());
						codeStateManager.addChange(initialChange);
					}
					
					// At this point it should be safe to install the
					// editor change callback.
					aceEditor.addOnChangeHandler(new AceEditorCallback() {
						@Override
						public void invokeAceCallback(JavaScriptObject obj) {
							Change change = ChangeFromAceOnChangeEvent.convert(obj, getUser().getId(), getProblem().getProblemId());
							codeStateManager.addChange(change);
						}
					});
					
					// We can now make the editor read/write
					aceEditor.setReadOnly(false);

					// Not sure if this is still necessary, but it's not harmful,
					// and might be beneficial for users running old/buggy browsers.
					aceEditor.redisplay();
					
					// State a timer to periodically flush pending changes.
					flushPendingChangesTimer = new Timer() {
						@Override
						public void run() {
							flushPendingChanges();
						}
					};
					flushPendingChangesTimer.scheduleRepeating(FLUSH_PENDING_CHANGES_INTERVAL_MS);
				}
			});
		}
		
		private void flushPendingChanges() {
			if (codeStateManager.getState() == CodeState.EDITABLE_DIRTY) {
				GWT.log("Flush changes");
				Change[] changes = codeStateManager.saveChanges();
				GWT.log("Sending " + changes.length + " changes");
				RPC.editCodeService.logChange(changes, System.currentTimeMillis(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof CloudCoderAuthenticationException) {
							recoverFromServerSessionTimeout(new Runnable() {
								@Override
								public void run() {
									 // Try again
									flushPendingChanges();
								}
							});
						} else if (caught instanceof QuizEndedException) {
							doEndQuiz();
						} else {
							getSession().add(StatusMessage.error("Could not save code changes", caught));
							codeStateManager.finishSavingChanges(false);
						}
					}

					@Override
					public void onSuccess(Boolean result) {
						codeStateManager.finishSavingChanges(true);
						GWT.log("Changes saved successfully");
					}
				});
			}
		}

		protected void doEndQuiz() {
			getSession().add(StatusMessage.information("Quiz has ended"));
			codeStateManager.preventEdits();
		}
	}

	@Override
	public void createWidget() {
		setWidget(new UI());
	}
	
	@Override
	public Class<?>[] getRequiredPageObjects() {
		return new Class<?>[0]; // FIXME
	}

	@Override
	public void activate() {
		addSessionObject(new NamedTestResult[0]);
		addSessionObject(new CompilerDiagnostic[0]);
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		super.deactivate();
		
		// If the user was working on a quiz, remove the problem
		// from the session.
		if (getSession().get(QuizInProgress.class) != null) {
			getSession().remove(QuizInProgress.class);
			getSession().remove(Problem.class);
		}
		
		if (getWidget() instanceof UI) {
			((UI)getWidget()).deactivate();
		}
	}

	@Override
	public PageId getPageId() {
		return PageId.DEVELOPMENT;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
	}

	/**
	 * @return
	 */
	private User getUser() {
		return getSession().get(User.class);
	}

	/**
	 * @return
	 */
	private Problem getProblem() {
		return getSession().get(Problem.class);
	}

}
