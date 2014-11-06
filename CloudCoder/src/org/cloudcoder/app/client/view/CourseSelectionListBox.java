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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author shanembonner
 *
 */
public class CourseSelectionListBox extends Composite implements SessionObserver, Subscriber {
	
	private ListBox listBox;
	private Session session;
	private int selectedIndex;
	private CourseAndCourseRegistration[] courseAndCourseRegistrationList;
	
	public CourseSelectionListBox() {
		listBox = new ListBox();
		
		// TODO: handle item selection events (make sure that single-selection is enabled)
		//   The handler should create a CourseSelection object and add it to the session
		//   (the Module in the CourseSelection can be null)
		
		
		
		//setting the visible item count to 1 would turn the listBox into a drop-down list
		listBox.setVisibleItemCount(5);
		
		
		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				handleChangeEvent(event);
			}
		});
		
		
		initWidget(listBox);
	}

	/**
	 * @param event
	 */
	protected void handleChangeEvent(ChangeEvent event) {
		Window.alert("Selection changed?");
		//selectedIndex = listBox.getSelectedIndex();
		//session.add(courseAndCourseRegistrationList[selectedIndex]);
		
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
		// subscribe to add object session events
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		courseAndCourseRegistrationList = session.get(CourseAndCourseRegistration[].class);
		if (courseAndCourseRegistrationList != null) {
			// TODO: add CourseAndCourseRegistration objects as list items
			for(int i = 0; i < courseAndCourseRegistrationList.length; i++){
				listBox.addItem(format(courseAndCourseRegistrationList[i]));
			}
			GWT.log("Added " + courseAndCourseRegistrationList.length + " courses...");
		}
		if (courseAndCourseRegistrationList == null) {
			GWT.log("No course and course registration list?");
		}

	}
	
	private String format(CourseAndCourseRegistration ccr) {
		return ccr.getCourse().getName() + ", " + ccr.getCourse().getTermAndYear().toString();
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseSelection) {
			// the course selection has changed, update which item is selected
			CourseSelection sel = (CourseSelection) hint;
		}
	}

}
