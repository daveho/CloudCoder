// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, York College of Pennsylvania
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * View to display the problem description.
 * 
 * @author David Hovemeyer
 */
public class ProblemDescriptionView extends ResizeComposite implements SessionObserver, Subscriber {
	/** Preferred width of the ProblemDescriptionView. */
	public static final double DEFAULT_WIDTH_PX = 400.0;
	
	private HTML problemDescriptionHtml;

	public ProblemDescriptionView() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		problemDescriptionHtml = new HTML("", true);
		layoutPanel.add(problemDescriptionHtml);
		problemDescriptionHtml.setStyleName("cc-problemDescription");
		layoutPanel.setWidgetLeftRight(problemDescriptionHtml, 0.0, Unit.PX, 0.0, Unit.PX);
		layoutPanel.setWidgetTopBottom(problemDescriptionHtml, 0.0, Unit.PX, 0.0, Unit.PX);
		
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
		// Note: if the problem description contains HTML markup, it will
		// be rendered.  This is intentional, since it allows a greater degree
		// of control over formatting that just plain text would allow.
		StringBuilder buf = new StringBuilder();
		String description = problem.getDescription();
		
		// Add the description as specified in the problem.
		buf.append(description);
		
		// Add author information.
		buf.append("<div class=\"cc-authorInfo\">Author: <span class=\"cc-authorName\">");
		String authorName = problem.getAuthorName();
		String authorWebsite = problem.getAuthorWebsite();
		if (!authorWebsite.trim().equals("")) {
			// Format author name as link to author website
			buf.append("<a href=\"");
			buf.append(new SafeHtmlBuilder().appendEscaped(authorWebsite).toSafeHtml().asString());
			buf.append("\">");
			buf.append(new SafeHtmlBuilder().appendEscaped(authorName).toSafeHtml().asString());
			buf.append("</a>");
		} else {
			// No author website
			buf.append(new SafeHtmlBuilder().appendEscaped(authorName).toSafeHtml().asString());
		}
		// Add license information.
		buf.append("</span><br>License: <span class=\"cc-problemLicense\"><a href=\"");
		buf.append(problem.getLicense().getUrl());
		buf.append("\">");
		buf.append(problem.getLicense().getName());
		buf.append("</a></span>");
		
		buf.append("</span>");
		buf.append("</div>");
		
		problemDescriptionHtml.setHTML(buf.toString());
	}
}
