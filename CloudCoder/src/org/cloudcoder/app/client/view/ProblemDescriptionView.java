package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

public class ProblemDescriptionView extends Composite implements SessionObserver, Subscriber {
	private Label problemNameLabel;
	private HTML problemDescriptionHtml;

	public ProblemDescriptionView() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		problemNameLabel = new Label("");
		problemNameLabel.setStyleName("cc-problemName");
		layoutPanel.add(problemNameLabel);
		problemNameLabel.setWidth("100%");
		layoutPanel.setWidgetLeftWidth(problemNameLabel, 0.0, Unit.PX, 302.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(problemNameLabel, 0.0, Unit.PX, 24.0, Unit.PX);
		
		problemDescriptionHtml = new HTML("", true);
		layoutPanel.add(problemDescriptionHtml);
		problemDescriptionHtml.setWidth("100%");
		layoutPanel.setWidgetLeftWidth(problemDescriptionHtml, 0.0, Unit.PX, 436.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(problemDescriptionHtml, 30.0, Unit.PX, 70.0, Unit.PX);
		
		initWidget(layoutPanel);
	}

	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		Problem problem = session.get(Problem.class);
		if (problem != null) {
			displayProblemDescription(problem);
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof Problem) {
			Problem problem = (Problem) hint;
			displayProblemDescription(problem);
		}
	}

	public void displayProblemDescription(Problem problem) {
		problemNameLabel.setText(problem.getTestName() + " - " + problem.getBriefDescription());
		problemDescriptionHtml.setHTML(SafeHtmlUtils.fromString(problem.getDescription()));
	}
}
