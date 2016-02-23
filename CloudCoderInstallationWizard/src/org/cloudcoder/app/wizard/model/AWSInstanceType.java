package org.cloudcoder.app.wizard.model;

public enum AWSInstanceType {
	// Just support T2 instances for now
	T2_NANO,
	T2_MICRO,
	T2_SMALL,
	T2_MEDIUM,
	T2_LARGE,
	;
	
	public String toString() {
		return name().toLowerCase().replace('_', '.');
	}
}
