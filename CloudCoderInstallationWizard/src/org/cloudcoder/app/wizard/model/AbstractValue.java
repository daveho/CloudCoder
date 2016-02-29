package org.cloudcoder.app.wizard.model;

public class AbstractValue {
	public final String name;
	public final String label;
	private DisplayOptions displayOptions;
	
	public AbstractValue(String name, String label) {
		this.name = name;
		this.label = label;
		this.displayOptions = new DisplayOptions();
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void addDisplayOption(DisplayOption opt) {
		displayOptions.addDisplayOption(opt);
	}
	
	public boolean hasDisplayOption(DisplayOption opt) {
		return displayOptions.hasDisplayOption(opt);
	}
}
