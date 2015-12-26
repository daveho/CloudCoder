// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.page.SessionUtil;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for ProblemAndSubmissionReceipts.
 * It serves as a summary of which problems are available in a particular
 * Course. It also displays a status with each problem (whether or not the
 * problem has been started, completed, etc.)  This is a simplified
 * version of the previous view (ProblemListView2) which attempts to
 * eliminate extraneous details and present the information in
 * a clearer manner.
 * 
 * @author David Hovemeyer
 */
public class ProblemListView3 extends ResizeComposite implements SessionObserver, Subscriber {
	private CloudCoderPage page;
	private DataGrid<ProblemAndSubmissionReceipt> cellTable;

	/**
	 * Constructor.
	 * 
	 * @param page the {@link CloudCoderPage} that will contain this view
	 */
	public ProblemListView3(CloudCoderPage page) {
		this.page = page;
		
		cellTable = new DataGrid<ProblemAndSubmissionReceipt>();
		
		// Configure the DataGrid that will show the problems
		cellTable.addColumn(new TestNameColumn(), "Name");
		cellTable.addColumn(new WhenDueColumn(), "Due");
		cellTable.addColumn(new SubmissionStatusColumn(), "Status");
		
		initWidget(cellTable);
	}

	private static class TestNameColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return object.getProblem().getTestname();
		}
	}
	
	private static class WhenDueColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return ViewUtil.formatDate(object.getProblem().getWhenDueAsDate());
		}
	}
	
	private static class SubmissionStatusColumn extends Column<ProblemAndSubmissionReceipt, ProblemAndSubmissionReceipt> {
		public SubmissionStatusColumn() {
			super(new BestScoreBarCell<ProblemAndSubmissionReceipt>());
		}

		@Override
		public ProblemAndSubmissionReceipt getValue(ProblemAndSubmissionReceipt object) {
			return object;
		}
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		// Subscribe to session ADDED_OBJECT events (so we will see when a course is selected)
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

		// When a problem is selected, add it to the session
		final SingleSelectionModel<ProblemAndSubmissionReceipt> selectionModel = new SingleSelectionModel<ProblemAndSubmissionReceipt>();
		cellTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ProblemAndSubmissionReceipt selected = selectionModel.getSelectedObject();
				if (selected != null) {
					// Add the problem to the Session
					session.add(selected.getProblem());
				}
			}
		});
		
		// Load the exercise when the user double-clicks
		cellTable.addDomHandler(new DoubleClickHandler() {
		    @Override
		    public void onDoubleClick(DoubleClickEvent event) { 
		        Problem problem = session.get(Problem.class);
	            if (problem != null) {
	                // Switch to DevelopmentPage
	                session.get(PageStack.class).push(PageId.DEVELOPMENT);
	            }
		    }    
		}, DoubleClickEvent.getType());
		
		// If there is already a Course selected, load its problems.
		// Otherwise, if there are problems already in the session, display them.
		CourseSelection courseSelection = session.get(CourseSelection.class);
		ProblemAndSubmissionReceipt[] problemList = session.get(ProblemAndSubmissionReceipt[].class);
		if (courseSelection != null) {
			loadProblemsForCourse(courseSelection);
		} else if (problemList != null) {
			displayLoadedProblems(problemList);
		}
	}
	
	/**
	 * Force a redisplay from contents of the {@link Session}.
	 * This is to help work around a bug where the view doesn't
	 * get updated properly if the course selection changes
	 * while the tab in which the view is displayed is
	 * not the current tab.
	 */
	public void redisplay() {
		ProblemAndSubmissionReceipt[] problemList =
				page.getSession().get(ProblemAndSubmissionReceipt[].class);
		if (problemList != null) {
			displayLoadedProblems(problemList);
			Problem selected = page.getSession().get(Problem.class);
			if (selected != null) {
				onProblemSelected(selected);
			}
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
			// A Course has been selected: load its problems
			CourseSelection courseSelection = (CourseSelection) hint;
			loadProblemsForCourse(courseSelection);
		} else if (key == Session.Event.ADDED_OBJECT && (hint instanceof ProblemAndSubmissionReceipt[])) {
			// The list of ProblemAndSubmissionReceipts has been reloaded.
			// This can happen because the current page has done something to change
			// the list (e.g., adding or removing a problem) for the current course.
			displayLoadedProblems((ProblemAndSubmissionReceipt[])hint);
		} else if (key == Session.Event.ADDED_OBJECT && hint instanceof Problem) {
			// The Problem selection has changed, update selection as appropriate
			onProblemSelected((Problem) hint);
		}
	}

	public void loadProblemsForCourse(final CourseSelection courseSelection) {
		GWT.log("Loading problems and submission receipts for course " + courseSelection.getCourse().getNameAndTitle());
		SessionUtil.loadProblemAndSubmissionReceiptsInCourse(page, courseSelection, page.getSession());
	}

	private void displayLoadedProblems(ProblemAndSubmissionReceipt[] problemList) {
		GWT.log("Displaying " + problemList.length + " problems/submission receipts");
		
		// Sort by due date
		ProblemAndSubmissionReceipt[] list = new ProblemAndSubmissionReceipt[problemList.length];
		System.arraycopy(problemList, 0, list, 0, problemList.length);
		ViewUtil.sortProblemsByDueDate(list);
		
		cellTable.setRowData(Arrays.asList(list));
	}

	private void onProblemSelected(Problem selectedProblem) {
		ProblemAndSubmissionReceipt[] data = page.getSession().get(ProblemAndSubmissionReceipt[].class);
		SingleSelectionModel<? super ProblemAndSubmissionReceipt> sm =
				(SingleSelectionModel<? super ProblemAndSubmissionReceipt>) cellTable.getSelectionModel();
		for (ProblemAndSubmissionReceipt p : data) {
			if (p.getProblem().getProblemId().equals(selectedProblem.getProblemId())) {
				// Found the selected problem, so change the selected row
				sm.clear();
				sm.setSelected(p, true);
				return;
			}
		}
		
		// The selected problem isn't being viewed currently,
		// so just clear the selection
		sm.clear();
	}
}
