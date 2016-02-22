package org.cloudcoder.app.wizard.ui;

import org.cloudcoder.app.wizard.model.EnumValue;
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
			
		case ENUM_CHOICE:
			return createEnumValueField(v);
			
		default:
			throw new IllegalArgumentException("Value type " + v.getValueType() + " not supported yet");
		}
	}

	private static<T extends Enum<T>> IPageField createEnumValueField(IValue v) {
		@SuppressWarnings("unchecked")
		EnumValue<T> value = (EnumValue<T>) v;
		EnumValueField<T> field = EnumValueField.createForEnumClass(value.getEnumCls());
		field.setValue(value);
		return field;
	}
}
