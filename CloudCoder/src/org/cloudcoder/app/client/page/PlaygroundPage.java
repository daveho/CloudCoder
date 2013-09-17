// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.client.model.ChangeList;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.CompilerDiagnosticListView;
import org.cloudcoder.app.client.view.IResultsTabPanelWidget;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.PlaygroundActionsPanel;
import org.cloudcoder.app.client.view.PlaygroundResultListView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.PlaygroundTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceAnnotationType;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * @author jaimespacco
 *
 */
public class PlaygroundPage extends CloudCoderPage
{
    public enum Mode {
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
    
    /**
     * UI class for DevelopmentPage.
     */
    private class UI extends Composite implements Subscriber {
        public static final double NORTH_PANEL_HEIGHT_PX = 50.0;
        public static final double SOUTH_PANEL_HEIGHT_PX = 200.0;

        private static final int POLL_SUBMISSION_RESULT_INTERVAL_MS = 1000;

        private LayoutPanel northLayoutPanel;
        private PageNavPanel pageNavPanel;
        private PlaygroundActionsPanel playgroundActionsPanel;
        private LayoutPanel southLayoutPanel;
        private LayoutPanel centerLayoutPanel;
        private StatusMessageView statusMessageView;

        private TabLayoutPanel resultsTabPanel;
        private PlaygroundResultListView playgroundResultListView;
        private CompilerDiagnosticListView compilerDiagnosticListView;
        private List<IResultsTabPanelWidget> resultsTabPanelWidgetList;

        private AceEditor aceEditor;
        private Mode mode;
        private Timer checkPendingSubmissionTimer;
        private Runnable onCleanCallback;

        public UI() {
            SplitLayoutPanel dockLayoutPanel = new SplitLayoutPanel();

            northLayoutPanel = new LayoutPanel();
            dockLayoutPanel.addNorth(northLayoutPanel, NORTH_PANEL_HEIGHT_PX);
            // playground actions (select language, run)
            playgroundActionsPanel = new PlaygroundActionsPanel();
            northLayoutPanel.add(playgroundActionsPanel);
            northLayoutPanel.setWidgetLeftWidth(playgroundActionsPanel, 0.0, Unit.PCT, 50.0, Unit.PCT);
            playgroundActionsPanel.setChangeLanguageHandler(new Runnable() {
                @Override
                public void run() {
                    mode=Mode.PREVENT_EDITS;
                    aceEditor.setReadOnly(true);
                    Language lang=playgroundActionsPanel.getLanguage();
                    switch(lang) {
                    case JAVA:
                        addSessionObject(StatusMessage.information("Language set to "+lang));
                        break;
                    case C:
                        addSessionObject(StatusMessage.information("Language set to "+lang));
                        break;
                    case CPLUSPLUS:
                        addSessionObject(StatusMessage.information("Language set to "+lang));
                        break;
                    case PYTHON:
                    case RUBY:
                    default:
                        addSessionObject(StatusMessage.information(lang+" not yet supported; defaulting to Java"));
                        lang=Language.JAVA;
                        playgroundActionsPanel.setSelectedLanguage(Language.JAVA);
                    }
                    if (lang!=null && aceEditor!=null) {
                        setEditorLanguage(lang);
                    }
                    aceEditor.setReadOnly(false);
                    mode=Mode.EDITING;
                }
            });
            // page nav (back, log out)
            pageNavPanel = new PageNavPanel();
            northLayoutPanel.add(pageNavPanel);
            northLayoutPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PCT, 50.0, Unit.PCT);
            
            southLayoutPanel = new LayoutPanel();
            dockLayoutPanel.addSouth(southLayoutPanel, SOUTH_PANEL_HEIGHT_PX);

            this.statusMessageView = new StatusMessageView();
            southLayoutPanel.add(statusMessageView);
            southLayoutPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
            
