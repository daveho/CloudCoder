package org.cloudcoder.app.client.page;

import java.util.TreeSet;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TermAndYear;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class CoursesAndProblemsPageUI extends Composite implements Subscriber {
	private CloudCoderPage page;

	private Tree tree;
	private DataGrid<Problem> cellTable;
	
	private static class BriefDescriptionCell extends AbstractCell<String> {

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
			if (value == null) {
				return;
			}
			SafeHtml safeValue = SafeHtmlUtils.fromString(value);
			sb.appendHtmlConstant("<span>");
			sb.append(safeValue);
			sb.appendHtmlConstant("</span");
		}
		
	}
	
	private static class BriefDescriptionColumn extends Column<Problem, String> {

		public BriefDescriptionColumn() {
			super(new BriefDescriptionCell());
		}

		@Override
		public String getValue(Problem object) {
			return object.getBriefDescription();
		}
		
	}

	public CoursesAndProblemsPageUI() {
		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		dockLayoutPanel.setSize("800px", "600px");

		tree = new Tree();
		dockLayoutPanel.addWest(tree, 28.0);

		InlineLabel problemDescriptionLabel = new InlineLabel("");
		dockLayoutPanel.addNorth(problemDescriptionLabel, 7.7);

		cellTable = new DataGrid<Problem>();
		dockLayoutPanel.add(cellTable);
		cellTable.setSize("100%", "100%");

		initWidget(dockLayoutPanel);
	}

	public void setPage(CloudCoderPage page) {
		this.page = page;
	}

	public void activate() {
		tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				TreeItem item = event.getSelectedItem();
				if (item instanceof CourseTreeItem) {
					CourseTreeItem courseTreeItem = (CourseTreeItem) item;
					Course course = courseTreeItem.getCourse();

					// Load problems for this course
					RPC.getCoursesAndProblemsService.getProblems(course, new AsyncCallback<Problem[]>() {
						@Override
						public void onFailure(Throwable caught) {
							// FIXME: display error
						}

						@Override
						public void onSuccess(Problem[] result) {
							page.getSession().add(result);
						}
					});
				}
			}
		});
		
		// Configure the DataGrid that will show the problems
		
//		cellTable.addColumn(new ProblemNameColumn(new Cell<String>()), new Header());
//		cellTable.addColumn(new BriefDescriptionColumn(), new Header<String>() {
//
//			@Override
//			public String getValue() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//			
//		});
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
		if (key == Session.Event.ADDED_OBJECT) {
			if (hint.getClass() == Course[].class) {
				displayLoadedCourses(hint);
			} else if (hint.getClass() == Problem[].class) {
				displayLoadedProblems(hint);
			}
		}
	}

	private void displayLoadedCourses(Object hint) {
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

	private void displayLoadedProblems(Object hint) {
		Problem[] problemList = (Problem[]) hint;
		
		
	}
}
