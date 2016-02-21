package org.cloudcoder.app.wizard.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Page implements Cloneable, Iterable<IValue> {
	private final String pageName;
	private List<IValue> values;
	
	public Page(String pageName) {
		this.pageName = pageName;
		this.values = new ArrayList<IValue>();
	}
	
	public void add(IValue value) {
		values.add(value);
	}

	public void addHelpText(String name, String label) {
		values.add(ImmutableStringValue.createHelpText(pageName, name, label));
	}
	
	public IValue getValue(String name) {
		for (IValue v : this) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		throw new NoSuchElementException("No such value: " + pageName + "." + name);
	}
	
	@Override
	public Iterator<IValue> iterator() {
		return values.iterator();
	}
	
	public String getPageName() {
		return pageName;
	}

	public Page clone() {
		try {
			Page dup = (Page) super.clone();
			// Deep copy values
			dup.values = new ArrayList<IValue>();
			for (IValue v : values) {
				dup.values.add(v.clone());
			}
			return dup;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen");
		}
	}
}
