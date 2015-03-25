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
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.page.SessionUtil;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.UserAchievementAndAchievement;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author shanembonner
 * Achievement summary view: provides a quick visual summary of
 * achievements that the u.
 *
 */
public class UserAchievementSummaryView extends Composite implements Subscriber, SessionObserver{
	public static final double HEIGHT_PX = 48.0;
	private CloudCoderPage page;
	private Session session;
	private FlowPanel flowPanel;
	private ICallback<UserAchievementSummaryItem> clickHandler;

	/**
	 * Constructor.
	 */
	public UserAchievementSummaryView(CloudCoderPage page) {
		// This widget is basically a div containing AchievementSummaryItems
		this.flowPanel = new FlowPanel();
		
		flowPanel.setStyleName("cc-achievementSummary", true);
		
		// Allow it to scroll
		ScrollPanel wrap = new ScrollPanel();
		wrap.add(flowPanel);
		
		initWidget(wrap);
		
		// Create a callback to handle item clicks
		this.clickHandler = new ICallback<UserAchievementSummaryItem>() {
			@Override
			public void call(UserAchievementSummaryItem value) {
				handleItemClick(value);
			}
		};
	}

	protected void handleItemClick(UserAchievementSummaryItem value) {
		// Add the Problem to the session (to select the problem)
		//ProblemAndSubmissionReceipt p = value.getProblemAndSubmissionReceipt();
		
		//TODO: add the achievement item to the session
		UserAchievementAndAchievement ua = value.getUserAchievementAndAchievement();
		
		session.add(ua.getAchievement());
	}

	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
	{
		this.session = session;
		
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		UserAchievementAndAchievement[] achievements = session.get(UserAchievementAndAchievement[].class);
		if (achievements != null) {
			// We already have some achievements - display them
			loadData(achievements);
		} else {
			// Initiate loading of user achievements
			GWT.log("Initial loading of user achievements...");
			doLoadUserAchievementAndAchievementList();
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof UserAchievementAndAchievement[]) {
			GWT.log("User events added: " + ((UserAchievementAndAchievement[])hint).length);
			loadData((UserAchievementAndAchievement[]) hint);
		} else if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseSelection) {
			// Course selection has changed - load user achievements for the course
			GWT.log("Course selection changed, loading user achievements...");
			doLoadUserAchievementAndAchievementList();
		}
	}

	private void doLoadUserAchievementAndAchievementList() {
		CourseSelection sel = session.get(CourseSelection.class);
		if (sel != null) {
			SessionUtil.loadUserAchievementAndAchievementList(page, sel);
		}
	}

	/**
	 * @param problemAndSubmissionReceipts
	 */
	private void loadData(UserAchievementAndAchievement[] userAchievementAndAchievements) {
		GWT.log("Adding " + userAchievementAndAchievements.length + " to achievement summary view");
		
		// clear current data, load new data
		flowPanel.clear();
		
		// Sort by due date
		//ProblemAndSubmissionReceipt[] list = new ProblemAndSubmissionReceipt[problemAndSubmissionReceipts.length]; //problemAndSubmissionReceipts.clone();
		//System.arraycopy(problemAndSubmissionReceipts, 0, list, 0, problemAndSubmissionReceipts.length);
		//ViewUtil.sortProblemsByDueDate(list);
		
		//loop through the problem and submission receipts,
		//create exerciseSummaryItem for each, and set each box's status (completed, failed, etc)
		
		
		for (UserAchievementAndAchievement item : userAchievementAndAchievements){
			// Create ExerciseSummaryItem
			UserAchievementSummaryItem summaryItem = new UserAchievementSummaryItem();
			summaryItem.setUserAchievementAndAchievement(item);
			summaryItem.setClickHandler(clickHandler);
			flowPanel.add(summaryItem);
		}
	}

}
