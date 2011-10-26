package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class CoursesAndProblemsPageUI extends DockLayoutPanel implements Subscriber {
	private CloudCoderPage page;
	
	private Tree tree;
	
	public CoursesAndProblemsPageUI() {
		super(Unit.EM);
		
		setSize("640px", "480px");
		
		tree = new Tree();
		addWest(tree, 18.2);
		
		InlineLabel problemDescriptionLabel = new InlineLabel("");
		addNorth(problemDescriptionLabel, 7.7);
		
		DataGrid<Problem> cellTable = new DataGrid<Problem>();
		add(cellTable);
		cellTable.setSize("100%", "100%");
	}
	
	public void setPage(CloudCoderPage page) {
		this.page = page;
	}

	private static class CourseTreeItem extends TreeItem {
		private Course course;
		
		public CourseTreeItem(Course course) {
			this.course = course;
		}
		
		public Course getCourse() {
			return course;
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint.getClass() == Course[].class) {
			Window.alert("Loading courses...");
			// Courses loaded
			tree.clear();
			Course[] courseList = (Course[]) hint;
			for (Course course : courseList) {
				tree.addItem(new CourseTreeItem(course));
				GWT.log("Added course " + course.getName());
			}
		}
	}
}
