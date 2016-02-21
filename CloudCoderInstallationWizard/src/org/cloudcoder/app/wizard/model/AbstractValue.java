package org.cloudcoder.app.wizard.model;

public class AbstractValue {
	public final String name;
	public final String label;
	
	public AbstractValue(String name, String label) {
		this.name = name;
		this.label = label;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}
}
