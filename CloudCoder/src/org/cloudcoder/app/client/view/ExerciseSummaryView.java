// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, Shane Bonner
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
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Exercise summary view: provides a quick visual summary of
 * completed and incomplete exercises.
 * 
 * @author Shane Bonner
 * @author David Hovemeyer
 */
public class ExerciseSummaryView extends Composite implements Subscriber, SessionObserver{
	private Session session;
	private FlowPanel flowPanel;
	private ICallback<ExerciseSummaryItem> clickHandler;

	/**
	 * Constructor.
	 */
	public ExerciseSummaryView() {
		// This widget is basically a div containing ExerciseSummaryItems
		this.flowPanel = new FlowPanel();
		flowPanel.setStyleName("cc-exerciseSummary", true);

		// Allow it to scroll
		ScrollPanel wrap = new ScrollPanel();
		wrap.add(flowPanel);
		
		initWidget(wrap);
		
		// Create a callback to handle item clicks
		this.clickHandler = new ICallback<ExerciseSummaryItem>() {
			@Override
			public void call(ExerciseSummaryItem value) {
				handleItemClick(value);
			}
		};
	}

	protected void handleItemClick(ExerciseSummaryItem value) {
		// Add the Problem to the session (to select the problem)
		ProblemAndSubmissionReceipt p = value.getProblemAndSubmissionReceipt();
		session.add(p.getProblem());
	}

	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
	{
		this.session = session;
		
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		ProblemAndSubmissionReceipt[] problemAndSubmissionReceipts = session.get(ProblemAndSubmissionReceipt[].class);
		if (problemAndSubmissionReceipts != null) {
			loadData(problemAndSubmissionReceipts);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof ProblemAndSubmissionReceipt[]) {
			loadData((ProblemAndSubmissionReceipt[]) hint);
		}
	}

	/**
	 * @param problemAndSubmissionReceipts
	 */
	private void loadData(ProblemAndSubmissionReceipt[] problemAndSubmissionReceipts) {
		// clear current data, load new data
		flowPanel.clear();
		
		// Sort by due date
		ProblemAndSubmissionReceipt[] list = new ProblemAndSubmissionReceipt[problemAndSubmissionReceipts.length]; //problemAndSubmissionReceipts.clone();
		System.arraycopy(problemAndSubmissionReceipts, 0, list, 0, problemAndSubmissionReceipts.length);
		ViewUtil.sortProblemsByDueDate(list);
		
		//loop through the problem and submission receipts,
		//create exerciseSummaryItem for each, and set each box's status (completed, failed, etc)
		
		for (ProblemAndSubmissionReceipt item : list){
			// Create ExerciseSummaryItem
			ExerciseSummaryItem summaryItem = new ExerciseSummaryItem();
			summaryItem.setProblemAndSubmissionReceipt(item);
			summaryItem.setClickHandler(clickHandler);
			flowPanel.add(summaryItem);
		}
	}

}
