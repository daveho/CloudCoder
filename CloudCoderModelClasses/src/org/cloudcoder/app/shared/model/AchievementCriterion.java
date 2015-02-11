package org.cloudcoder.app.shared.model;

import java.io.Serializable;

public class AchievementCriterion implements Serializable, IModelObject<AchievementCriterion> {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int achievementId;
	private AchievementCriterionType type;
	private int value;
	private String tag;
	
	public static final ModelObjectField<? super AchievementCriterion, Integer> ID = new ModelObjectField<AchievementCriterion, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(AchievementCriterion obj, Integer value) {obj.setId(value); }
		public Integer get(AchievementCriterion obj) { return obj.getId(); }
	};
	
	public static final ModelObjectField<? super AchievementCriterion, Integer> ACHIEVEMENTID = new ModelObjectField<AchievementCriterion, Integer>("achievement_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(AchievementCriterion obj, Integer value) {obj.setAchievementId(value); }
		public Integer get(AchievementCriterion obj) {return obj.getAchievementId(); }
	};
	
	
	public static final ModelObjectField<? super AchievementCriterion, AchievementCriterionType> TYPE = new ModelObjectField<AchievementCriterion, AchievementCriterionType>("type", AchievementCriterionType.class, 0, ModelObjectIndexType.NONE) {
		public void set(AchievementCriterion obj, AchievementCriterionType value) { obj.setType(value); }
		public AchievementCriterionType get(AchievementCriterion obj) { return obj.getType(); }
	};
	
	public static final ModelObjectField<? super AchievementCriterion, Integer> VALUE = new ModelObjectField<AchievementCriterion, Integer>("value", Integer.class, 0, ModelObjectIndexType.NONE) {
		public void set(AchievementCriterion obj, Integer value) {obj.setValue(value); }
		public Integer get(AchievementCriterion obj) {return obj.getValue(); }
	};
	
	public static final ModelObjectField<? super AchievementCriterion, String> TAG = new ModelObjectField<AchievementCriterion, String>("tag", String.class, 100, ModelObjectIndexType.NON_UNIQUE) {
		public void set(AchievementCriterion obj, String value) {obj.setTag(value); }
		public String get(AchievementCriterion obj) { return obj.getTag(); }
	};
	
	/**
	 * Constructor
	 */
	public AchievementCriterion(){
		
	}
	
	/**
	 * Description of fields (schema version 0)
	 */
	public static final ModelObjectSchema<AchievementCriterion> SCHEMA_V0 = new ModelObjectSchema<AchievementCriterion>("achievement_criterion")
			.add(ID)
			.add(ACHIEVEMENTID)
			.add(TYPE)
			.add(VALUE)
			.add(TAG);
	
	public static final ModelObjectSchema<AchievementCriterion> SCHEMA = SCHEMA_V0;
	
	@Override
	public ModelObjectSchema<? super AchievementCriterion> getSchema() {
		return SCHEMA;
	}

	/**
	 * set the id of the earned achievement
	 * @param value the id of the earned achievement
	 */
	public void setId(int value) {
		this.id = value;
		
	}
	
	/**
	 * @return the id of this AchievementCriterion 
	 */
	private int getId() {
		return this.id;
	}
	
	/**
	 * set the achievementId
	 * @param value of the achievementId
	 */
	public void setAchievementId(int value) {
		this.achievementId = value;
	}
	
	public void setType(AchievementCriterionType type) {
		this.type = type;
	}
	
	public AchievementCriterionType getType() {
		return type;
	}

	/**
	 * return the achievementId
	 * @return
	 */
	public int getAchievementId() {
		return achievementId;
	}
	
	/**
	 * set the value of the achievement
	 * @param value the value of the achievement
	 */
	protected void setValue(int value) {
		this.value = value;
	}

	/**
	 * return the value of the achievement
	 * @return the value of the achievement
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * set the tag of the achievement
	 * @param value the tag of the achievement
	 */
	protected void setTag(String value) {
		this.tag = value;
	}

	/**
	 * get the tag of the achievement
	 * @return the tag of the achievement
	 */
	protected String getTag() {
		return tag;
	}




}
