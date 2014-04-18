// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import java.util.Collections;
import java.util.Comparator;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.TermAndYear;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * UI for selecting a course from which to import all problems. 
 * 
 * @author David Hovemeyer
 */
public class ImportCourseSelectionView extends Composite {
	public static final double HEIGHT_PX = 200.0;
	
	private Session session;
	private ArrayList<CourseAndCourseRegistration> instructorCourseList;
	private DataGrid<CourseAndCourseRegistration> instructorCourseGrid;

	private SingleSelectionModel<CourseAndCourseRegistration> selectionModel;
	
	private static class TermAndYearColumn extends TextColumn<CourseAndCourseRegistration>  {
		@Override
		public String getValue(CourseAndCourseRegistration object) {
			TermAndYear termAndYear = object.getCourse().getTermAndYear();
			return termAndYear.getTerm().getName() + " " + termAndYear.getYear();
		}
	}
	
	private static class CourseNameColumn extends TextColumn<CourseAndCourseRegistration> {
		@Override
		public String getValue(CourseAndCourseRegistration object) {
			return object.getCourse().getName() + " - " + object.getCourse().getTitle();
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * @param session the {@link Session}
	 */
	public ImportCourseSelectionView(Session session) {
		this.session = session;
		this.instructorCourseList = new ArrayList<CourseAndCourseRegistration>();
		
		populateInstructorCourseList();
		
		instructorCourseGrid = new DataGrid<CourseAndCourseRegistration>();
		instructorCourseGrid.addColumn(new TermAndYearColumn(), "Term");
		instructorCourseGrid.addColumn(new CourseNameColumn(), "Course");
		
		instructorCourseGrid.setRowData(0, instructorCourseList);
		
		selectionModel = new SingleSelectionModel<CourseAndCourseRegistration>();
		instructorCourseGrid.setSelectionModel(selectionModel);

		initWidget(instructorCourseGrid);
	}
	
	/**
	 * @return selected {@link CourseAndCourseRegistration}
	 */
	public CourseAndCourseRegistration getSelected() {
		return selectionModel.getSelectedObject();
	}

	private void populateInstructorCourseList() {
		CourseSelection courseSelection = session.get(CourseSelection.class);
		
		// Build a list of all courses other than the current one where the current user was an instructor
		for (CourseAndCourseRegistration reg : session.get(CourseAndCourseRegistration[].class)) {
			if (!reg.getCourseRegistration().getRegistrationType().isInstructor()) {
				// Current user not registered as an instructor for the other course
				continue;
			}
			if (reg.getCourse().getId() == courseSelection.getCourse().getId()) {
				// This is the course that the user wants to import problems into
				continue;
			}
			instructorCourseList.add(reg);
		}
		
		// Sort by term/year and name/title
		Collections.sort(instructorCourseList, new Comparator<CourseAndCourseRegistration>() {
			@Override
			public int compare(CourseAndCourseRegistration lhs, CourseAndCourseRegistration rhs) {
				int cmp;
				
				cmp = lhs.getCourse().getTermAndYear().compareTo(rhs.getCourse().getTermAndYear());
				if (cmp != 0) {
					return cmp;
				}
				
				cmp = lhs.getCourse().getName().compareTo(rhs.getCourse().getName());
				if (cmp != 0) {
					return cmp;
				}
				
				return lhs.getCourse().getTitle().compareTo(rhs.getCourse().getTitle());
			}
		});
	}
}
