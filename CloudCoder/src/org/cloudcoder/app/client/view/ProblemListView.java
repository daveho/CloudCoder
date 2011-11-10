package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class ProblemListView extends Composite implements SessionObserver, Subscriber {
	private DataGrid<Problem> cellTable;

	public ProblemListView() {
		cellTable = new DataGrid<Problem>();
		
		initWidget(cellTable);
	}

	private static class TestNameColumn extends TextColumn<Problem> {
		@Override
		public String getValue(Problem object) {
			return object.getTestName();
		}
	}

	private static class BriefDescriptionColumn extends TextColumn<Problem> {
		@Override
		public String getValue(Problem object) {
			return object.getBriefDescription();
		}
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		// Subscribe to session ADDED_OBJECT events (so we will see when a course is selected)
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		// Configure the DataGrid that will show the problems
		cellTable.addColumn(new TestNameColumn(), "Name");
		cellTable.addColumn(new BriefDescriptionColumn(), "Description");

		// When a problem is selected, add it to the session
		final SingleSelectionModel<Problem> selectionModel = new SingleSelectionModel<Problem>();
		cellTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				Problem problem = selectionModel.getSelectedObject();
				if (problem != null) {
					// Add the problem to the Session
					session.add(problem);
				}
			}
		});
		
		// If there is already a Course selected, load its problems
		Course course = session.get(Course.class);
		Problem[] problemList = session.get(Problem[].class);
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
		RPC.getCoursesAndProblemsService.getProblems(course, new AsyncCallback<Problem[]>() {
			@Override
			public void onFailure(Throwable caught) {
				// FIXME: status message
			}

			@Override
			public void onSuccess(Problem[] result) {
				displayLoadedProblems(result);
			}
		});
	}

	private void displayLoadedProblems(Problem[] problemList) {
		cellTable.setRowCount(problemList.length);
		cellTable.setRowData(0, Arrays.asList(problemList));
	}
}
