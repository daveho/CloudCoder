package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.CloudCoderPageUI;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;

public class PageNavPanel extends Composite {
	public static final double WIDTH = 350.0;
	public static final Unit WIDTH_UNIT = Unit.PX;
	public static final double HEIGHT = 40.0;
	public static final Unit HEIGHT_UNIT = Unit.PX;
	
	private Button backPageButton;
	private Button logOutButton;
	
	private Runnable backHandler;
	private Runnable logoutHandler;
	
	public PageNavPanel() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		//layoutPanel.setSize(WIDTH + WIDTH_UNIT.toString(), HEIGHT + HEIGHT_UNIT.toString());
		
		logOutButton = new Button("Log out");
		logOutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (logoutHandler != null) {
					logoutHandler.run();
				}
			}
		});
		layoutPanel.add(logOutButton);
		layoutPanel.setWidgetRightWidth(logOutButton, 0.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(logOutButton, 0.0, Unit.PX, 27.0, Unit.PX);
		
		backPageButton = new Button("<< Back");
		backPageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (backHandler != null) {
					backHandler.run();
				}
			}
		});
		layoutPanel.add(backPageButton);
		layoutPanel.setWidgetRightWidth(backPageButton, 87.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(backPageButton, 0.0, Unit.PX, 27.0, Unit.PX);
		
		initWidget(layoutPanel);
	}
	
	public void setBackHandler(Runnable backHandler) {
		this.backHandler = backHandler;
	}
	
	public void setLogoutHandler(Runnable logoutHandler) {
		this.logoutHandler = logoutHandler;
	}
}
