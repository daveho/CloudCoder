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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.ProblemAdminPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndModule;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * View to show problems in the {@link ProblemAdminPage}.
 * 
 * @author David Hovemeyer
 */
public class CourseAdminProblemListView extends ResizeComposite implements Subscriber, SessionObserver {
	private static final String CHECKMARK_URL = GWT.getModuleBaseForStaticFiles() + "images/check-mark-icon-sm.png";

	private CloudCoderPage page;
	private DataGrid<ProblemAndModule> grid;
	private Session session;
	private MultiSelectionModel<ProblemAndModule> selectionModel;
	private ICallback<ProblemAndModule> editModuleNameCallback;
	
	/**
	 * Constructor.
	 */
	public CourseAdminProblemListView(CloudCoderPage page) {
		this.page = page;
		grid = new DataGrid<ProblemAndModule>();
		grid.addColumn(new ProblemIdColumn(), "Id");
		grid.addColumn(new ProblemNameColumn(), "Name");
		grid.addColumn(new ProblemBriefDescriptionColumn(), "Description");
		grid.addColumn(new ProblemTypeColumn(), "Type");
		grid.addColumn(new ProblemWhenAssignedColumn(), "Assigned");
		grid.addColumn(new ProblemWhenDueColumn(), "Due");
		grid.addColumn(new ProblemVisibleColumn(), "Visible");
		grid.addColumn(new ProblemLicense(), "License");
		grid.addColumn(new ProblemSharedColumn(), "Shared");
		
		grid.setColumnWidth(0, 5.0, Unit.PCT);
		grid.setColumnWidth(1, 12.5, Unit.PCT);
		grid.setColumnWidth(2, 22.0, Unit.PCT);
		grid.setColumnWidth(3, 12.5, Unit.PCT);
		grid.setColumnWidth(4, 9.0, Unit.PCT);
		grid.setColumnWidth(5, 9.0, Unit.PCT);
		grid.setColumnWidth(6, 60.0, Unit.PX);
		grid.setColumnWidth(7, 10.0, Unit.PCT);
		grid.setColumnWidth(8, 60.0, Unit.PX);
		grid.setColumnWidth(9, 20.0, Unit.PCT);
		
		// The column displaying the module name allows editing, and invokes
		// a callback when the module name changes.
		ProblemModuleNameColumn moduleNameColumn = new ProblemModuleNameColumn();
		grid.addColumn(moduleNameColumn, "Module (click to edit)");
		moduleNameColumn.setFieldUpdater(new FieldUpdater<ProblemAndModule, String>() {
			@Override
			public void update(int index, ProblemAndModule object, String value) {
				if (!value.equals(object.getModule().getName())) {
					object.getModule().setName(value);
					if (editModuleNameCallback != null) {
						editModuleNameCallback.call(object);
					}
				}
			}
		});
		initWidget(grid);
	}
	
	/**
	 * Set a callback to be invoked when the module name of a problem is changed.
	 * 
	 * @param callback callback invoked when the module name of a problem is changed
	 */
	public void setEditModuleNameCallback(ICallback<ProblemAndModule> callback) {
		this.editModuleNameCallback = callback;
	}
	
	private static class ProblemIdColumn extends TextColumn<ProblemAndModule> {
		@Override
		public String getValue(ProblemAndModule object) {
			return String.valueOf(object.getProblem().getProblemId());
		}
	}
	
