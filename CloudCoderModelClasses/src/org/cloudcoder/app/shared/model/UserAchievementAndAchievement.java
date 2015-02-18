package org.cloudcoder.app.shared.model;

import java.io.Serializable;

public class UserAchievementAndAchievement implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private UserAchievement userAchievement;
	private Achievement achievement;
	
	public UserAchievementAndAchievement() {
		
	}
	
	public void setUserAchievement(UserAchievement userAchievement) {
		this.userAchievement = userAchievement;
	}
	
	public UserAchievement getUserAchievement() {
		return userAchievement;
	}
	
	public void setAchievement(Achievement achievement) {
		this.achievement = achievement;
	}
	
	public Achievement getAchievement() {
		return achievement;
	}
}