            this.resultsTabPanel = new TabLayoutPanel(24, Unit.PX);
            southLayoutPanel.add(resultsTabPanel);
            southLayoutPanel.setWidgetTopBottom(resultsTabPanel, StatusMessageView.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
            southLayoutPanel.setWidgetLeftRight(resultsTabPanel, 0.0, Unit.PX, 0.0, Unit.PX);

            this.resultsTabPanelWidgetList = new ArrayList<IResultsTabPanelWidget>();
            // the playgroundResultListView still needs to be initialize in the activate() method
            this.playgroundResultListView=new PlaygroundResultListView();
            addResultsTab(this.playgroundResultListView, "Run results");

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
            mode = Mode.LOADING;

            // Activate views
            playgroundResultListView.activate(session, subscriptionRegistrar);

            statusMessageView.activate(session, subscriptionRegistrar);
            compilerDiagnosticListView.activate(session, subscriptionRegistrar);

            // Subscribe to ChangeList events
            // XXX Still necessary?
            session.get(ChangeList.class).subscribe(ChangeList.State.CLEAN, this, subscriptionRegistrar);

            // Create AceEditor instance
            createEditor(Language.JAVA);

            // add a handler for editor change events
            //TODO need to associate with an editing session
            //TODO Sort of like a mini-file system?
            //addEditorChangeEventHandler(session, problem);

            // Add logout and back handlers
            pageNavPanel.setLogoutHandler(new LogoutHandler(session));
            pageNavPanel.setBackHandler(new PageBackHandler(session) {
                @Override
                public void run() {
                    super.run();
                }
            });

            // Add submit handler
            playgroundActionsPanel.setRunHandler(new Runnable() {
                @Override
                public void run() {
                    if (mode == Mode.PREVENT_EDITS) {
                        // Submitting is prevented if edits are prevented
                        return;
                    }
                    runWhenClean(new Runnable() {
                        @Override
                        public void run() {
                            doRun();
                        }
                    });
                }
            });
            createCheckPendingSubmissionTimer();
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

        private void doRun() {
            // Full text of submission has arrived at server,
            // and because the editor is read-only, we know that the
            // local text is in-sync.  So, submit the code!

            addSessionObject(StatusMessage.pending("Running your code, please wait..."));
            // clear any annotations we set from compiler errors
            //Problem problem = getSession().get(Problem.class);
            String text = aceEditor.getText();
            
            // Create a fake problem to send to the server
            Problem fakeProblem=new Problem();
            //TODO get the language out of the session, or wherever we end up storing it
            
            Language lang=playgroundActionsPanel.getLanguage();
            ProblemType type=ProblemType.JAVA_PROGRAM;
            switch(lang) {
            case JAVA:
                type=ProblemType.JAVA_PROGRAM;
                break;
            case C:
            case CPLUSPLUS:
                type=ProblemType.C_PROGRAM;
                break;
            case PYTHON:
                //FIXME Add PYTHON_METHOD and RUBY_METHOD to problem types
                //type=ProblemType.PYTHON_FUNCTION;
                //break;
            case RUBY:
                //type=ProblemType.RUBY_METHOD;
                //break;
            default:
                type=ProblemType.JAVA_PROGRAM;
                addSessionObject(StatusMessage.information("Language not yet supported; default to Java"));
            }
            fakeProblem.setProblemType(type);
            fakeProblem.setProblemId(1);
            addSessionObject(fakeProblem);
            
            TestCase[] testCaseList=getSession().get(TestCase[].class);
            
            doRunRPC(fakeProblem, text, testCaseList);
        }

        protected void doRunRPC(final Problem problem, final String text, final TestCase[] testCaseList) {
            // Do not allow submit if edits are disallowed
            if (mode == Mode.PREVENT_EDITS) {
                return;
            }

            RPC.runService.run(problem, text, testCaseList, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof CloudCoderAuthenticationException) {
                        recoverFromServerSessionTimeout(new Runnable(){
                            public void run() {
                                // Try again!
                                doRunRPC(problem, text, testCaseList);
                            }
                        });
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
                    // TODO should I re-enable the editor here?
                    checkPendingSubmissionTimer.scheduleRepeating(POLL_SUBMISSION_RESULT_INTERVAL_MS);
                }
            });
        }