	private static class ProblemNameColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().getTestname();
		}
	}
	
	private static class ProblemBriefDescriptionColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().getBriefDescription();
		}
	}
	
	private static class ProblemTypeColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().getProblemType().toString().replace("_", " ");
		}
	}
	
	private static class ProblemWhenAssignedColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return ViewUtil.formatDate(object.getProblem().getWhenAssignedAsDate());
		}
	}
	
	private static class ProblemWhenDueColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return ViewUtil.formatDate(object.getProblem().getWhenDueAsDate());
		}
	}
	
	private static class ProblemLicense extends TextColumn<ProblemAndModule> {
        /* (non-Javadoc)
         * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
         */
        @Override
        public String getValue(ProblemAndModule object) {
            switch (object.getProblem().getLicense()) {
            case CC_ATTRIB_SHAREALIKE_3_0:
                return "CC";
            case GNU_FDL_1_3_NO_EXCEPTIONS:
                return "GFDL";
            case NOT_REDISTRIBUTABLE:
                return "not permissive";
            default:
                return "unknown";
            }
        }
    }
	
	private static class ProblemVisibleColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().isVisible() ? "true" : "false";
		}
	}
	
	private static class ProblemSharedCell extends AbstractCell<Boolean> {
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
				Boolean value, SafeHtmlBuilder sb) {
			if (value) {
				sb.appendHtmlConstant("<img src=\"" + CHECKMARK_URL + "\" alt=\"yes\" />");
			}
		}
	}
	
	private static class ProblemSharedColumn extends Column<ProblemAndModule, Boolean> {
		public ProblemSharedColumn() {
			super(new ProblemSharedCell());
		}
		
		@Override
		public Boolean getValue(ProblemAndModule object) {
			return object.getProblem().isShared();
		}
	}
	
	private static class ProblemModuleNameColumn extends Column<ProblemAndModule, String> {
		public ProblemModuleNameColumn() {
			super(new EditTextCell());
		}
		
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getModule().getName();
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		// Set selection model.
		// When a Problem is selected, it will be added to the Session.
		this.selectionModel = new MultiSelectionModel<ProblemAndModule>();
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
			    Set<ProblemAndModule> problemModuleSet=selectionModel.getSelectedSet();
			    Problem[] problems=getProblemsFromProblemAndModule(problemModuleSet);
			    if (problems!=null) {
			        session.add(problems);
			        if (problems.length==1) {
			            // If there's only one problem, add it by itself to the session
			            // This makes the options that operate on a single problem work better
			            session.add(problems[0]);
			        }
			    }
			}
		});
		grid.setSelectionModel(selectionModel);

		// Force loading of problems in course.
		// This avoids the problem that if a module in a course was selected
		// in the courses/problems page, some of the problems may not be
		// in the session (because they weren't in the selected module).
		CourseSelection courseSelection = session.get(CourseSelection.class);
		Course course = courseSelection.getCourse();
		loadProblems(session, course);
	}
	
	public static Problem[] getProblemsFromProblemAndModule(Collection<ProblemAndModule> collection) {
        Problem[] problems=new Problem[collection.size()];
        int i=0;
        for (ProblemAndModule pm : collection) {
            problems[i]=pm.getProblem();
            i++;
        }
        return problems;
	}
	
	/**
	 * Get the currently-selected {@link Problem}s.
	 * 
	 * @return the currently-selected {@link Problem}s
	 */
	public Problem[] getSelected() {
		return getProblemsFromProblemAndModule(selectionModel.getSelectedSet());
	}

	/**
	 * Force {@link Problem}s to be reloaded.
	 * 
	 * @param session the current {@link Session}
	 * @param course  the current {@link Course}
	 */
	public void loadProblems(final Session session, final Course course) {
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, session.get(User.class), (Module)null, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
			/* (non-Javadoc)
			 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
			 */
			@Override
			public void onSuccess(ProblemAndSubmissionReceipt[] result) {
				displayProblems(result);
			}
			/* (non-Javadoc)
			 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
			 */
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable() {
						public void run() {
							// Try again!
							loadProblems(session, course);
						}
					});
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
			// Course selected, load its problems.
			// Note that this isn't really needed by CourseAdminPage (because there
			// is only one Course which is pre-selected), but if this view is
			// reused in another page at some point, this might be useful.
			CourseSelection courseSelection = (CourseSelection) hint;
			Course course = courseSelection.getCourse();
			loadProblems(session, course);
		} else if (key == Session.Event.ADDED_OBJECT && (hint instanceof ProblemAndSubmissionReceipt[])) {
			// This can happen when these is an explicit reload of problems
			displayProblems((ProblemAndSubmissionReceipt[]) hint);
		}
	}

	protected void displayProblems(ProblemAndSubmissionReceipt[] problemAndSubmissionReceiptList) {
		ProblemAndModule[] problems = new ProblemAndModule[problemAndSubmissionReceiptList.length];
		int count = 0;
		for (ProblemAndSubmissionReceipt p : problemAndSubmissionReceiptList) {
			problems[count++] = new ProblemAndModule(p.getProblem(), p.getModule());
		}
		displayProblems(problems);
	}

	protected void displayProblems(ProblemAndModule[] result) {
		grid.setRowCount(result.length);
		grid.setRowData(Arrays.asList(result));
		grid.setVisibleRange(0, result.length);
	}

    /**
     * @return
     */
    public boolean hasPotentialUnsharedExercises() {
        for (ProblemAndModule problemAndModule : grid.getVisibleItems()) {  
            Problem p=problemAndModule.getProblem();
            if (!p.isShared() && (p.getProblemAuthorship()==ProblemAuthorship.ORIGINAL || 
                    p.getProblemAuthorship()==ProblemAuthorship.IMPORTED_AND_MODIFIED))
            {
                // an unshared exercise that is original (i.e. new to this author)
                // or has been imported and modified can be shared
                return true;
            }
        }
        return false;
    }
}
