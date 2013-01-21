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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.TermAndYear;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

/**
 * Tree view for terms, courses, and modules.
 * 
 * @author David Hovemeyer
 */
public class TermAndCourseTreeView extends Composite {
	/**
	 * Tree view model for TermAndCourseTreeView.
	 */
	private class Model implements TreeViewModel {
		private List<TermAndYear> termAndYearList;
		private Map<TermAndYear, Course[]> termAndYearToCourseList;
		private Map<Course, ListDataProvider<Module>> courseToDataProvider;
		private Map<Course, Boolean> courseModulesLoaded;
		
		private SingleSelectionModel<Object> selectionModel;
		
		public Model(CourseAndCourseRegistration[] courseAndRegList) {
			termAndYearList = new ArrayList<TermAndYear>();
			termAndYearToCourseList = new HashMap<TermAndYear, Course[]>();
			
			courseToDataProvider = new HashMap<Course, ListDataProvider<Module>>();
			courseModulesLoaded = new HashMap<Course, Boolean>();
			
			// Mark all courses as having not yet loaded their modules
			for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
				Course course = courseAndReg.getCourse();
				if (!courseToDataProvider.containsKey(course)) {
					ListDataProvider<Module> dataProvider = new ListDataProvider<Module>(new ArrayList<Module>());
					courseToDataProvider.put(course, dataProvider);
					courseModulesLoaded.put(course, false);
				}
			}

			// Build sorted collection of TermAndYear objects
			TreeSet<TermAndYear> termAndYearSet = new TreeSet<TermAndYear>();
			for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
				Course course = courseAndReg.getCourse();
				termAndYearSet.add(course.getTermAndYear());
			}
			
			// Build list of TermAndYears, in reverse chronological order.
			// For each TermAndYear, map it to an array of Courses.
			for (TermAndYear termAndYear : termAndYearSet) {
				termAndYearList.add(termAndYear);
				ArrayList<Course> courseListForTermAndYear = new ArrayList<Course>();
				for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
					Course course = courseAndReg.getCourse();
					// Note: because a user may have multiple CourseRegistrations
					// for the same course (e.g., an instructor who is teaching
					// multiple sections), we may have duplicate courses.
					// So, check before adding one to the course list.
					if (course.getTermAndYear().equals(termAndYear) && !courseListForTermAndYear.contains(course)) {
						courseListForTermAndYear.add(course);
					}
				}
				termAndYearToCourseList.put(
						termAndYear,
						courseListForTermAndYear.toArray(new Course[courseListForTermAndYear.size()]));
			}
			
			// Selection model: Courses and Modules can be selected
			selectionModel = new SingleSelectionModel<Object>();

			// Handle selection change events by:
			//   - Loading modules for course (if not yet loaded)
			//   - Invoking view's selection change handler (if there is one)
			selectionModel.addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					Object selected = selectionModel.getSelectedObject();
					if (selected instanceof Course) {
						Course course = (Course)selected;
						if (!courseModulesLoaded.get(course)) {
							courseModulesLoaded.put(course, true);
							//RPC.getCoursesAndProblemsService.getModulesInCourse

							// FIXME: hardcoded for just default "Uncategorized" Module - should load modules via RPC
							Module defaultModule = new Module();
							defaultModule.setId(1);
							defaultModule.setName("Uncategorized");
							Module[] moduleList = new Module[]{ defaultModule };
							courseToDataProvider.get(course).getList().addAll(Arrays.asList(moduleList));
						}
					}
					
					if (selectionChangeHandler != null) {
						selectionChangeHandler.onSelectionChange(event);
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.view.client.TreeViewModel#getNodeInfo(java.lang.Object)
		 */
		@Override
		public <T> NodeInfo<?> getNodeInfo(T value) {
			if (value == null) {
				// root nodes - TermAndYear
				ListDataProvider<TermAndYear> dataProvider = new ListDataProvider<TermAndYear>();
				dataProvider.getList().addAll(termAndYearList);
				Cell<TermAndYear> cell = new AbstractCell<TermAndYear>() {
					/* (non-Javadoc)
					 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
					 */
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							TermAndYear value, SafeHtmlBuilder sb) {
						sb.appendEscaped(value.toString());
					}
				};
				return new DefaultNodeInfo<TermAndYear>(dataProvider, cell);
			} else if (value instanceof TermAndYear) {
				Course[] courseList = termAndYearToCourseList.get(value);
				ListDataProvider<Course> dataProvider = new ListDataProvider<Course>();
				dataProvider.getList().addAll(Arrays.asList(courseList));
				Cell<Course> cell = new AbstractCell<Course>() {
					/* (non-Javadoc)
					 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
					 */
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							Course value, SafeHtmlBuilder sb) {
						sb.appendEscaped(value.toString());
					}
				};
				return new DefaultNodeInfo<Course>(dataProvider, cell, selectionModel, null);
			} else if (value instanceof Course) {
				Cell<Module> cell = new AbstractCell<Module>() {
					/* (non-Javadoc)
					 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
					 */
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							Module value, SafeHtmlBuilder sb) {
						sb.appendEscaped(value.getName());
					}
				};
				return new DefaultNodeInfo<Module>(courseToDataProvider.get((Course)value), cell, selectionModel, null);
			} else {
				throw new IllegalStateException();
			}
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.view.client.TreeViewModel#isLeaf(java.lang.Object)
		 */
		@Override
		public boolean isLeaf(Object value) {
			if (value instanceof Module) {
				return true;
			}
			if (value instanceof Course) {
				Boolean modulesLoaded = courseModulesLoaded.get(value);
				return modulesLoaded == null || !modulesLoaded;
			}
			return false;
		}
	}
	
	private CellTree cellTree;
	private Model model;
	private Handler selectionChangeHandler;
	
	public TermAndCourseTreeView(CourseAndCourseRegistration[] courseList) {
		model = new Model(courseList);
		cellTree = new CellTree(model, null);
		initWidget(cellTree);
		
		// Expand the first child of the root.
		if (cellTree.getRootTreeNode().getChildCount() > 0) {
			cellTree.getRootTreeNode().setChildOpen(0, true);
		}
	}
	
	/**
	 * Add Handler for selection change events.
	 * 
	 * @param h the Handler to add
	 */
	public void addSelectionHandler(Handler h) {
		selectionChangeHandler = h;
	}

	/**
	 * @return the currently selected course
	 */
	public Course getSelectedCourse() {
		Object obj = model.selectionModel.getSelectedObject();
		if (obj instanceof Course) {
			return (Course) obj;
		} else {
			// TODO: should return the Course if a Module is selected
			return null;
		}
	}
}
