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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Exercise summary view: provides a quick visual summary of
 * completed and incomplete exercises.
 * 
 * @author Shane Bonner
 * @author David Hovemeyer
 */
public class ExerciseSummaryView extends Composite implements Subscriber, SessionObserver{
	private FlowPanel flowPanel;
	private List<ExerciseSummaryItem> itemList;

	/**
	 * Constructor.
	 */
	public ExerciseSummaryView() {
		itemList = new ArrayList<ExerciseSummaryItem>();
		
		this.flowPanel = new FlowPanel();
		flowPanel.setStyleName("cc-exerciseSummary", true);
		initWidget(flowPanel);
	}

	public void addExerciseSummaryItem(ExerciseSummaryItem item) {
		itemList.add(item);
		flowPanel.add(item);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
	{
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
		// TODO Auto-generated method stub
		// clear current data, load new data
		itemList.clear();
		flowPanel.clear();
		
		//loop through the problem and submission receipt, HOW DO I ACESS THIS?
		//create exerciseSummaryItem for each, and set each box's status (completed, failed, etc)
		
		for(int i = 0; i < problemAndSubmissionReceipts.length; i++){
			ProblemAndSubmissionReceipt item = problemAndSubmissionReceipts[i];
			SubmissionReceipt receipt = item.getReceipt();
			SubmissionStatus status = receipt != null ? receipt.getStatus() : SubmissionStatus.NOT_STARTED;
			
			ExerciseSummaryItem summaryItem = new ExerciseSummaryItem();
			summaryItem.setStyleName("cc-exerciseSummaryItem", true);
			
			//determine status, create ExerciseSummaryItem and use method addExerciseSummaryItem
			
			switch (status) {
			case NOT_STARTED:
				summaryItem.setStyleName("cc-exerciseNotStarted", true); break;
			case STARTED:
				summaryItem.setStyleName("cc-exerciseStarted", true); break;
			case TESTS_FAILED:
				summaryItem.setStyleName("cc-exerciseTestsFailed", true); break;
			case COMPILE_ERROR:
				summaryItem.setStyleName("cc-exerciseCompileError", true); break;
			case BUILD_ERROR:
				summaryItem.setStyleName("cc-exerciseBuildError", true); break;
			case TESTS_PASSED:
				summaryItem.setStyleName("cc-exerciseTestsPassed", true); break;
			default:
				summaryItem.setStyleName("cc-exerciseStatusUnknown", true); break;
			}
			addExerciseSummaryItem(summaryItem);
		}
	}

}
