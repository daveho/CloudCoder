package org.cloudcoder.app.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class DevActionsPanel extends Composite {
	private Runnable submitHandler;
	
	public DevActionsPanel() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		Button submitButton = new Button("Submit!");
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (submitHandler != null) {
					submitHandler.run();
				}
			}
		});
		layoutPanel.add(submitButton);
		layoutPanel.setWidgetRightWidth(submitButton, 0.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetBottomHeight(submitButton, 15.0, Unit.PX, 27.0, Unit.PX);

		initWidget(layoutPanel);
	}
	
	public void setSubmitHandler(Runnable submitHandler) {
		this.submitHandler = submitHandler;
	}
}
