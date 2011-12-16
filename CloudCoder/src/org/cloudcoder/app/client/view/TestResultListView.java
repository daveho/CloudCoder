package org.cloudcoder.app.client.view;

import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;

public class TestResultListView extends Composite implements SessionObserver, Subscriber {
	private CellTable<TestResult> cellTable;
	
	public TestResultListView() {
		cellTable = new CellTable<TestResult>();
		cellTable.setSize("100%", "100%");
		
		cellTable.addColumn(new OutcomeColumn(), "Outcome");
		cellTable.addColumn(new MessageColumn(), "Message");
		cellTable.addColumn(new OutputColumn(), "Output");
		cellTable.addColumn(new ErrorOutputColumn(), "Error output");
		
		initWidget(cellTable);
	}
	
	private static class OutcomeColumn extends TextColumn<TestResult> {
		@Override
		public String getValue(TestResult object) {
			return object.getOutcome().toString().toLowerCase();
		}
	}
	
	private static class MessageColumn extends TextColumn<TestResult> {
		@Override
		public String getValue(TestResult object) {
			return object.getMessage();
		}
	}
	
	private static class OutputColumn extends TextColumn<TestResult> {
		@Override
		public String getValue(TestResult object) {
			return object.getStdout();
		}
	}
	
	private static class ErrorOutputColumn extends TextColumn<TestResult> {
		@Override
		public String getValue(TestResult object) {
			return object.getStderr();
		}
	}
	
	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		TestResult[] testResultList = session.get(TestResult[].class);
		if (testResultList != null) {
			displayTestResults(testResultList);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof TestResult[]) {
			displayTestResults((TestResult[]) hint);
		}
	}
	
	private void displayTestResults(TestResult[] testResultList) {
		cellTable.setRowCount(testResultList.length);
		List<TestResult> list = Arrays.asList(testResultList);
		cellTable.setRowData(list);
	}
}
