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

import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.UserAchievementAndAchievement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author shanembonner
 *
 */

public class UserAchievementSummaryItem extends Composite { 
//	private HTML html;
	private UserAchievementAndAchievement userAchievementAndAchievement;
	private ICallback<UserAchievementSummaryItem> clickHandler;
	private Image image;
	private Label label;
	
	public UserAchievementSummaryItem(){
//		this.html = new HTML("<span></span>");
//		html.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				onClickEvent();
//			}
//		});
//		configureStyle(); // Just set a default style
//		initWidget(html);
		
		final Image image = new Image();
		// TODO: create and configure Image

		ErrorHandler handler = new ErrorHandler(){
			public void onError(ErrorEvent event){
				label.setText("An error occurred while loading.");
			}
		};
		
		image.addErrorHandler(handler);
		initWidget(image);
	}

	
	protected void onClickEvent() {
		if (clickHandler != null) {
			clickHandler.call(this);
		}
	}
	
	/**
	 * set this item's UserAchievementAndAchievement
	 * @param item
	 */
	public void setUserAchievementAndAchievement(UserAchievementAndAchievement item) {
		this.userAchievementAndAchievement = item;
		configureStyle();
		setTooltip(item.getAchievement().getAchievementTitle()); 
	}
	
	/**
	 * get this item's userAchievementAndAchievement item
	 * @return
	 */
	public UserAchievementAndAchievement getUserAchievementAndAchievement(){
		return userAchievementAndAchievement;
	}
	
	/**
	 * Set a callback to be invoked when the user clicks on this
	 * item.  The {@link AchievementSummaryView} will use this to
	 * handle item clicks.
	 * 
	 * @param clickHandler the click handler to set
	 */
	public void setClickHandler(ICallback<UserAchievementSummaryItem> clickHandler) {
		this.clickHandler = clickHandler;
	}
	
	/**
	 * If the userAchievement field in the userAchievementAndAchievement object is null,
	 * the user has not earned that achievement. If it exists, the user has earned it.
	 * @return
	 */
	public boolean getStatus(){
		if(userAchievementAndAchievement.getUserAchievement() == null){
			return false;
		}else{
			return true;
		}
	}
	
	private void configureStyle() {
		
		this.setStyleName("cc-achievementSummaryItem"); 
		
		//find out if the user has earned the achievement
		Boolean status = getStatus();
		
		//if the achievement has not been earned, the item will appear transparent.
		if(status == false){
			this.setStyleName("cc-achievementNotEarned");
		}else{
			this.setStyleName("cc-achievementEarned");
		}
		
		image.setUrl(GWT.getModuleBaseURL() + "/aimg/" + userAchievementAndAchievement.getAchievement().getAchievementImageId());
	}
	
	private void setTooltip(String tooltip) {
		image.getElement().setAttribute("title", tooltip);
	}
}