        private void setEditorLanguage(Language language) {
            AceEditorMode editorMode = ViewUtil.getModeForLanguage(language);
            if (editorMode == null) {
                addSessionObject(StatusMessage.error("Warning: unknown programming language " + language));
                editorMode = AceEditorMode.JAVA;
            }
            aceEditor.setMode(editorMode);
        }
        
        private void createEditor(Language language) {
            aceEditor = new AceEditor();
            aceEditor.setSize("100%", "100%");
            centerLayoutPanel.add(aceEditor);
            aceEditor.startEditor();
            aceEditor.setFontSize("14px");

            // based on programming language used in the Problem,
            // choose an editor mode
            setEditorLanguage(language);

            aceEditor.setTheme(AceEditorTheme.VIBRANT_INK);
            aceEditor.setShowPrintMargin(false);
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
                    RPC.runService.checkSubmission(new AsyncCallback<SubmissionResult>() {
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
                addSessionObject(new PlaygroundTestResult[0]);
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
                    addSessionObject(StatusMessage.error("Error running code"));
                    addSessionObject(new PlaygroundTestResult[0]);
                } else if (result.getCompilationResult().getOutcome()==CompilationOutcome.FAILURE) {
                    // Code did not compile
                    addSessionObject(StatusMessage.error("Error compiling submission"));
                    addSessionObject(new PlaygroundTestResult[0]);
                    // mark the ACE editor with compiler errors
                    for (CompilerDiagnostic d : compilerDiagnosticList) {
                        aceEditor.addAnnotation((int)d.getStartLine()-1, (int)d.getStartColumn()-1, d.getMessage(), AceAnnotationType.ERROR);
                    }
                    aceEditor.setAnnotations();
                } else {
                    // Code compiled, and test results were sent back.

                    // Display the test results
                    displayTestResults(result);
                    addSessionObject(StatusMessage.goodNews("Brilliant!  Code compiled and ran!  Check output below"));
                }

                if (compilerDiagnosticList.length > 0) {
                    // show the compiler diagnostics
                    resultsTabPanel.selectTab(compilerDiagnosticListView);
                } else {
                    // show the test results tab
                    resultsTabPanel.selectTab(playgroundResultListView);
                }

                // Can resume editing now
                doneWithOnCleanCallback();
            }

        }

        /**
         * Display the test results from given {@link SubmissionResult}.
         * 
         * For playground mode, we don't care about the names of the TestCases.
         * (We <i>do</i> care about the test case names in the analogous method to this
         * one in {@link DevelopmentPage})
         * 
         * @param submissionResult the {@link SubmissionResult} containing the
         *        test results to display
         */
        private void displayTestResults(final SubmissionResult submissionResult) {
            // add a simplified version of the TestResults for display in our table
            addSessionObject(PlaygroundTestResult.convertTestResult(submissionResult.getTestResults()));
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
		return new Class<?>[0]; // FIXME
	}

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
     */
    @Override
    public void activate() {
        addSessionObject(new ChangeList());
        // create default test case
        TestCase t1=new TestCase();
        t1.setInput("");
        t1.setTestCaseId(1);
        t1.setProblemId(1);
        t1.setOutput("");
        TestCase[] testCases=new TestCase[1];
        testCases[0]=t1;
        addSessionObject(testCases);
        
        addSessionObject(PlaygroundTestResult.convertTestCase(testCases));
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
        return PageId.PLAYGROUND_PAGE;
    }

    @Override
    public void initDefaultPageStack(PageStack pageStack) {
        pageStack.push(PageId.PLAYGROUND_PAGE);
    }
}
