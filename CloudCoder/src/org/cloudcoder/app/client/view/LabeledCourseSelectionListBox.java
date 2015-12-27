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
import org.cloudcoder.app.client.view.CourseSelectionListBox.DisplayMode;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A labeled {@link CourseSelectionListBox}.
 * 
 * @author David Hovemeyer
 */
public class LabeledCourseSelectionListBox extends Composite implements SessionObserver {
	public static final double HEIGHT_PX = 24.0;
	public static final double LISTBOX_WIDTH_PX = 480.0;

	private CourseSelectionListBox courseSelectionListBox;

	/**
	 * Constructor.
	 * 
	 * @param page       the {@link CloudCoderPage}
	 * @param labelText  the label text
	 */
	public LabeledCourseSelectionListBox(CloudCoderPage page, String labelText) {
		FlowPanel panel = new FlowPanel();
		
		panel.setStyleName("cc-inlineFlowPanel", true);

		if (!labelText.endsWith(" ")) {
			labelText = labelText + " ";
		}
		
		InlineLabel label = new InlineLabel(labelText);
		panel.add(label);
		this.courseSelectionListBox = new CourseSelectionListBox(page, 1);
		panel.add(courseSelectionListBox);
		courseSelectionListBox.setWidth(LISTBOX_WIDTH_PX + "px");
		courseSelectionListBox.setHeight(HEIGHT_PX + "px");
		
		initWidget(panel);
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		courseSelectionListBox.activate(session, subscriptionRegistrar);
	}

	/**
	 * Set the {@link CourseSelectionListBox}'s display mode.
	 * 
	 * @param mode the display mode
	 */
	public void setDisplayMode(DisplayMode mode) {
		courseSelectionListBox.setDisplayMode(mode);
	}
}
