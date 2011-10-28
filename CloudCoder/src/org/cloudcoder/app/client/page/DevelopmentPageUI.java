package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

public class DevelopmentPageUI extends Composite {
	private ProblemDescriptionView problemDescriptionView;
	private LayoutPanel southLayoutPanel;
	private LayoutPanel centerLayoutPanel;
	
	private AceEditor aceEditor;
	
	public DevelopmentPageUI() {
		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		dockLayoutPanel.setSize("800px", "600px");
		
		problemDescriptionView = new ProblemDescriptionView();
		dockLayoutPanel.addNorth(problemDescriptionView, 7.7);
		
		southLayoutPanel = new LayoutPanel();
		dockLayoutPanel.addSouth(southLayoutPanel, 7.7);
		
		centerLayoutPanel = new LayoutPanel();
		dockLayoutPanel.add(centerLayoutPanel);
		
		
		initWidget(dockLayoutPanel);
	}
	
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
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
		
		aceEditor.setReadOnly(false);
	}
}
