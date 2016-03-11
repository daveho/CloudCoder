package org.cloudcoder.app.wizard.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Contains all user-defined configuration properties.
 * Consists of {@link Page}s, each of which is a sequence of
 * {@link IValue}s.
 * 
 * @author David Hovemeyer
 */
public class Document implements Cloneable {
	private List<Page> pages;
	private Map<String, ISelectivePageEnablement> selectivePageEnablementMap;
	private Map<String, IPageNavigationHook> pageNavigationHookMap;
	private String errorPage;
	private String finishedPage;
	
	public Document() {
		pages = new ArrayList<Page>();
		selectivePageEnablementMap = new HashMap<String, ISelectivePageEnablement>();
		pageNavigationHookMap = new HashMap<String, IPageNavigationHook>();
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
	
	public interface CompositeNameCallback<E> {
		public E execute(String pageName, String name);
	}

	public IValue getValue(String compositeName) {
		return withCompositeName(compositeName, new CompositeNameCallback<IValue>() {
			@Override
			public IValue execute(String pageName, String name) {
				return getPage(pageName).getValue(name);
			}
		});
	}

	public void replaceValue(String compositeName, final IValue value) {
		withCompositeName(compositeName, new CompositeNameCallback<Boolean>() {
			@Override
			public Boolean execute(String pageName, String name) {
				Page page = getPage(pageName);
				page.replaceValue(name, value);
				return true;
			}
		});
	}

	public static<E> E withCompositeName(String compositeName, CompositeNameCallback<E> callback) {
		int dot = compositeName.indexOf('.');
		if (dot < 0) {
			throw new IllegalArgumentException("Invalid composite name: " + compositeName);
		}
		String pageName = compositeName.substring(0, dot);
		String name = compositeName.substring(dot+1);
		return callback.execute(pageName, name);
	}

//	public void selectivelyEnablePageRange(String startPage, String endPage, ISelectivePageEnablement pageEnablement) {
//		int startIndex = getPageIndex(startPage);
//		int endIndex = getPageIndex(endPage);
//		for (int i = startIndex; i <= endIndex; i++) {
//			selectivePageEnablementMap.put(pages.get(i).getPageName(), pageEnablement);
//		}
//	}

	public void selectivelyEnablePage(String pageName, ISelectivePageEnablement pageEnablement) {
		selectivePageEnablementMap.put(pageName, pageEnablement);
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
	
	public void addPageNavigationHook(String pageName, IPageNavigationHook hook) {
		pageNavigationHookMap.put(pageName, hook);
	}
	
	public boolean hasPageNavigationHook(String pageName) {
		return pageNavigationHookMap.containsKey(pageName);
	}
	
	public IPageNavigationHook getPageNavigationHook(String pageName) {
		return pageNavigationHookMap.get(pageName);
	}
	
	public void setErrorPage(String errorPage) {
		this.errorPage = errorPage;
	}
	
	public String getErrorPage() {
		return errorPage;
	}
	
	public void setFinishedPage(String finishedPage) {
		this.finishedPage = finishedPage;
	}
	
	public String getFinishedPage() {
		return finishedPage;
	}
}
