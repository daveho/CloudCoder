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

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;

/**
 * Table view for compiler diagnostics.
 * 
 * @author David Hovemeyer
 */
public class CompilerDiagnosticListView extends Composite implements SessionObserver, Subscriber {
	private CellTable<CompilerDiagnostic> cellTable;
	
	public CompilerDiagnosticListView() {
		cellTable = new CellTable<CompilerDiagnostic>();
		cellTable.setSize("100%", "100%");
		
		initWidget(cellTable);
	}

	private class MessageColumn extends TextColumn<CompilerDiagnostic> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(CompilerDiagnostic object) {
			return object.getMessage();
		}
	}
	
	private class LineColumn extends TextColumn<CompilerDiagnostic> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(CompilerDiagnostic object) {
			return String.valueOf(object.getStartLine());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		// Add columns
		cellTable.addColumn(new MessageColumn(), "Message");
		cellTable.addColumn(new LineColumn(), "Line number");
		
		CompilerDiagnostic[] compilerDiagnosticList = session.get(CompilerDiagnostic[].class);
		if (compilerDiagnosticList != null) {
			displayCompilerDiagnostics(compilerDiagnosticList);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof CompilerDiagnostic[]) {
			displayCompilerDiagnostics((CompilerDiagnostic[]) hint);
		}
	}

	private void displayCompilerDiagnostics(CompilerDiagnostic[] compilerDiagnosticList) {
		cellTable.setRowCount(compilerDiagnosticList.length);
		cellTable.setRowData(Arrays.asList(compilerDiagnosticList));
	}
}
