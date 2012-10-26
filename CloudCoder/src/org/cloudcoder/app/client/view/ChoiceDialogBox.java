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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * Dialog box asking the user to choose between choices.
 * 
 * @param <ChoiceType> the data type of the choice values to be selected
 * @author David Hovemeyer
 */
public class ChoiceDialogBox<ChoiceType> extends DialogBox {
	private class Choice {
		private String buttonText;
		private ChoiceType choice;
		
		public Choice(String buttonText, ChoiceType choice) {
			this.buttonText = buttonText;
			this.choice = choice;
		}
		
		public String getButtonText() {
			return buttonText;
		}
		
		public ChoiceType getChoice() {
			return choice;
		}
	}
	
	/**
	 * Callback interface.
	 *
	 * @param <ChoiceType> choice value type
	 */
	public interface ChoiceHandler<ChoiceType> {
		/**
		 * Called when a choice is made.
		 * 
		 * @param choice the chosen choice value
		 */
		public void handleChoice(ChoiceType choice);
	}
	
	private ChoiceHandler<ChoiceType> handler;
	private List<Choice> choiceList;
	private String bodyText;
	private boolean uiCreated;
	
	/**
	 * Constructor.
	 * 
	 * @param title dialog title
	 * @param text dialog caption
	 */
	public ChoiceDialogBox(String title, String text, ChoiceHandler<ChoiceType> handler) {
		this.handler = handler;
		this.choiceList = new ArrayList<Choice>();
		setText(title);
		this.bodyText = text;
		setGlassEnabled(true);
	}
	
	/**
	 * Add a choice.
	 * 
	 * @param buttonText  text to display on the choice's button
	 * @param choice      the choice value
	 */
	public void addChoice(String buttonText, ChoiceType choice) {
		choiceList.add(new Choice(buttonText, choice));
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.DialogBox#show()
	 */
	@Override
	public void show() {
		createUI();
		super.show();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#center()
	 */
	@Override
	public void center() {
		createUI();
		super.center();
	}

	private void createUI() {
		if (!uiCreated) {
			FlowPanel buttonPanel = new FlowPanel();
			
			FlowPanel div = new FlowPanel();
			div.add(new HTML("<br />"));
			div.add(new Label(bodyText));
			div.add(new HTML("<br />"));
			buttonPanel.add(div);
			
			FlowPanel buttonDiv = new FlowPanel();
			
			for (final Choice choice : choiceList) {
				Button button = new Button(choice.getButtonText());
				button.setStyleName("cc-choiceDialogButton", true);
				button.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						hide();
						// Invoke callback with the chosen choice value.
						handler.handleChoice(choice.getChoice());
					}
				});
				buttonDiv.add(button);
			}
			
			buttonPanel.add(buttonDiv);
			
			add(buttonPanel);
			
			uiCreated = true;
		}
	}
}
