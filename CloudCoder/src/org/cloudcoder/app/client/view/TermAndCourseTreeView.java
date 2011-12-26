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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;

/**
 * Tree view for terms and courses.
 * 
 * @author David Hovemeyer
 */
public class TermAndCourseTreeView extends Composite implements SessionObserver {
	private CellTree cellTree;
	
	public TermAndCourseTreeView(Course[] courseList) {
		TermAndCourseTreeModel model = new TermAndCourseTreeModel(courseList);
		cellTree = new CellTree(model, null);
		initWidget(cellTree);
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		// TODO Auto-generated method stub
		
	}
}
