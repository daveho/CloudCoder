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
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.PlaygroundTestResult;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * Display and modify inputs and outputs for "playground mode"
 * 
 * @author jaimespacco
 *
 */
public class PlaygroundResultListView extends ResizeComposite implements SessionObserver, Subscriber, IResultsTabPanelWidget
{
    private DataGrid<PlaygroundTestResult> cellTable;
    
    public PlaygroundResultListView() {
        cellTable = new DataGrid<PlaygroundTestResult>();
        cellTable.addColumn(new TestNumberColumn(), "Test Number");
        cellTable.addColumn(new InputColumn(), "Input");
        cellTable.addColumn(new StdoutColumn(), "Output");
        cellTable.addColumn(new StderrColumn(), "Error Output");
        initWidget(cellTable);
    }

    private static class TestNumberColumn extends TextColumn<PlaygroundTestResult> {
        @Override
        public String getValue(PlaygroundTestResult object) {
            return object.getTestNumber()+"";
        }
    }
    private static class InputColumn extends TextColumn<PlaygroundTestResult> {
        @Override
        public String getValue(PlaygroundTestResult object) {
            return object.getInput();
        }
    }
    private static class StdoutColumn extends TextColumn<PlaygroundTestResult> {
        @Override
        public String getValue(PlaygroundTestResult object) {
            return object.getStdout();
        }
    }
    private static class StderrColumn extends TextColumn<PlaygroundTestResult> {
        @Override
        public String getValue(PlaygroundTestResult object) {
            return object.getStderr();
        }
    }
    
    
    @Override
    public void eventOccurred(Object key, Publisher publisher, Object hint) {
        if (key == Session.Event.ADDED_OBJECT && hint instanceof PlaygroundTestResult[]) {
            displayTestResults((PlaygroundTestResult[]) hint);
        }
    }
    
    private void displayTestResults(PlaygroundTestResult[] testResultList) {
        cellTable.setRowData(Arrays.asList(testResultList));
    }
    
    @Override
    public void setSelected() {
        // Workaround for http://code.google.com/p/google-web-toolkit/issues/detail?id=7065
        cellTable.redraw();
    }

    @Override
    public void activate(Session session,SubscriptionRegistrar subscriptionRegistrar)
    {
        session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
        //TODO add default TestCase[] to the session, or as an instance variable
        PlaygroundTestResult[] testResultList = session.get(PlaygroundTestResult[].class);
        if (testResultList==null) {
            testResultList=new PlaygroundTestResult[1];
            testResultList[0]=new PlaygroundTestResult();
        }
        displayTestResults(testResultList);

    }
}
