package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * Model object representing an achievement that a User has earned
 * 
 * @author shanembonner
 *
 */
public class UserAchievement implements Serializable, IModelObject<UserAchievement> {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int userId;
	private int achievementId;
	private long timestamp;
	
	public static final ModelObjectField<? super UserAchievement, Integer> ID = new ModelObjectField<UserAchievement, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY){
		public void set(UserAchievement obj, Integer value) { obj.setId(value); }
		public Integer get(UserAchievement obj) { return obj.getId(); }
	};
	
	public static final ModelObjectField<? super UserAchievement, Integer> USERID = new ModelObjectField<UserAchievement, Integer>("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE){
		public void set(UserAchievement obj, Integer value) {obj.setUserId(value); }
		public Integer get(UserAchievement obj) { return obj.getUserId(); }
	};
	
	public static final ModelObjectField<? super UserAchievement, Integer> ACHIEVEMENTID = new ModelObjectField<UserAchievement, Integer>("achievement_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE){
		public void set(UserAchievement obj, Integer value) {obj.setAchievementId(value); }
		public Integer get(UserAchievement obj) { return obj.getAchievementId(); }
	};

	public static final ModelObjectField<? super UserAchievement, Long> TIMESTAMP = new ModelObjectField<UserAchievement, Long>("timestamp", Long.class, 0, ModelObjectIndexType.NON_UNIQUE){
		public void set(UserAchievement obj, Long value) {obj.setTimestamp(value); }
		public Long get(UserAchievement obj) { return obj.getTimestamp(); }
	};

	/**
	 * Constructor
	 */
	public UserAchievement(){
		
	}
	
	/**
	 * Description of fields (scheme version 0)
	 */
	public static final ModelObjectSchema<UserAchievement> SCHEMA_V0 = new ModelObjectSchema<UserAchievement>("user_achievement")
		.add(ID)
		.add(USERID)
		.add(ACHIEVEMENTID)
		.add(TIMESTAMP);
	
	/**
	 * Description of fields (current schema version)
	 */
	public static final ModelObjectSchema<UserAchievement> SCHEMA = SCHEMA_V0;
	
	@Override
	public ModelObjectSchema<? super UserAchievement> getSchema() {
		return SCHEMA;
	}

	/**
	 * set the achievement's unique id
	 * @param value the achievement's unique id
	 */
	public void setId(int value) {
		this.id = value;
	}
	
	/**
	 * get the achievement's unique id
	 * @return the achievement's unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * set the id of the user who unlocked the achievement
	 * @param value the id of the user
	 */
	public void setUserId(int value) {
		this.userId = value;
	}

	/**
	 * get the id of the user who unlocked the achievement
	 * @return the id of the user
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * set the id 
	 * @param value the id
	 */
	public void setAchievementId(Integer value) {
		this.achievementId = value;
	}

	/**
	 * get the id
	 * @return the id5
	 */
	public int getAchievementId() {
		return achievementId;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
}

