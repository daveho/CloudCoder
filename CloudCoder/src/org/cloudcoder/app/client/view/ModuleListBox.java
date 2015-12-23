// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.page.SessionUtil;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

/**
 * ListBox to select a {@link Module} in the currently-selected course.
 * Affects the current {@link CourseSelection}.
 * 
 * @author David Hovemeyer
 */
public class ModuleListBox extends Composite implements SessionObserver, Subscriber {
	private CloudCoderPage page;
	private ListBox listBox;
	private CourseSelection currentCourseSelection;
	private Module[] moduleList;

	/**
	 * Constructor.
	 * 
	 * @param page the {@link CloudCoderPage}
	 */
	public ModuleListBox(CloudCoderPage page) {
		this.page = page;
		this.listBox = new ListBox();
		
		listBox.setVisibleItemCount(1); // make it a drop-down
		
		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				handleChangeEvent(event);
			}
		});
		
		initWidget(listBox);
	}

	protected void handleChangeEvent(ChangeEvent event) {
		int selectedIndex = listBox.getSelectedIndex();
		
		if (selectedIndex == 0) {
			// This is the special "All modules" item.
			// Add a CourseSelection without a Module.
			if (currentCourseSelection.getModule() != null) {
				page.getSession().add(new CourseSelection(currentCourseSelection.getCourse(), null));
			}
		} else {
			// Module was selected
			page.getSession().add(new CourseSelection(currentCourseSelection.getCourse(), moduleList[selectedIndex-1]));
		}
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		CourseSelection courseSelection = session.get(CourseSelection.class);
		if (courseSelection != null) {
			sync(courseSelection);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (hint instanceof CourseSelection) {
			CourseSelection courseSelection = (CourseSelection) hint;
			sync(courseSelection);
		}
	}

	private void sync(CourseSelection courseSelection) {
		if (currentCourseSelection != null && courseSelection.equals(currentCourseSelection)) {
			// nothing to do
			return;
		}

		CourseSelection origCourseSelection = currentCourseSelection;
		this.currentCourseSelection = courseSelection;

		// If only the module has changed, show the selected
		// module.
		if (origCourseSelection != null && origCourseSelection.getCourse().equals(courseSelection.getCourse())) {
			showSelectedModule();
			return;
		}

		// Course has changed: load modules for that course (and then
		// show selected module)
		this.moduleList = null;
		listBox.clear();
		SessionUtil.loadModulesForCourse(page, currentCourseSelection.getCourse(), new ICallback<Module[]>() {
			@Override
			public void call(Module[] value) {
				moduleList = value;
				populateListBox();
			}
		});
	}

	protected void populateListBox() {
		listBox.clear();
		listBox.addItem("All modules");
		for (Module m : moduleList) {
			listBox.addItem(m.getName());
		}
		showSelectedModule();
	}

	private void showSelectedModule() {
		if (currentCourseSelection.getModule() == null) {
			// Select "All modules"
			listBox.setSelectedIndex(0);
		} else {
			// Display selected module
			for (int i = 0; i < moduleList.length; i++) {
				if (currentCourseSelection.getModule().getId() == moduleList[i].getId()) {
					listBox.setSelectedIndex(i+1);
				}
			}
		}
	}
}
