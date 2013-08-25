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

package org.cloudcoder.app.server.rpc;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.server.login.ILoginProvider;
import org.cloudcoder.app.server.login.LoginProviderServletContextListener;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.InitErrorList;
import org.cloudcoder.app.shared.model.Activity;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.InitErrorException;
import org.cloudcoder.app.shared.model.LoginSpec;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link LoginService}.
 * The actual authentication decision is made by whatever
 * {@link ILoginProvider} implementation was created by
 * {@link LoginProviderServletContextListener}: this allows multiple
 * login providers to be supported (chosen at configuration time.)
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(LoginServiceImpl.class);
    
    /**
     * Set this to true to have very short session timeouts.
     * Useful for testing that RPC calls that fail because of session
     * timeouts can be successfully completed following a successful
     * call to CloudCoderPage.recoverFromServerSessionTimeout().
     * Should NOT be set to true for production!
     */
    private static boolean DEBUG_SESSION_TIMEOUTS = false;

    /**
     * Default session timeout in seconds.  Defaults to 30 minutes.
     */
    private static final int SESSION_TIMEOUT_IN_SECONDS = 30 * 60;
    
    @Override
    public LoginSpec getLoginSpec() {
    	ILoginProvider provider = LoginProviderServletContextListener.getProviderInstance();
    	
    	LoginSpec loginInfo = new LoginSpec();
    	
    	ConfigurationSetting setting = Database.getInstance().getConfigurationSetting(ConfigurationSettingName.PUB_TEXT_INSTITUTION);
    	String institutionName = setting != null ? setting.getValue() : "Unknown institution";
    	loginInfo.setInstitutionName(institutionName);
    	
    	
    	boolean usernamePasswordRequired = provider.isUsernamePasswordRequired();
		loginInfo.setUsernamePasswordRequired(usernamePasswordRequired);
    	
		if (!usernamePasswordRequired) {
	    	loginInfo.setPreAuthorizedUsername(provider.getPreAuthorizedUsername(getThreadLocalRequest()));
		}
		
		return loginInfo;
    }

	@Override
	public User login(String userName, String password) {
	    // Can this method be called anywhere?
	    // Does AdminAuthorizationFilter have access to the ServletConfig?
        
		User user=null;

		// Get the configured ILoginProvider instance
		ILoginProvider provider = LoginProviderServletContextListener.getProviderInstance();
		
		// Try to log in
		user = provider.login(userName, password, getThreadLocalRequest());
		
	    if (user == null) {
	    	logger.info("Login failure for user {}", userName);
	    }

		if (user != null) {
			// Set User object in server HttpSession so that other
			// servlets will know that the client is logged in
			HttpSession session = getThreadLocalRequest().getSession();
			session.setAttribute(SessionAttributeKeys.USER_KEY, user);
			
			// Set session timeout.
			int maxInactive = SESSION_TIMEOUT_IN_SECONDS;
			if (DEBUG_SESSION_TIMEOUTS) {
				// This is useful for testing automatic retry of RPC calls
				// that fail because of a server session timeout:
				// expires server sessions after 20 seconds of inactivity.
				maxInactive = 20;
			}
			session.setMaxInactiveInterval(maxInactive);
		}
		
		return user;
	}

    @Override
	public void logout() {
		HttpSession session = getThreadLocalRequest().getSession();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> attributeNames = (Enumeration<String>) session.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attr = attributeNames.nextElement();
			session.removeAttribute(attr);
		}
	}
    
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#getUser()
	 */
	@Override
	public User getUser() throws InitErrorException {
		// Special case: this is the first RPC call that is made by the
		// client.  If a fatal init error occurred here in the server side
		// of the webapp, throw an InitErrorException to let the client
		// know to display a diagnostic page (that the cloudcoder admin
		// can use to resolve the issue.)
		if (InitErrorList.instance().hasErrors()) {
			throw new InitErrorException();
		}
		
		return (User) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.USER_KEY);
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#getActivity()
	 */
	@Override
	public Activity getActivity() {
		return (Activity) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.ACTIVITY_KEY);
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#setActivity(org.cloudcoder.app.shared.model.Activity)
	 */
	@Override
	public void setActivity(Activity activity) {
		getThreadLocalRequest().getSession().setAttribute(SessionAttributeKeys.ACTIVITY_KEY, activity);
	}
	
	@Override
	public String[] getInitErrorList() {
		List<String> initErrorList = InitErrorList.instance().getErrorList();
		return initErrorList.toArray(new String[initErrorList.size()]);
	}
}
