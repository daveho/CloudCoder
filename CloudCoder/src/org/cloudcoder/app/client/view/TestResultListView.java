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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.user.cellview.client.Column;
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
		
		cellTable.addColumn(new OutcomeColumn(), "Outcome");
		cellTable.addColumn(new MessageColumn(), "Message");
		cellTable.addColumn(new OutputColumn(new ExtractOutputText() {
			@Override
			public String getOutputText(TestResult testResult) {
				return testResult.getStdout();
			}
		}), "Output");
		cellTable.addColumn(new OutputColumn(new ExtractOutputText() {
			@Override
			public String getOutputText(TestResult testResult) {
				return testResult.getStderr();
			}
		}), "Error output");
		
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
	
	private static abstract class ShowFullOutputButtonColumn extends Column<TestResult, String> {
		public ShowFullOutputButtonColumn() {
			super(new ButtonCell());

			// Set a FieldUpdater to handle the button click
			setFieldUpdater(new FieldUpdater<TestResult, String>() {
				@Override
				public void update(int index, TestResult object, String value) {
					// Show the TestResultOutputDialog.
					TestResultOutputDialog dialog = new TestResultOutputDialog(getText(object));
					dialog.center();
				}
			});
		}

		protected abstract String getText(TestResult object);

		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(TestResult object) {
			return "Show all";
		}
	}
	
	private static abstract class OutputFirstLineColumn extends TextColumn<TestResult> {
		@Override
		public String getValue(TestResult object) {
			return firstLine(getText(object));
		}

		protected abstract String getText(TestResult object);
		
		private static String firstLine(String s) {
			int eol = s.indexOf('\n');
			return (eol < 0) ? s : s.substring(0, eol);
		}
	}
	
//	private static class ErrorOutputColumn extends TextColumn<TestResult> {
//		@Override
//		public String getValue(TestResult object) {
//			return object.getStderr();
//		}
//	}
	
	private interface ExtractOutputText {
		public String getOutputText(TestResult testResult);
	}
	
	private static class OutputColumn extends Column<TestResult, TestResult> {
		public OutputColumn(ExtractOutputText extractor) {
			super(new CompositeCell<TestResult>(getCells(extractor)));
		}
		
		private static List<HasCell<TestResult, ?>> getCells(final ExtractOutputText extractor) {
			List<HasCell<TestResult, ?>> result = new ArrayList<HasCell<TestResult, ?>>();
			result.add(new ShowFullOutputButtonColumn(){
				/* (non-Javadoc)
				 * @see org.cloudcoder.app.client.view.TestResultListView.ShowFullOutputButtonColumn#getText(org.cloudcoder.app.shared.model.TestResult)
				 */
				@Override
				protected String getText(TestResult object) {
					return extractor.getOutputText(object);
				}
			});
			result.add(new OutputFirstLineColumn() {
				/* (non-Javadoc)
				 * @see org.cloudcoder.app.client.view.TestResultListView.OutputFirstLineColumn#getText(org.cloudcoder.app.shared.model.TestResult)
				 */
				@Override
				protected String getText(TestResult object) {
					return extractor.getOutputText(object);
				}
			});
			return result;
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public TestResult getValue(TestResult object) {
			return object;
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
