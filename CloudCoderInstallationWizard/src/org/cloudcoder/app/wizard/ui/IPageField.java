package org.cloudcoder.app.wizard.ui;

import java.awt.Component;

import org.cloudcoder.app.wizard.model.IValue;

public interface IPageField {
	public Component asComponent();
	public int getFieldHeight();
	public void markValid();
	public void markInvalid();
	public IValue getCurrentValue();
	public void setChangeCallback(Runnable callback);
	public void setSelectiveEnablement(boolean enabled);
}
