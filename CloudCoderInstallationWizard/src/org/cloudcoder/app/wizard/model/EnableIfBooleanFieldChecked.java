package org.cloudcoder.app.wizard.model;

public class EnableIfBooleanFieldChecked implements ISelectiveEnablement {
	private String booleanValueName;
	
	public EnableIfBooleanFieldChecked(String booleanValueName) {
		this.booleanValueName = booleanValueName;
	}

	@Override
	public boolean isEnabled(Page page) {
		return page.getValue(booleanValueName).getBoolean();
	}
}
