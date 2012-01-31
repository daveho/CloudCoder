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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * View to display the problem description.
 * 
 * @author David Hovemeyer
 */
public class ProblemDescriptionView extends Composite implements SessionObserver, Subscriber {
	/** The preferred height of the ProblemDescriptionView. */
	public static final double HEIGHT_PX = 175.0;
	
	private Label problemNameLabel;
	private HTML problemDescriptionHtml;

	public ProblemDescriptionView() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		problemNameLabel = new Label("");
		problemNameLabel.setStyleName("cc-problemName");
		layoutPanel.add(problemNameLabel);
		layoutPanel.setWidgetLeftRight(problemNameLabel, 0.0, Unit.PX, 0.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(problemNameLabel, 0.0, Unit.PX, 24.0, Unit.PX);
		
		problemDescriptionHtml = new HTML("", true);
		layoutPanel.add(problemDescriptionHtml);
//		problemDescriptionHtml.setWidth("100%");
		problemDescriptionHtml.setStyleName("cc-problemDescription");
		layoutPanel.setWidgetLeftRight(problemDescriptionHtml, 0.0, Unit.PX, 0.0, Unit.PX);
		layoutPanel.setWidgetTopBottom(problemDescriptionHtml, 30.0, Unit.PX, 0.0, Unit.PX);
		
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
		
		// Note: if the problem description contains HTML markup, it will
		// be rendered.  This is intentional, since it allows a greater degree
		// of control over formatting that just plain text would allow.
		problemDescriptionHtml.setHTML(problem.getDescription());
	}
}
