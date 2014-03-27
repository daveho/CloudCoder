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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * View that shows the name and brief description of a {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class ProblemNameAndBriefDescriptionView extends Composite implements SessionObserver, Subscriber {
	public static final double HEIGHT_PX = PageNavPanel.HEIGHT_PX;
	
	private Label problemNameLabel;
	
	public ProblemNameAndBriefDescriptionView() {
		problemNameLabel = new Label("");
		problemNameLabel.setStyleName("cc-problemName");
		initWidget(problemNameLabel);
	}
	
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		Problem problem = session.get(Problem.class);
		if (problem != null) {
			displayProblemNameAndBriefDescription(problem);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof Problem) {
			displayProblemNameAndBriefDescription((Problem) hint);
		}
	}
	
	private void displayProblemNameAndBriefDescription(Problem problem) {
		problemNameLabel.setText(problem.getTestname() + " - " + problem.getBriefDescription());
	}
}
