// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, Shane Bonner
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.page.SessionUtil;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

/**
 * View to display a list of courses the current user is registered
 * for, and allow the user to select one, adding a {@link CourseSelection}
 * for the selected course.
 * 
 * @author Shane Bonner
 * @author David Hovemeyer
 */
public class CourseSelectionListBox extends Composite implements SessionObserver, Subscriber {
	/**
	 * Comparator used to order CourseAndCourseRegistrations used in the listbox.
	 */
	private static class CourseAndCourseRegistrationComparator implements Comparator<CourseAndCourseRegistration> {
		@Override
		public int compare(CourseAndCourseRegistration left, CourseAndCourseRegistration right) {
			int cmp;
			
			// First, compare by TermAndYear in reverse chronological order
			// (which is the natural sort order for TermAndYear)
			cmp = left.getCourse().getTermAndYear().compareTo(right.getCourse().getTermAndYear());
			if (cmp != 0) {
				return cmp;
			}
			
			// Next, sort by course name and title
			cmp = left.getCourse().getNameAndTitle().compareTo(right.getCourse().getNameAndTitle());
			if (cmp != 0) {
				return 0;
			}
			
			// Next, sort by course id (which handles the unlikely but conceivable case that
			// there course be two distinct courses with the same name)
			cmp = left.getCourse().getId() - right.getCourse().getId();
			if (cmp != 0) {
				return cmp;
			}
			
			// At this point, we are looking at two course registrations
			// for the same course.
			
			// Next, sort by instructor status (instructor status is "less than":
			// this means that in the unlikely but conceivable case that a user is an instructor
			// for one section but a normal student in another, the instructor
			// registration appears first.)
			if (left.getCourseRegistration().getRegistrationType().isInstructor() !=
					right.getCourseRegistration().getRegistrationType().isInstructor()) {
				return left.getCourseRegistration().getRegistrationType().isInstructor() ? -1 : 1;
			}
			
			// Next, compare section numbers
			cmp = left.getCourseRegistration().getSection() - right.getCourseRegistration().getSection();
			if (cmp != 0) {
				return cmp;
			}
			
			// At this point, it's safe to say that these CourseAndCourseRegistrations
			// are identical (which shouldn't happen)
			return 0;
		}
	}
	
	/**
	 * The display mode.
	 */
	public enum DisplayMode {
		/** Just the course name and term/year. This is the default. */
		PLAIN,
		/** Course name and title, and term/year. */
		FANCY,
	}
	
	private CloudCoderPage page;
	private ListBox listBox;
	private Session session;
	private int selectedIndex;
	private CourseAndCourseRegistration[] courseAndCourseRegistrationList;
	private DisplayMode displayMode;
	
