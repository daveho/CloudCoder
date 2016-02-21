package org.cloudcoder.app.wizard.ui;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;
import org.cloudcoder.app.wizard.model.StringValue;

public class PageFieldFactory {
	public static IPageField createForValue(IValue v) {
		switch (v.getValueType()) {
		case IMMUTABLE_STRING:
			ImmutableStringValueField isvf = new ImmutableStringValueField();
			isvf.setValue((ImmutableStringValue) v);
			return isvf;
			
		case STRING:
			StringValueField svf = new StringValueField();
			svf.setValue((StringValue) v);
			return svf;
			
		default:
			throw new IllegalArgumentException("Value type " + v.getValueType() + " not supported yet");
		}
	}
}
