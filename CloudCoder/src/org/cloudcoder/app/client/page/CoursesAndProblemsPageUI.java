package org.cloudcoder.app.client.page;

import java.util.TreeSet;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.ProblemListView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TermAndYear;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class CoursesAndProblemsPageUI extends Composite implements Subscriber, CloudCoderPageUI {
	private CloudCoderPage page;

	private Tree tree;
	//private DataGrid<Problem> cellTable;
	private ProblemListView problemListView;
	private ProblemDescriptionView problemDescriptionView;
	private LayoutPanel layoutPanel;
	private Button loadProblemButton;
	
	public CoursesAndProblemsPageUI() {
		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		//dockLayoutPanel.setSize("800px", "600px");

		tree = new Tree();
		dockLayoutPanel.addWest(tree, 28.0);
		
		problemDescriptionView = new ProblemDescriptionView();
		dockLayoutPanel.addNorth(problemDescriptionView, 7.7);
		
		layoutPanel = new LayoutPanel();
		dockLayoutPanel.add(layoutPanel);
		
		loadProblemButton = new Button("Load Problem!");
		layoutPanel.add(loadProblemButton);
		layoutPanel.setWidgetRightWidth(loadProblemButton, 0.0, Unit.PX, 120.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(loadProblemButton, 0.0, Unit.PX, 40.0, Unit.PX);
		loadProblemButton.setSize("120px", "40px");

		//cellTable = new DataGrid<Problem>();
		problemListView = new ProblemListView();
		layoutPanel.add(problemListView);
		layoutPanel.setWidgetTopBottom(problemListView, 46.0, Unit.PX, 0.0, Unit.PX);
		layoutPanel.setWidgetLeftRight(problemListView, 0.0, Unit.PX, 0.0, Unit.PX);

		initWidget(dockLayoutPanel);
	}

	public void setPage(CloudCoderPage page) {
		this.page = page;
	}

	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
		// Subscribe to Session events
		session.subscribeToAll(Session.Event.values(), this, subscriptionRegistrar);
		
		// Activate views
		problemListView.activate(session, subscriptionRegistrar);
		problemDescriptionView.activate(session, subscriptionRegistrar);
		
		// When a course is selected in the tree, load its problems
		tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				TreeItem item = event.getSelectedItem();
				if (item instanceof CourseTreeItem) {
					CourseTreeItem courseTreeItem = (CourseTreeItem) item;
					Course course = courseTreeItem.getCourse();

					session.add(course);
				}
			}
		});
		
		// Add handler for load problem button
		loadProblemButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Problem problem = session.get(Problem.class);
				if (problem != null) {
					session.notifySubscribers(Session.Event.PROBLEM_CHOSEN, problem);
				}
			}
		});
		
		// Load courses
		RPC.getCoursesAndProblemsService.getCourses(new AsyncCallback<Course[]>() {
			@Override
			public void onSuccess(Course[] result) {
				page.addSessionObject(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// FIXME: display error
			}
		});
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
}
