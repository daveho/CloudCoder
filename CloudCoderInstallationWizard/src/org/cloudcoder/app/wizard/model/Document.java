package org.cloudcoder.app.wizard.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Document implements Cloneable {
	private List<Page> pages;
	private Map<String, ISelectivePageEnablement> selectivePageEnablementMap;
	
	public Document() {
		pages = new ArrayList<Page>();
		selectivePageEnablementMap = new HashMap<String, ISelectivePageEnablement>();
	}
	
	public int getNumPages() {
		return pages.size();
	}
	
	public Page get(int index) {
		return pages.get(index);
	}
	
	public void addPage(Page p) {
		pages.add(p);
	}
	
	public Page getPage(String pageName) {
		return pages.get(getPageIndex(pageName));
	}
	
	private int getPageIndex(String pageName) {
		for (int i = 0; i < pages.size(); i++) {
			Page p = pages.get(i);
			if (p.getPageName().equals(pageName)) {
				return i;
			}
		}
		throw new NoSuchElementException("No such page: " + pageName);
	}
	
	public boolean hasPage(String pageName) {
		try {
			getPageIndex(pageName);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	private interface WithCompositeName<E> {
		public E execute(String pageName, String name);
	}

	public IValue getValue(String compositeName) {
		return doWithCompositeName(compositeName, new WithCompositeName<IValue>() {
			@Override
			public IValue execute(String pageName, String name) {
				return getPage(pageName).getValue(name);
			}
		});
	}

	public void replaceValue(String compositeName, final IValue value) {
		doWithCompositeName(compositeName, new WithCompositeName<Boolean>() {
			@Override
			public Boolean execute(String pageName, String name) {
				Page page = getPage(pageName);
				page.replaceValue(name, value);
				return true;
			}
		});
	}

	private<E> E doWithCompositeName(String compositeName, WithCompositeName<E> callback) {
		int dot = compositeName.indexOf('.');
		if (dot < 0) {
			throw new IllegalArgumentException("Invalid composite name: " + compositeName);
		}
		String pageName = compositeName.substring(0, dot);
		String name = compositeName.substring(dot+1);
		return callback.execute(pageName, name);
	}

	public void selectivelyEnablePageRange(String startPage, String endPage, ISelectivePageEnablement pageEnablement) {
		int startIndex = getPageIndex(startPage);
		int endIndex = getPageIndex(endPage);
		for (int i = startIndex; i <= endIndex; i++) {
			selectivePageEnablementMap.put(pages.get(i).getPageName(), pageEnablement);
		}
	}
	
	public boolean isPageEnabled(int pageIndex) {
		Page p = pages.get(pageIndex);
		ISelectivePageEnablement enablement = selectivePageEnablementMap.get(p.getPageName());
		if (enablement == null) {
			//System.out.println("No selective enablement for page " + p.getPageName());
			return true;
		}
		boolean enabled = enablement.isEnabled(this);
		//System.out.printf("Page %s %s enabled\n", p.getPageName(), enabled ? "is" : "is not");
		return enabled;
	}
}
