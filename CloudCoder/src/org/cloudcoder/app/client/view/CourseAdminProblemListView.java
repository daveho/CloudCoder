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

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CourseAdminPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * View to show problems in the {@link CourseAdminPage}.
 * 
 * @author David Hovemeyer
 */
public class CourseAdminProblemListView extends ResizeComposite implements Subscriber, SessionObserver {
	private DataGrid<Problem> grid;
	
	/**
	 * Constructor.
	 */
	public CourseAdminProblemListView() {
		grid = new DataGrid<Problem>();
		grid.addColumn(new ProblemNameColumn(), "Name");
		grid.addColumn(new ProblemBriefDescriptionColumn(), "Description");
		grid.addColumn(new ProblemTypeColumn(), "Type");
		grid.addColumn(new ProblemWhenAssignedColumn(), "Assigned");
		grid.addColumn(new ProblemWhenDueColumn(), "Due");
		grid.addColumn(new ProblemVisibleColumn(), "Visible");
		initWidget(grid);
	}
	
	private static class ProblemNameColumn extends TextColumn<Problem> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Problem object) {
			return object.getTestName();
		}
	}
	
	private static class ProblemBriefDescriptionColumn extends TextColumn<Problem> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Problem object) {
			return object.getBriefDescription();
		}
	}
	
	private static class ProblemTypeColumn extends TextColumn<Problem> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Problem object) {
			return object.getProblemType().toString();
		}
	}
	
	private static class ProblemWhenAssignedColumn extends TextColumn<Problem> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Problem object) {
			return ViewUtil.formatDate(object.getWhenAssignedAsDate());
		}
	}
	
	private static class ProblemWhenDueColumn extends TextColumn<Problem> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Problem object) {
			return ViewUtil.formatDate(object.getWhenDueAsDate());
		}
	}
	
	private static class ProblemVisibleColumn extends TextColumn<Problem> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Problem object) {
			// FIXME: there is no visible attribute in Problem, need to add one
			return "true";
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
		// If the session contains a list of ProblemAndSubmissionReceipts, display the problems.
		// Otherwise, initiate loading of problems for course.
		ProblemAndSubmissionReceipt[] problemAndSubmissionReceiptList = session.get(ProblemAndSubmissionReceipt[].class);
		if (problemAndSubmissionReceiptList != null) {
			displayProblems(problemAndSubmissionReceiptList);
		} else {
			Course course = session.get(Course.class);
			RPC.getCoursesAndProblemsService.getProblems(course, new AsyncCallback<Problem[]>() {
				@Override
				public void onSuccess(Problem[] result) {
					displayProblems(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					session.add(new StatusMessage(StatusMessage.Category.ERROR, "Could not load problems for course"));
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		// TODO Auto-generated method stub
		
	}

	protected void displayProblems(ProblemAndSubmissionReceipt[] problemAndSubmissionReceiptList) {
		Problem[] problems = new Problem[problemAndSubmissionReceiptList.length];
		int count = 0;
		for (ProblemAndSubmissionReceipt p : problemAndSubmissionReceiptList) {
			problems[count++] = p.getProblem();
		}
		displayProblems(problems);
	}

	protected void displayProblems(Problem[] result) {
		grid.setRowCount(result.length);
		grid.setRowData(Arrays.asList(result));
	}
}
