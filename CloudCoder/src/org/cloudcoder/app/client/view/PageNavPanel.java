package org.cloudcoder.app.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.dom.client.Style.Unit;

public class PageNavPanel extends Composite {
	public static final double WIDTH = 350.0;
	public static final Unit WIDTH_UNIT = Unit.PX;
	public static final double HEIGHT = 40.0;
	public static final Unit HEIGHT_UNIT = Unit.PX;
	
	public PageNavPanel() {
		LayoutPanel layoutPanel = new LayoutPanel();
		
		//layoutPanel.setSize(WIDTH + WIDTH_UNIT.toString(), HEIGHT + HEIGHT_UNIT.toString());
		
		Button btnLogOut = new Button("Log out");
		layoutPanel.add(btnLogOut);
		layoutPanel.setWidgetRightWidth(btnLogOut, 0.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(btnLogOut, 0.0, Unit.PX, 27.0, Unit.PX);
		
		Button button = new Button("<< Back");
		layoutPanel.add(button);
		layoutPanel.setWidgetRightWidth(button, 87.0, Unit.PX, 81.0, Unit.PX);
		layoutPanel.setWidgetTopHeight(button, 0.0, Unit.PX, 27.0, Unit.PX);
		
		initWidget(layoutPanel);
	}
}
