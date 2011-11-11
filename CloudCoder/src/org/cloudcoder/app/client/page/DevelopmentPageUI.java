package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.ChangeFromAceOnChangeEvent;
import org.cloudcoder.app.client.model.ChangeList;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.DevActionsPanel;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.TestResultListView;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

public class DevelopmentPageUI extends Composite implements CloudCoderPageUI, Subscriber {
	public static final double SOUTH_PANEL_HEIGHT_EM = 15.0;
	public static final double NORTH_PANEL_HEIGHT_EM = 7.7;
	public static final int FLUSH_CHANGES_INTERVAL_MS = 2000;
	
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

	private LayoutPanel northLayoutPanel;
	private ProblemDescriptionView problemDescriptionView;
	private PageNavPanel pageNavPanel;
	private DevActionsPanel devActionsPanel;
	private LayoutPanel southLayoutPanel;
	private LayoutPanel centerLayoutPanel;
	private LayoutPanel buttonsLayoutPanel;
	private TestResultListView testResultListView;

	private CloudCoderPage page;
	private AceEditor aceEditor;
	private Timer flushPendingChangeEventsTimer;
	private Mode mode;

	public DevelopmentPageUI() {
		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		//dockLayoutPanel.setSize("800px", "600px");
		//dockLayoutPanel.setSize("100%", "100%");

		northLayoutPanel = new LayoutPanel();
		dockLayoutPanel.addNorth(northLayoutPanel, NORTH_PANEL_HEIGHT_EM);
		problemDescriptionView = new ProblemDescriptionView();
		northLayoutPanel.add(problemDescriptionView);
		northLayoutPanel.setWidgetLeftRight(problemDescriptionView, 0.0, Unit.PX, 350.0, Unit.PX);
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
		northLayoutPanel.setWidgetRightWidth(buttonsLayoutPanel, 0.0, Unit.PX, 350.0, Unit.PX);
		northLayoutPanel.setWidgetTopHeight(buttonsLayoutPanel, 0.0, Unit.PX, NORTH_PANEL_HEIGHT_EM, Unit.EM);

		southLayoutPanel = new LayoutPanel();
		this.testResultListView = new TestResultListView();
		southLayoutPanel.add(testResultListView);
		southLayoutPanel.setWidgetLeftRight(testResultListView, 0.0, Unit.PX, 0.0, Unit.PX);
		southLayoutPanel.setWidgetTopBottom(testResultListView, 0.0, Unit.PX, 0.0, Unit.PX);
		dockLayoutPanel.addSouth(southLayoutPanel, SOUTH_PANEL_HEIGHT_EM);

		centerLayoutPanel = new LayoutPanel();
		dockLayoutPanel.add(centerLayoutPanel);

		initWidget(dockLayoutPanel);
	}
	
	@Override
	public void setPage(CloudCoderPage page) {
		this.page = page;
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		final Problem problem = session.get(Problem.class);

		mode = Mode.LOADING;
		
		// Activate problem description view
		problemDescriptionView.activate(session, subscriptionRegistrar);
		
		// Activate TestResultListView
		testResultListView.activate(session, subscriptionRegistrar);
		
		// Subscribe to ChangeList events
		session.get(ChangeList.class).subscribe(ChangeList.State.CLEAN, this, subscriptionRegistrar);

		// Create AceEditor instance
		createEditor();

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
		if (hint == ChangeList.State.CLEAN && mode == Mode.SUBMIT_PENDING_CLEAN_CHANGE_LIST) {
			// The change list just became clean - initiate the submit
			mode = Mode.SUBMIT_IN_PROGRESS;
			doSubmit();
		}
	}

	public void doSubmit() {
		// Full text of submission has arrived at server,
		// and because the editor is read-only, we know that the
		// local text is in-sync.  So, submit the code!

		Session session = page.getSession();
		Problem problem = session.get(Problem.class);
		String text = aceEditor.getText();
		RPC.submitService.submit(problem.getProblemId(), text, new AsyncCallback<TestResult[]>() {
			@Override
			public void onFailure(Throwable caught) {
				final String msg = "Error sending submission to server for compilation"; 
//					getSession().add(new StatusMessage(StatusMessage.Category.ERROR, msg));
				GWT.log(msg, caught);
				// TODO: should set editor back to read/write?
			}

			@Override
			public void onSuccess(TestResult[] results) {
				// Great, got results back from server!
				page.getSession().add(results);
				
				// Add a status message about the results
//					page.getSession().add(new StatusMessage(
//							StatusMessage.Category.INFORMATION, "Received " + results.length + " test result(s)"));
				
				// Can resume editing now
//					startEditing();
				mode = Mode.EDITING;
				aceEditor.setReadOnly(false);
			}
		});
	}

	public void createEditor() {
		aceEditor = new AceEditor();
		aceEditor.setSize("100%", "100%");
		centerLayoutPanel.add(aceEditor);
		aceEditor.startEditor();
		aceEditor.setMode(AceEditorMode.JAVA);
		aceEditor.setTheme(AceEditorTheme.TWILIGHT);
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
		RPC.editCodeService.loadCurrentText(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				aceEditor.setText(result);
				aceEditor.setReadOnly(false);
				mode = Mode.EDITING;
				
				// Workaround for GWT/ACE weirdness: the editor contents do not render
				// correctly (they appear blank.)  Manually resizing the window causes
				// the correct contents to appear, and so does setting the AceEditor
				// font size.
				aceEditor.setFontSize("14px");
			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO: display error
				GWT.log("Couldn't get current text for problem", caught);
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

							//session.add(new StatusMessage(StatusMessage.Category.ERROR, "Could not save code to server!"));
						}

						@Override
						public void onSuccess(Boolean result) {
							changeList.endTransmit(true);
						}
					};

					RPC.editCodeService.logChange(changeBatch, callback);
				}
			}
		};
		flushPendingChangeEventsTimer.scheduleRepeating(FLUSH_CHANGES_INTERVAL_MS);
	}
}
