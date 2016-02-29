package org.cloudcoder.app.wizard.model;

public class DisplayOptions implements Cloneable {
	private int displayOptions;
	
	public DisplayOptions() {
		this.displayOptions = 0;
	}
	
	public void addDisplayOption(DisplayOption opt) {
		displayOptions |= (1 << opt.ordinal());
	}
	
	public boolean hasDisplayOption(DisplayOption opt) {
		return (displayOptions & (1 << opt.ordinal())) != 0;
	}
	
	@Override
	public DisplayOptions clone() {
		try {
			return (DisplayOptions) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("This should not happen");
		}
	}
}
