package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubscriptionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class ProblemListView2 extends Composite implements SessionObserver, Subscriber {
	private Session session;
	private DataGrid<ProblemAndSubscriptionReceipt> cellTable;

	public ProblemListView2() {
		cellTable = new DataGrid<ProblemAndSubscriptionReceipt>();
		
		// Configure the DataGrid that will show the problems
		cellTable.addColumn(new TestNameColumn(), "Name");
		cellTable.addColumn(new BriefDescriptionColumn(), "Description");
		
		initWidget(cellTable);
	}

	private static class TestNameColumn extends TextColumn<ProblemAndSubscriptionReceipt> {
		@Override
		public String getValue(ProblemAndSubscriptionReceipt object) {
			return object.getProblem().getTestName();
		}
	}

	private static class BriefDescriptionColumn extends TextColumn<ProblemAndSubscriptionReceipt> {
		@Override
		public String getValue(ProblemAndSubscriptionReceipt object) {
			return object.getProblem().getBriefDescription();
		}
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
		// Subscribe to session ADDED_OBJECT events (so we will see when a course is selected)
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

		// When a problem is selected, add it to the session
		final SingleSelectionModel<ProblemAndSubscriptionReceipt> selectionModel = new SingleSelectionModel<ProblemAndSubscriptionReceipt>();
		cellTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ProblemAndSubscriptionReceipt selected = selectionModel.getSelectedObject();
				if (selected != null) {
					// Add the problem to the Session
					// TODO: add ProblemAndSubmissionReceipt
					session.add(selected.getProblem());
				}
			}
		});
		
		// If there is already a Course selected, load its problems
		Course course = session.get(Course.class);
		ProblemAndSubscriptionReceipt[] problemList = session.get(ProblemAndSubscriptionReceipt[].class);
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
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, new AsyncCallback<ProblemAndSubscriptionReceipt[]>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error loading problems for course", caught);
				session.add(new StatusMessage(StatusMessage.Category.ERROR, "Error loading problems for course: " + caught.getMessage()));
			}

			@Override
			public void onSuccess(ProblemAndSubscriptionReceipt[] result) {
				displayLoadedProblems(result);
			}
		});
	}

	private void displayLoadedProblems(ProblemAndSubscriptionReceipt[] problemList) {
		cellTable.setRowCount(problemList.length);
		cellTable.setRowData(0, Arrays.asList(problemList));
	}
}
