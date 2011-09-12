package org.cloudcoder.app.client;

import org.cloudcoder.app.client.page.LoginPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CloudCoder implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Session session = new Session();
		
		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
		
		LoginPage loginPage = new LoginPage();
		loginPage.setSession(session);
		rootLayoutPanel.add(loginPage);
	}
}
