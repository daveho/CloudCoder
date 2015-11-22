// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.client.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

/**
 * Accordion panel widget.
 * We're rolling our own because I don't feel like adding
 * a heavyweight UI framework like SmartGWT just to get an
 * accordion panel.  Also, I'm not sure we want a dependency
 * on an LGPL library.  (I'm not sure what LGPL even means
 * for a webapp.)
 * 
 * @author David Hovemeyer
 */
public class AccordionPanel extends Composite {
	private static int nextId = 0;

	private FlowPanel flowPanel;
	
	// Wrapper for an accordion widget
	private static class Wrapper {
		/** The label. */
		final Label label;
		/** The actual widget. */
		final IsWidget widget;
		/** The unique assigned to the widget: allows
		 * us to find the widget in order to do a slide toggle.
		 */
		final String id;
		
		Wrapper(Label label, IsWidget widget, String id) {
			this.label = label;
			this.widget = widget;
			this.id = id;
		}
	}
	
	private List<Wrapper> wrapperList;
	private Wrapper selected;
	
	/**
	 * Constructor.
	 */
	public AccordionPanel() {
		this.wrapperList = new ArrayList<Wrapper>();
		
		this.flowPanel = new FlowPanel();
		flowPanel.setStyleName("cc-accordionPanel", true);
		
		initWidget(flowPanel);
	}
	
	/**
	 * Add a widget to the accordion panel.
	 * 
	 * @param widget     the widget to add
	 * @param labelText  the text for the widget's label
	 */
	public void add(IsWidget widget, String labelText) {
		// Create wrapper div
		FlowPanel div = new FlowPanel();
		div.setStyleName("cc-accordionPanelWidget", true);

		// Add to overall FlowPanel
		flowPanel.add(div);
		
		// Create label for this widget
		Label label = new Label(labelText);
		label.setStyleName("cc-accordionPanelWidgetLabel", true);
		
		// Add label and widget to wrapper div
		div.add(label);
		div.add(widget);
		
		// Generate a unique id for the widget
		String id = "ccAccordionWidget" + nextId;
		nextId++;
		widget.asWidget().getElement().setId(id);
		
		// Create a wrapper and add it to the list
		final Wrapper wrapper = new Wrapper(label, widget, id);
		wrapperList.add(wrapper);
		
		// Add click handler for label
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onLabelClick(wrapper);
			}
		});
		
		if (wrapperList.size() == 1) {
			// First widget added, make it the selected one
			this.selected = wrapper;
			wrapper.label.setStyleName("cc-accordionPanelWidgetLabelSelected", true);
		} else {
			// Hide this widget by default: it should become visible
			// when its label is clicked (and the widget is slide toggled)
			wrapper.widget.asWidget().getElement().getStyle().setDisplay(Display.NONE);
		}
		
	}

	protected void onLabelClick(Wrapper wrapper) {
		if (wrapper != selected) {
			if (selected != null) {
				// Unselect
				slideToggle(selected.id);
				selected.label.removeStyleName("cc-accordionPanelWidgetLabelSelected");
			}
			
			// Select the clicked widget
			selected = wrapper;
			selected.label.setStyleName("cc-accordionPanelWidgetLabelSelected", true);
			slideToggle(selected.id);
		}
	}
	
	private native void slideToggle(String id) /*-{
		var elt = $doc.getElementById(id);
		$wnd.$(elt).slideToggle(); // Yes, we can haz jquery
	}-*/;
}
