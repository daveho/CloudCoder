package org.cloudcoder.app.shared.model;

import java.io.Serializable;

public class AchievementImage implements Serializable, IModelObject<AchievementImage> {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private byte[] imageArr;
	
	public static final ModelObjectField<? super AchievementImage, Integer> ID = new ModelObjectField<AchievementImage, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(AchievementImage obj, Integer value) { obj.setId(value); }
		public Integer get(AchievementImage obj) { return obj.getId(); }
	};
	
	public static final ModelObjectField<? super AchievementImage, byte[]> IMAGEARR = new ModelObjectField<AchievementImage, byte[]>("image_arr", byte[].class, 65535, ModelObjectIndexType.NONE) {
		public void set(AchievementImage obj, byte[] value) { obj.setImageArr(value); }
		public byte[] get(AchievementImage obj) { return obj.getImageArr(); }
	};

	public static final ModelObjectSchema<AchievementImage> SCHEMA_V0 = new ModelObjectSchema<AchievementImage>("achievement_image")
		.add(ID)
		.add(IMAGEARR);
		
	/**
	 * Description of fields (current schema version)
	 */
	public static final ModelObjectSchema<AchievementImage> SCHEMA = SCHEMA_V0;

	@Override
	public ModelObjectSchema<? super AchievementImage> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Constructor
	 */
	public AchievementImage(){
		
	}
	
	public void setId(int value) {
		this.id = value;
		
	}

	public int getId() {
		return id;
	}
	
	public void setImageArr(byte[] value) {
		this.imageArr = value;
	}
	public byte[] getImageArr() {
		return imageArr;
	}
	
}
