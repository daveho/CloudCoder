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
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;

/**
 * View displaying the list of TestResults.
 * 
 * @author David Hovemeyer
 */
public class TestResultListView extends Composite implements SessionObserver, Subscriber {
	private DataGrid<TestResult> cellTable;
	
	public TestResultListView() {
		cellTable = new DataGrid<TestResult>();
		
		// Odd bug: when width is set to 100%, the scroll bar extends
		// beyond the width of the parent.  So, make it a bit less than
		// 100% width.
		cellTable.setSize("98%", "100%");
		
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
//		cellTable.setRowCount(testResultList.length);
		cellTable.setRowData(Arrays.asList(testResultList));
	}
}
