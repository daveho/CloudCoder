package org.cloudcoder.app.shared.model;

/**
 * A module is a category containing related {@link Problem}s
 * in a {@link Course}. 
 * 
 * @author David Hovemeyer
 */
public class Module implements IModelObject<Module> {
	private int id;
	private String name;
	private int courseId;
	
	public static ModelObjectField<Module, Integer> ID = new ModelObjectField<Module, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(Module obj, Integer value) { obj.setId(value); }
		public Integer get(Module obj) { return obj.getId(); }
	};
	public static ModelObjectField<Module, String> NAME = new ModelObjectField<Module, String>("name", String.class, 40) {
		public void set(Module obj, String value) { obj.setName(value); }
		public String get(Module obj) { return obj.getName(); }
	};
	
	public static final Module DEFAULT_MODULE = new Module();
	static {
		DEFAULT_MODULE.setId(0);
		DEFAULT_MODULE.setName("Uncategorized");
	}
	
	/**
	 * Model object schema (version 0).
	 */
	public static ModelObjectSchema<Module> SCHEMA_V0 = new ModelObjectSchema<Module>("module")
			.add(ID)
			.add(NAME)
			.addPersistedModelObject(DEFAULT_MODULE);
	
	/**
	 * Model object schema (current version).
	 */
	public static ModelObjectSchema<Module> SCHEMA = SCHEMA_V0;
	
	/**
	 * Constructor.
	 */
	public Module() {
		
	}
	
	@Override
	public ModelObjectSchema<? super Module> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set the unique id.
	 * @param id the unique id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the module's unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the module's name.
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the module's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the course id.
	 * @param courseId the course id to set
	 */
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	/**
	 * @return the course id
	 */
	public int getCourseId() {
		return courseId;
	}
}
