// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog to show detailed program output from
 * a TestResult.
 * 
 * @author David Hovemeyer
 */
public class TestResultOutputDialog extends DialogBox {
	/**
	 * Constructor.
	 * 
	 * @param outputText the output text to show
	 */
	public TestResultOutputDialog(String outputText) {
		setText("Test Result Output");
		
		setGlassEnabled(true);

		LayoutPanel layoutPanel = new LayoutPanel();
		layoutPanel.setSize("540px", "405px");
		
		ScrollPanel scrollPanel = new ScrollPanel();
		layoutPanel.add(scrollPanel);
		layoutPanel.setWidgetLeftRight(scrollPanel, 10.0, Unit.PX, 10.0, Unit.PX);
		layoutPanel.setWidgetTopBottom(scrollPanel, 10.0, Unit.PX, 50.0, Unit.PX);
		
		SafeHtmlBuilder outputBuilder = new SafeHtmlBuilder();
		outputBuilder.appendHtmlConstant("<pre>");
		outputBuilder.appendEscaped(outputText);
		outputBuilder.appendHtmlConstant("</pre>");
		HTML output = new HTML(outputBuilder.toSafeHtml());
		scrollPanel.add(output);
		
		Button dismiss = new Button("Dismiss");
		layoutPanel.add(dismiss);
		layoutPanel.setWidgetRightWidth(dismiss, 10.0, Unit.PX, 100.0, Unit.PX);
		layoutPanel.setWidgetBottomHeight(dismiss, 10.0, Unit.PX, 30.0, Unit.PX);
		
		dismiss.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TestResultOutputDialog.this.hide();
			}
		});
		
		setWidget(layoutPanel);
	}
}
