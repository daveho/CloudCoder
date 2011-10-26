package org.cloudcoder.app.client.page;

import java.util.TreeSet;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TermAndYear;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class CoursesAndProblemsPageUI extends Composite implements Subscriber {
	private CloudCoderPage page;
	
	private Tree tree;
	
	public CoursesAndProblemsPageUI() {
		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		dockLayoutPanel.setSize("640px", "480px");
		
		tree = new Tree();
		dockLayoutPanel.addWest(tree, 18.2);
		
		InlineLabel problemDescriptionLabel = new InlineLabel("");
		dockLayoutPanel.addNorth(problemDescriptionLabel, 7.7);
		
		DataGrid<Problem> cellTable = new DataGrid<Problem>();
		dockLayoutPanel.add(cellTable);
		cellTable.setSize("100%", "100%");
		
		initWidget(dockLayoutPanel);
	}
	
	public void setPage(CloudCoderPage page) {
		this.page = page;
	}
	
	private static class TermAndYearTreeItem extends TreeItem {
		private TermAndYear termAndYear;
		
		public TermAndYearTreeItem(TermAndYear termAndYear) {
			super(termAndYear.toString());
			this.termAndYear = termAndYear;
		}
		
		public TermAndYear getTermAndYear() {
			return termAndYear;
		}
	}

	private static class CourseTreeItem extends TreeItem {
		private Course course;
		
		public CourseTreeItem(Course course) {
			super(course.getName());
			this.course = course;
		}
		
		public Course getCourse() {
			return course;
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint.getClass() == Course[].class) {
			//Window.alert("Loading courses...");
			
			// Courses loaded
			tree.clear();
			Course[] courseList = (Course[]) hint;

			// Build sorted collection of TermAndYear objects
			TreeSet<TermAndYear> termAndYearSet = new TreeSet<TermAndYear>();
			for (Course course : courseList) {
				termAndYearSet.add(course.getTermAndYear());
			}
			
			// Build tree, using TermAndYear items in descending chronological
			// order, and then attaching the Course items to each TermAndYear item
			for (TermAndYear termAndYear : termAndYearSet) {
				TermAndYearTreeItem termAndYearTreeItem = new TermAndYearTreeItem(termAndYear);
				tree.addItem(termAndYearTreeItem);
				for (Course course : courseList) {
					if (course.getTermAndYear().equals(termAndYear)) {
						termAndYearTreeItem.addItem(new CourseTreeItem(course));
					}
				}
			}
		}
	}
}
