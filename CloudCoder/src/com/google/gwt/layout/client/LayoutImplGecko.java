package com.google.gwt.layout.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;

public class LayoutImplGecko extends LayoutImpl {
	public LayoutImplGecko() {
	}

	@Override
	public void fillParent(Element elem) {
		super.fillParent(elem);

		if (workOn(elem)) {
			Style style = elem.getStyle();
			style.setProperty("MozBoxSizing", "border-box");
			style.setWidth(100, Unit.PCT);
			style.setHeight(100, Unit.PCT);
		}
	}

	public boolean workOn(Element elem) {
		String tagName = elem.getTagName();
		return tagName.equalsIgnoreCase("input") || tagName.equalsIgnoreCase("button");
	}

	@Override
	public void removeChild(Element container, Element child) {
		super.removeChild(container, child);

		if (workOn(child)) {
			// Cleanup child styles set by fillParent().
			Style style = child.getStyle();
			style.clearProperty("MozBoxSizing");
			style.clearWidth();
			style.clearHeight();
		}
	}
}
