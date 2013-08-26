// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ILoginView;
import org.cloudcoder.app.client.view.PreauthorizedUserLoginView;
import org.cloudcoder.app.client.view.UsernamePasswordLoginView;
import org.cloudcoder.app.shared.model.LoginSpec;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Login page.
 */
public class LoginPage extends CloudCoderPage {
	private static final double LOGIN_VIEW_TOP_PX = 32.0;
	private static final double LOGIN_VIEW_WIDTH_PX = 340.0;
	private static final double LOGIN_VIEW_HEIGHT_PX = 480.0;
	private static final double LOGO_TOP_PX = 120.0;
	
	/**
	 * UI class for LoginPage.
	 */
	private class UI extends LayoutPanel implements SessionObserver {
		private InlineLabel pageTitleLabel;
		private ILoginView loginView;
		protected LoginSpec loginSpec;
		
		public UI() {
			Image cloudCoderLogoImage = new Image(GWT.getModuleBaseURL() + "images/CloudCoderLogo-med.png");
			add(cloudCoderLogoImage);
			setWidgetLeftWidth(cloudCoderLogoImage, LOGIN_VIEW_WIDTH_PX + 10.0, Unit.PX, 240.0, Unit.PX);
			setWidgetTopHeight(cloudCoderLogoImage, LOGO_TOP_PX, Unit.PX, 165.0, Unit.PX);
			
			pageTitleLabel = new InlineLabel("");
			pageTitleLabel.setStylePrimaryName("cc-pageTitle");
			add(pageTitleLabel);
			setWidgetLeftWidth(pageTitleLabel, 57.0, Unit.PX, 533.0, Unit.PX);
			setWidgetTopHeight(pageTitleLabel, 44.0, Unit.PX, 31.0, Unit.PX);
			
			InlineLabel welcomeLabel = new InlineLabel("Welcome to CloudCoder at");
			add(welcomeLabel);
			setWidgetLeftWidth(welcomeLabel, 57.0, Unit.PX, 313.0, Unit.PX);
			setWidgetTopHeight(welcomeLabel, 23.0, Unit.PX, 15.0, Unit.PX);
		}
		
		@Override
		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			RPC.loginService.getLoginSpec(new AsyncCallback<LoginSpec>() {
				@Override
				public void onFailure(Throwable caught) {
					GWT.log("Failure to get LoginSpec", caught);
				}
				
				public void onSuccess(LoginSpec result) {
					loginSpec = result;
					
					setPubTextInstitution(loginSpec.getInstitutionName());
					
					if (loginSpec.isUsernamePasswordRequired()) {
						loginView = new UsernamePasswordLoginView();
					} else {
						GWT.log("Using preauthorized user login...");
						
						if (loginSpec.getPreAuthorizedUsername() != null) {
							// User is preauthorized.
							// Just use a UI that prompts the user to press a button
							// to log in.
							loginView = new PreauthorizedUserLoginView();
						} else {
							// Try logging in with username and password.
							loginView = new UsernamePasswordLoginView();
						}
					}

					loginView.setLoginSpec(loginSpec);
					loginView.setLoginCallback(new Runnable() {
						@Override
						public void run() {
							attemptLogin();
						}
					});
					
					add(loginView);
					setWidgetLeftWidth(loginView, 0.0, Unit.PX, LOGIN_VIEW_WIDTH_PX, Unit.PX);
					setWidgetTopHeight(loginView, LOGIN_VIEW_TOP_PX, Unit.PX, LOGIN_VIEW_HEIGHT_PX, Unit.PX);
					
					// For whatever reason, activating the login view (which will
					// probably set the focus of a UI component, e.g. the username
					// textbox) doesn't seem to work if done synchronously.
					// Doing it after a brief delay seems to work.
					new Timer() {
						@Override
						public void run() {
							loginView.activate();
						}
					}.schedule(20);
				}
			});
		}

		protected void attemptLogin() {
			String username = loginView.getUsername();
			String password = loginView.getPassword();
			if (username.equals("") || password.equals("")) {
				loginView.setErrorMessage("Please enter your username and password");
				return;
			}
			
			loginView.setInfoMessage("Logging in...");
			RPC.loginService.login(username, password, new AsyncCallback<User>() {
				@Override
				public void onFailure(Throwable caught) {
					loginView.setInfoMessage("");
					loginView.setErrorMessage("Error communicating with server (are you connected to the network?)");
				}
				
				@Override
				public void onSuccess(User result) {
					if (result == null) {
						loginView.setInfoMessage("");
						loginView.setErrorMessage("Username and password not found");
					} else {
						// Successful login!
						getSession().add(loginSpec);
						getSession().add(result);
						getSession().get(PageStack.class).push(PageId.COURSES_AND_PROBLEMS);
					}
				}
			});
		}

		public void setPubTextInstitution(String result) {
			pageTitleLabel.setText(result);
		}
	}

	private UI ui;

	@Override
	public void createWidget() {
		ui = new UI();
	}
	
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
	}

	@Override
	public IsWidget getWidget() {
		return ui;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
	 */
	@Override
	public boolean isActivity() {
		return false;
	}
	
	@Override
	public PageId getPageId() {
		return PageId.LOGIN;
	}
	
	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		throw new IllegalStateException("Not an activity");
	}
}
