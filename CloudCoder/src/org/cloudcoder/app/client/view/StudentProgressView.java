// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

/**
 * Table view for summarizing student progress.
 * Views the session's array of {@link UserAndSubmissionReceipt} objects,
 * where the submission receipts are the students best submission for
 * a particular problem.
 * 
 * @author David Hovemeyer
 */
public class StudentProgressView extends Composite implements Subscriber, SessionObserver {
	private DataGrid<UserAndSubmissionReceipt> grid;
	private Problem problem;
	private TestCase[] testCaseList;
	private UserAndSubmissionReceipt[] data;
	
	public StudentProgressView() {
		data = new UserAndSubmissionReceipt[0];

		grid = new DataGrid<UserAndSubmissionReceipt>();
		grid.addColumn(new UsernameColumn(), "Username");
		grid.addColumn(new FirstnameColumn(), "First name");
		grid.addColumn(new LastnameColumn(), "Last name");
		grid.addColumn(new StartedColumn(), "Started");
		grid.addColumn(new BestScoreColumn(), "Best score");
		grid.addColumn(new BestScoreBarColumn(), "Best score bar");
		
		initWidget(grid);
	}
	
	private class UsernameColumn extends TextColumn<UserAndSubmissionReceipt> {
		@Override
		public String getValue(UserAndSubmissionReceipt object) {
			return object.getUser().getUsername();
		}
	}
	
	private class FirstnameColumn extends TextColumn<UserAndSubmissionReceipt> {
		@Override
		public String getValue(UserAndSubmissionReceipt object) {
			return object.getUser().getFirstname();
		}
	}
	
	private class LastnameColumn extends TextColumn<UserAndSubmissionReceipt> {
		@Override
		public String getValue(UserAndSubmissionReceipt object) {
			return object.getUser().getLastname();
		}
	}
	
	private class StartedColumn extends TextColumn<UserAndSubmissionReceipt> {
		@Override
		public String getValue(UserAndSubmissionReceipt object) {
			return object.getSubmissionReceipt() == null ? "false" : "true";
		}
	}
	
	private class BestScoreColumn extends TextColumn<UserAndSubmissionReceipt> {
		public String getValue(UserAndSubmissionReceipt object) {
			StringBuilder buf = new StringBuilder();

			if (object.getSubmissionReceipt() != null) {
				buf.append(object.getSubmissionReceipt().getNumTestsPassed());
			} else {
				buf.append(0);
			}
			
			if (testCaseList != null) {
				buf.append("/");
				buf.append(testCaseList.length);
			}
			return buf.toString();
		}
	}
	
	private class BestScoreBarCell extends AbstractCell<Integer> {
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, Integer value, SafeHtmlBuilder sb) {
			if (value == null) {
				appendNoInfoBar(sb);
				return;
			}
			
			// If we don't know the total number of test cases, don't display anything
			if (testCaseList == null || testCaseList.length == 0) {
				appendNoInfoBar(sb);
				return;
			}
			
			StringBuilder buf = new StringBuilder();
			buf.append("<div class=\"cc-barOuter\"><div class=\"cc-barInner\" style=\"width: ");
			int pct = (value * 10000) / (testCaseList.length * 100);
			buf.append(pct);
			buf.append("%\"></div></div>");
			
			String s= buf.toString();
			
			sb.append(SafeHtmlUtils.fromSafeConstant(s));
		}

		/**
		 * @param sb
		 */
		private void appendNoInfoBar(SafeHtmlBuilder sb) {
			sb.append(SafeHtmlUtils.fromSafeConstant("<div class=\"cc-barNoInfo\"></div>"));
		}
	}
	
	private class BestScoreBarColumn extends Column<UserAndSubmissionReceipt, Integer> {
		public BestScoreBarColumn() {
			super(new BestScoreBarCell());
		}
		
		@Override
		public Integer getValue(UserAndSubmissionReceipt object) {
			if (object.getSubmissionReceipt() == null) {
				return 0;
			}
			return object.getSubmissionReceipt().getNumTestsPassed();
		}
	}
	
	@Override
	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		problem = session.get(Problem.class);
		
		// Get test cases (so we know how many test cases there were)
		RPC.getCoursesAndProblemsService.getTestCasesForProblem(problem.getProblemId().intValue(), new AsyncCallback<TestCase[]>() {
			@Override
			public void onFailure(Throwable caught) {
				session.add(StatusMessage.error("Could not get test cases for problem", caught));
			}
			
			@Override
			public void onSuccess(TestCase[] result) {
				testCaseList = result;
				refreshView();
			}
		});
		
		// Get best submission receipts for each user
		RPC.getCoursesAndProblemsService.getBestSubmissionReceipts(problem, new AsyncCallback<UserAndSubmissionReceipt[]>() {
			@Override
			public void onFailure(Throwable caught) {
				session.add(StatusMessage.error("Could not get submission receipts for problem", caught));
			}
			
			@Override
			public void onSuccess(UserAndSubmissionReceipt[] result) {
				data = result;
				refreshView();
			}
		});
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof Problem) {
			problem = (Problem) hint;
			refreshView();
		} else if (key == Session.Event.ADDED_OBJECT && hint instanceof TestCase[]) {
			testCaseList = (TestCase[]) hint;
			refreshView();
		} else if (key == Session.Event.ADDED_OBJECT && hint instanceof UserAndSubmissionReceipt[]) {
			data = (UserAndSubmissionReceipt[]) hint;
			refreshView();
		}
	}

	private void refreshView() {
		grid.setRowCount(data.length);
		grid.setRowData(Arrays.asList(data));
	}
}
