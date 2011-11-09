package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;
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

public class DevelopmentPageUI extends Composite {
	public static final double NORTH_PANEL_HEIGHT = 7.7;
	public static final int FLUSH_CHANGES_INTERVAL_MS = 2000;
	
	private enum Mode {
		/** Loading problem and current text - editing not allowed. */
		LOADING,
		
		/** Normal state - user is allowed to edit the program text. */
		EDITING,
		
		/**
		 * Submit in progress.
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
	private LayoutPanel southLayoutPanel;
	private LayoutPanel centerLayoutPanel;
	private LayoutPanel buttonsLayoutPanel;

	private AceEditor aceEditor;
	private Timer flushPendingChangeEventsTimer;
	private Mode mode;

	public DevelopmentPageUI() {
		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		dockLayoutPanel.setSize("800px", "600px");
		//dockLayoutPanel.setSize("100%", "100%");

		northLayoutPanel = new LayoutPanel();
		dockLayoutPanel.addNorth(northLayoutPanel, NORTH_PANEL_HEIGHT);
		problemDescriptionView = new ProblemDescriptionView();
		northLayoutPanel.add(problemDescriptionView);
		northLayoutPanel.setWidgetLeftRight(problemDescriptionView, 0.0, Unit.PX, 350.0, Unit.PX);
		buttonsLayoutPanel = new LayoutPanel();
		pageNavPanel = new PageNavPanel();
		buttonsLayoutPanel.add(pageNavPanel);
		buttonsLayoutPanel.setWidgetLeftRight(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
		buttonsLayoutPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT);
		
		northLayoutPanel.add(buttonsLayoutPanel);
		northLayoutPanel.setWidgetRightWidth(buttonsLayoutPanel, 0.0, Unit.PX, 350.0, Unit.PX);
		northLayoutPanel.setWidgetTopHeight(buttonsLayoutPanel, 0.0, Unit.PX, NORTH_PANEL_HEIGHT, Unit.EM);

		southLayoutPanel = new LayoutPanel();
		dockLayoutPanel.addSouth(southLayoutPanel, 10.0);

		centerLayoutPanel = new LayoutPanel();
		dockLayoutPanel.add(centerLayoutPanel);

		initWidget(dockLayoutPanel);
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		mode = Mode.LOADING;
		
		session.subscribeToAll(Session.Event.values(), problemDescriptionView, subscriptionRegistrar);
		// FIXME: need better way to connect view to Problem
		session.notifySubscribers(Session.Event.ADDED_OBJECT, session.get(Problem.class));

		// Create AceEditor instance
		aceEditor = new AceEditor();
		aceEditor.setSize("100%", "100%");
		centerLayoutPanel.add(aceEditor);
		aceEditor.startEditor();
		aceEditor.setMode(AceEditorMode.JAVA);
		aceEditor.setTheme(AceEditorTheme.ECLIPSE);

		// editor will be readonly until problem text is loaded
		aceEditor.setReadOnly(true);

		// add a handler for editor change events
		final User user = session.get(User.class);
		final Problem problem = session.get(Problem.class);
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
		
		// Tell the server which problem we want to work on
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
