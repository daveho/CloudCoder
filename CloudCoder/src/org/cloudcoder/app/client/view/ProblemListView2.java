// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for ProblemAndSubmissionReceipts.
 * It serves as a summary of which problems are available in a particular
 * Course. It also displays a status with each problem (whether or not the
 * problem has been started, completed, etc.)
 * 
 * @author David Hovemeyer
 */
public class ProblemListView2 extends Composite implements SessionObserver, Subscriber {
	private Session session;
	private DataGrid<ProblemAndSubmissionReceipt> cellTable;

	public ProblemListView2() {
		cellTable = new DataGrid<ProblemAndSubmissionReceipt>();
		
		// Configure the DataGrid that will show the problems
		cellTable.addColumn(new TestNameColumn(), "Name");
		cellTable.addColumn(new BriefDescriptionColumn(), "Description");
		cellTable.addColumn(new WhenAssignedColumn(), "Assigned");
		cellTable.addColumn(new WhenDueColumn(), "Due");
		cellTable.addColumn(new SubmissionStatusColumn(), "Status");
		
		initWidget(cellTable);
	}

	private static class TestNameColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return object.getProblem().getTestName();
		}
	}

	private static class BriefDescriptionColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return object.getProblem().getBriefDescription();
		}
	}
	
	private static class WhenAssignedColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			Date whenAssigned = object.getProblem().getWhenAssignedAsDate();
			//SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy mm:hh a");
			DateTimeFormat f = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
			return f.format(whenAssigned);
		}
	}
	
	private static class WhenDueColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			Date whenDue = object.getProblem().getWhenDueAsDate();
//			SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy mm:hh a");
			DateTimeFormat f = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
			return f.format(whenDue);
		}
	}
	
	private static class SubmissionStatusColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			SubmissionStatus status = (object.getReceipt() != null) ? object.getReceipt().getStatus() : SubmissionStatus.NOT_STARTED;
			return status.toString();
		}
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
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
		
		// If there is already a Course selected, load its problems
		Course course = session.get(Course.class);
		ProblemAndSubmissionReceipt[] problemList = session.get(ProblemAndSubmissionReceipt[].class);
		if (course != null) {
			loadProblemsForCourse(course);
		} else if (problemList != null) {
			displayLoadedProblems(problemList);
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && (hint instanceof Course)) {
			// A Course has been selected: load its problems
			loadProblemsForCourse((Course) hint);
		}
	}

	public void loadProblemsForCourse(Course course) {
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error loading problems for course", caught);
				session.add(new StatusMessage(StatusMessage.Category.ERROR, "Error loading problems for course: " + caught.getMessage()));
			}

			@Override
			public void onSuccess(ProblemAndSubmissionReceipt[] result) {
				displayLoadedProblems(result);
			}
		});
	}

	private void displayLoadedProblems(ProblemAndSubmissionReceipt[] problemList) {
		cellTable.setRowCount(problemList.length);
		cellTable.setRowData(0, Arrays.asList(problemList));
	}
}
