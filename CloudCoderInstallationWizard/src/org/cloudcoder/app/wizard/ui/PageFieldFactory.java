package org.cloudcoder.app.wizard.ui;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public class PageFieldFactory {
	public static IPageField createForValue(IValue v) {
		switch (v.getValueType()) {
		case IMMUTABLE_STRING:
			ImmutableStringValueField f = new ImmutableStringValueField();
			f.setValue((ImmutableStringValue) v);
			return f;
			
		default:
			throw new IllegalArgumentException("Value type " + v.getValueType() + " not supported yet");
		}
	}
}
