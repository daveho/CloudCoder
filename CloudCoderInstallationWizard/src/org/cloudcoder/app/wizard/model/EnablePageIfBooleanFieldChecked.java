package org.cloudcoder.app.wizard.model;

/**
 * Enable a {@link Page} only if a boolean field is checked
 * (set to true.)
 */
public class EnablePageIfBooleanFieldChecked implements ISelectivePageEnablement {
	private String valueName;

	/**
	 * Constructor.
	 * 
	 * @param valueName composite name of the boolean value
	 */
	public EnablePageIfBooleanFieldChecked(String valueName) {
		this.valueName = valueName;
	}
	
	@Override
	public boolean isEnabled(Document document) {
		return document.getValue(valueName).getBoolean();
	}
}