	/**
	 * Constructor.
	 * 
	 * @param page the {@link CloudCoderPage}
	 * @param visibleItemCount the visible item count: should be 1 if a drop-down list is desired
	 */
	public CourseSelectionListBox(CloudCoderPage page, int visibleItemCount) {
		this.page = page;
		listBox = new ListBox();

		listBox.setVisibleItemCount(visibleItemCount);
		
		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				handleChangeEvent(event);
			}
		});
		
		displayMode = DisplayMode.PLAIN;
		
		initWidget(listBox);
	}
	
	/**
	 * @param displayMode the displayMode to set
	 */
	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
	}
	
	/**
	 * Constructor.  The list will show 5 items (and scroll if there are more.)
	 * 
	 * @param page the {@link CloudCoderPage}
	 */
	public CourseSelectionListBox(CloudCoderPage page) {
		//setting the visible item count to 1 would turn the listBox into a drop-down list
		this(page, 5);
	}

	/**
	 * @param event
	 */
	protected void handleChangeEvent(ChangeEvent event) {
		//Window.alert("Selection changed?");
		selectedIndex = listBox.getSelectedIndex();
		session.add(courseAndCourseRegistrationList[selectedIndex]);
		
		//session.add(courseAndCourseRegistrationList[selectedIndex]);

		// Construct a CourseSelection for the selected CourseAndCourseRegistration
		CourseSelection courseSelection =
				new CourseSelection(courseAndCourseRegistrationList[selectedIndex].getCourse(), null);

		// Add it to the session
		session.add(courseSelection);

	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
		// subscribe to add object session events
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		CourseAndCourseRegistration[] regList = session.get(CourseAndCourseRegistration[].class);
		if (regList == null) {
			GWT.log("No course and course registration list?");
			SessionUtil.getCourseAndCourseRegistrationsRPC(page, session);
		} else {
			courseAndCourseRegistrationList = filterCourseRegistrations(regList);
			displayCourses();
		}
	}
	private void displayCourses() {
		listBox.clear();
		//add CourseAndCourseRegistration objects as list items
		for(int i = 0; i < courseAndCourseRegistrationList.length; i++){
			listBox.addItem(format(courseAndCourseRegistrationList[i]));
		}
		GWT.log("Added " + courseAndCourseRegistrationList.length + " courses...");
		
		CourseSelection sel = session.get(CourseSelection.class);
		if (sel != null) {
			updateSelection(sel);
		}
	}
	
	private String format(CourseAndCourseRegistration ccr) {
		switch (displayMode) {
		case FANCY:
			return ccr.getCourse().getNameAndTitle() + ", " + ccr.getCourse().getTermAndYear().toString();
			
		case PLAIN:
		default:
			return ccr.getCourse().getName() + ", " + ccr.getCourse().getTermAndYear().toString();
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseAndCourseRegistration[]) {
			// Courses loaded successfully
			courseAndCourseRegistrationList = filterCourseRegistrations((CourseAndCourseRegistration[]) hint);
			displayCourses();
			
			// If there is no CourseSelection in the Session yet,
			// make the first course the selected one
			CourseSelection sel = session.get(CourseSelection.class);
			if (sel == null && courseAndCourseRegistrationList.length > 0) {
				session.add(new CourseSelection(courseAndCourseRegistrationList[0].getCourse(), null));
			}
		} else if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseSelection) {
			// the course selection has changed, update which item is selected
			updateSelection((CourseSelection) hint);
		}
	}

	private CourseAndCourseRegistration[] filterCourseRegistrations(CourseAndCourseRegistration[] hint) {
		// Sort CourseAndCourseRegistrations
		ArrayList<CourseAndCourseRegistration> sortedRegList = new ArrayList<CourseAndCourseRegistration>(Arrays.asList(hint));
		Collections.sort(sortedRegList, new CourseAndCourseRegistrationComparator());
		
		// Build a list with a single entry per registered course.
		// Due to the sort order, this ensures that if the user
		// is an instructor in a course, that registration is the
		// one that is used.
		ArrayList<CourseAndCourseRegistration> filteredRegList = new ArrayList<CourseAndCourseRegistration>();
		Set<Integer> courseIds = new HashSet<Integer>();
		for (CourseAndCourseRegistration reg : sortedRegList) {
			if (!courseIds.contains(reg.getCourse().getId())) {
				courseIds.add(reg.getCourse().getId());
				filteredRegList.add(reg);
			}
		}
		
		return filteredRegList.toArray(new CourseAndCourseRegistration[filteredRegList.size()]);
	}

	/**
	 * @param sel
	 */
	private void updateSelection(CourseSelection sel) {
		if (courseAndCourseRegistrationList == null) {
			// This can happen if two CourseSelectionListBoxes are active,
			// and one becomes fully initialized before the other
			return;
		}
		for (int index = 0; index < courseAndCourseRegistrationList.length; index++) {
			if (sel.getCourse().getId() == courseAndCourseRegistrationList[index].getCourse().getId()) {
				listBox.setSelectedIndex(index);
				break;
			}
		}
	}

}
