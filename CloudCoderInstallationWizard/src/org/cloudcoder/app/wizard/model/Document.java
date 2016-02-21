package org.cloudcoder.app.wizard.model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Document implements Cloneable {
	private List<Page> pages;
	
	public Document() {
		pages = new ArrayList<Page>();
	}
	
	public Page get(int index) {
		return pages.get(index);
	}
	
	public void addPage(Page p) {
		pages.add(p);
	}
	
	public Page getPage(String pageName) {
		for (Page p : pages) {
			if (p.getPageName().equals(pageName)) {
				return p;
			}
		}
		throw new NoSuchElementException("No such page: " + pageName);
	}
	
	public IValue getValue(String compositeName) {
		int dot = compositeName.indexOf('.');
		if (dot < 0) {
			throw new IllegalArgumentException("Invalid composite name: " + compositeName);
		}
		String pageName = compositeName.substring(0, dot);
		String name = compositeName.substring(dot+1);
		return getPage(pageName).getValue(name);
	}
}
