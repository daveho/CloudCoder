package org.cloudcoder.app.wizard.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.cloudcoder.app.wizard.model.validators.IValidator;
import org.cloudcoder.app.wizard.model.validators.NoopValidator;

public class Page implements Cloneable, Iterable<IValue> {
	private final String pageName, label;
	private List<IValue> values;
	private List<IValidator> validators;
	private Map<String, ISelectiveEnablement> selectiveEnablementMap;
	
	public Page(String pageName, String label) {
		this.pageName = pageName;
		this.label = label;
		this.values = new ArrayList<IValue>();
		this.validators = new ArrayList<IValidator>();
		this.selectiveEnablementMap = new HashMap<String, ISelectiveEnablement>();
	}
	
	public void add(IValue value, IValidator validator) {
		values.add(value);
		validators.add(validator);
	}

	public void addHelpText(String name, String label) {
		add(ImmutableStringValue.createHelpText(pageName, name, label), NoopValidator.INSTANCE);
	}
	
	public void selectivelyEnable(String name, ISelectiveEnablement selectiveEnablement) {
		selectiveEnablementMap.put(name, selectiveEnablement);
	}
	
	/**
	 * Check whether named {@link IValue} is selectively enabled
	 * or disabled.  (Validation is skipped for selectively-disabled
	 * page fields.)
	 * 
	 * @param name the name of an {@link IValue}
	 * @return true if the {@link IValue} is enabled, false if disabled
	 */
	public boolean isEnabled(String name) {
		if (!selectiveEnablementMap.containsKey(name)) {
			return true;
		}
		ISelectiveEnablement selectiveEnablement = selectiveEnablementMap.get(name);
		return selectiveEnablement.isEnabled(this);
	}
	
	public IValue getValue(String name) {
		for (IValue v : this) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		throw new NoSuchElementException("No such value: " + pageName + "." + name);
	}
	
	public int getNumValues() {
		return values.size();
	}
	
	public IValue get(int index) {
		return values.get(index);
	}

	public void set(int index, IValue value) {
		values.set(index, value);
	}
	
	public IValidator getValidator(int index) {
		return validators.get(index);
	}
	
	@Override
	public Iterator<IValue> iterator() {
		return values.iterator();
	}
	
	public String getPageName() {
		return pageName;
	}
	
	public String getLabel() {
		return label;
	}

	public Page clone() {
		try {
			Page dup = (Page) super.clone();
			// Deep copy values
			dup.values = new ArrayList<IValue>();
			for (IValue v : values) {
				dup.values.add(v.clone());
			}
			// Validators and selective enablement map
			// do not need to be cloned (they are never modified)
			return dup;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen");
		}
	}
}
