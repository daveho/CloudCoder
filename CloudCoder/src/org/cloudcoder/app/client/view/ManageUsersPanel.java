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
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;


/**
 * UI for managing users in a course, for the "Manage course" tab.
 * Currently, there are just two things you can do here:
 * edit a user and view progress for a user.
 * This implements the remaining functionality in the
 * UserAdminPage that isn't addressed by the other
 * UIs in the "Manage course" tab.
 * 
 * @author David Hovemeyer
 */
public class ManageUsersPanel extends Composite implements CourseInstructorUI, SessionObserver {
	private enum UserAction implements IButtonPanelAction {
		EDIT("Edit", "Edit user information"),
		DELETE("Delete", "Delete user from course"),
		VIEW_USER_PROGRESS("Statistics", "View progress of user in course");

		private String name;
		private String tooltip;

		private UserAction(String name, String tooltip) {
			this.name = name;
			this.tooltip = tooltip;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getTooltip() {
			return tooltip;
		}
	}

	private UserAdminUsersListView userListView;
	private int courseId;
	private ButtonPanel<UserAction> userManagementButtonPanel;
	private Session session;

	public ManageUsersPanel() {
		LayoutPanel panel = new LayoutPanel();

		panel.setWidth("100%");
		panel.setHeight("500px");

		// TODO: top stuff, buttons, etc.
		userManagementButtonPanel = new ButtonPanel<UserAction>(UserAction.values()) {
			@Override
			public boolean isEnabled(UserAction action) {
				User selected = userListView.getSelectedUser();
				return selected != null;
			}

			@Override
			public void onButtonClick(UserAction action) {
				switch (action) {
				case DELETE:
					handleDeleteUser();
					break;
				case EDIT:
					handleEditUser();
					break;
				case VIEW_USER_PROGRESS:
					handleUserProgress();
					break;
				default:
					break;

				}
			}
		};
		panel.add(userManagementButtonPanel);
		panel.setWidgetLeftRight(userManagementButtonPanel, 10.0, Unit.PX, 10.0, Unit.PX);
		panel.setWidgetTopHeight(userManagementButtonPanel, 0.0, Unit.PX, ButtonPanel.HEIGHT_PX, Unit.PX);

		userManagementButtonPanel.setStyleName("cc-inlineFlowPanel", true); // display inline

		userListView = new UserAdminUsersListView();
		panel.add(userListView);
		panel.setWidgetTopBottom(userListView, 44.0, Unit.PX, 20.0, Unit.PX);
		panel.setWidgetLeftRight(userListView, 10.0, Unit.PX, 10.0, Unit.PX);

		initWidget(panel);
	}

	protected void handleDeleteUser() {
		session.add(StatusMessage.information("Should be deleting user"));
	}

	protected void handleEditUser() {
		session.add(StatusMessage.information("Should be editing user"));
	}

	protected void handleUserProgress() {
		session.add(StatusMessage.information("Should be viewing user progress"));
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
		// Keep track of changes to instructor status
		new CourseInstructorStatusMonitor(this).activate(session, subscriptionRegistrar);

		userListView.activate(session, subscriptionRegistrar);
		userManagementButtonPanel.activate(session, subscriptionRegistrar);
	}

	@Override
	public void setEnabled(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCourseChange(Course course) {
		// TODO Auto-generated method stub

	}
}
