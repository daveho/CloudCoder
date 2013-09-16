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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * A panel containing a horizontal row of buttons.
 * 
 * @author David Hovemeyer
 */
public abstract class ButtonPanel<ActionType extends IButtonPanelAction> extends Composite {
	/**
	 * Height in pixels.
	 */
	public static final double HEIGHT_PX = 30.0;
	
	private Map<ActionType, Button> actionToButtonMap;

	/**
	 * Constructor.
	 * 
	 * @param actions the {@link IButtonPanelAction} objects; a button is created for each one
	 */
	public ButtonPanel(ActionType[] actions) {
		this.actionToButtonMap = new HashMap<ActionType, Button>();
		
		FlowPanel panel = new FlowPanel();

		for (final ActionType action : actions) {
			final Button button = new Button(action.getName());
			actionToButtonMap.put(action, button);

			button.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onButtonClick(action);
				}
			});
			button.setEnabled(action.isEnabledByDefault());
			
			String tooltip = action.getTooltip();
			if (!tooltip.equals("")) {
				button.setTitle(tooltip);
			}
			
			// Add some space between buttons
			if (actionToButtonMap.size() > 1) {
				panel.add(new InlineHTML(" "));
			}
			
			panel.add(button);
		}
		
		initWidget(panel);
	}
	
	/**
	 * Update the enabled/disable status for each button.
	 * The {@link #isEnabled(IButtonPanelAction)} method is called
	 * for each {@link IButtonPanelAction} to determine the status
	 * of the corresponding button.
	 */
	public void updateButtonEnablement() {
		for (Map.Entry<ActionType, Button> entry : actionToButtonMap.entrySet()) {
			entry.getValue().setEnabled(isEnabled(entry.getKey()));
		}
	}
	
	/**
	 * Callback to handle an action's button being clicked.
	 * 
	 * @param action the {@link IButtonPanelAction} whose button was clicked
	 */
	public abstract void onButtonClick(ActionType action);
	
	/**
	 * Callback to determine if the button for a particular action
	 * should be enabled or disabled.
	 * 
	 * @param action the {@link IButtonPanelAction}
	 * @return true if the action's button should be enabled, false otherwise
	 */
	public abstract boolean isEnabled(ActionType action);
}
