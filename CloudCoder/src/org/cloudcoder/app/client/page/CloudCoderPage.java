// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageParams;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.SessionExpiredDialogBox;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.DefaultSubscriptionRegistrar;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Common superclass for all CloudCoder "pages".
 * Provides helper methods for managing session data and event subscribers.
 * Also provides support for allowing the user to log back in
 * when a server-side session timeout occurs.
 */
public abstract class CloudCoderPage {
	private List<Class<?>> sessionObjectClassList;
	private DefaultSubscriptionRegistrar subscriptionRegistrar;
	private Session session;
	private List<Runnable> recoveryCallbackList;
	private String params;
	private IsWidget widget;
	
	/**
	 * Constructor.
	 */
	public CloudCoderPage() {
		this.sessionObjectClassList = new ArrayList<Class<?>>();
		this.subscriptionRegistrar = new DefaultSubscriptionRegistrar();
		this.recoveryCallbackList = new ArrayList<Runnable>();
	}

	/**
	 * Set the Session object that the page should use.
	 * 
	 * @param session the Session object
	 */
	public void setSession(Session session) {
		this.session = session;
	}
	
	/**
	 * Subclasses may call this method when a server-side session
	 * timeout occurs. It will allow the user to log back in,
	 * establishing a new valid server-side session if successful.
	 * A callback is invoked upon a successful recovery, which can be
	 * used to retry the RPC call that failed.
	 * 
	 * @param successfulRecoveryCallback callback to invoke when the session
	 *                                   is successfully recovered
	 */
	public void recoverFromServerSessionTimeout(final Runnable successfulRecoveryCallback) {
		GWT.log("Starting recovery from server side session timeout...");
		
		recoveryCallbackList.add(successfulRecoveryCallback);
		
		if (recoveryCallbackList.size() > 1) {
			// There is already at least one recovery callback registered,
			// meaning that the session expired dialog has already been displayed.
			// Just return without creating another one; on a successful
			// recovery, all registered recovery callbacks will be executed.
			GWT.log("Recovery callbacks pending (dialog is up?), returning");
			return;
		}
		
		final SessionExpiredDialogBox dialog = new SessionExpiredDialogBox();
		
		Runnable callback = new Runnable() {
			@Override
			public void run() {
				String password = dialog.getPassword();
				
				// Try to log back in.
				RPC.loginService.login(session.get(User.class).getUsername(), password, new AsyncCallback<User>() {
					@Override
					public void onSuccess(User result) {
						if (result == null) {
							dialog.setError("Invalid password");
							return;
						}
						
						session.add(StatusMessage.goodNews("Successfully logged back in"));
						
						// Now we should be able to perform whichever operation(s)
						// that were blocked because the session had timed out.
						executeRecoveryCallbacks();
						
						// Hide the dialog.
						dialog.hide();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						dialog.setError("Could not log in: " + caught.getMessage());
					}
				});
			}
		};
		
		dialog.setLoginButtonHandler(callback);
		
		GWT.log("Showing session timeout dialog");
		dialog.center();
	}

	private void executeRecoveryCallbacks() {
		for (Runnable callback : recoveryCallbackList) {
			callback.run();
		}
		recoveryCallbackList.clear();
	}
	
	/**
	 * Add an object to the Session.
	 * When this page is finished, the object (or any object of the
	 * same type which replaced the original object) will be removed
	 * from the Session.
	 * 
	 * @param obj  object to add to the Session
	 */
	protected void addSessionObject(Object obj) {
		session.add(obj);
		sessionObjectClassList.add(obj.getClass());
	}
	
	/**
	 * Remove all objects added to the Session.
	 */
	protected void removeAllSessionObjects() {
		for (Class<?> cls : sessionObjectClassList) {		

			session.remove(cls);
		}
	}

	/**
	 * Create this page's widget.
	 */
	public abstract void createWidget();
	
	/**
	 * Get the Class objects corresponding to the objects
	 * which must be in the {@link Session} in order for this
	 * page to work correctly.  This list does not include
	 * objects such as {@link User} which can be assumed
	 * always to be in the session.
	 * 
	 * @return page object classes
	 */
	public abstract Class<?>[] getRequiredPageObjects();
	
	/**
	 * Set any parameters that were specified as part of the
	 * fragment in the URL.  For example,
	 * if the URL is <i>SITE</i>/cloudcoder/#exercise?c=4,p=5
	 * then the parameters are "c=4,p=5".  ("exercise" in
	 * this case is the fragment name that identifies
	 * {@link DevelopmentPage}.)  These parameters
	 * can be used to allow the page to populate the session
	 * with objects specified by the parameters, allowing
	 * direct links to execises (and any other resources within
	 * the webapp that we'd like to support direct links to.)
	 * 
	 * @param params the parameters
	 */
	public final void setUrlFragmentParams(String params) {
		this.params = params;
	}
	
	/**
	 * Load any required page objects (if page parameters were specified).
	 * Execute the onSuccess/onFailure callbacks as appropriate.
	 */
	public final void loadPageObjects(Runnable onSuccess, ICallback<Pair<String, Throwable>> onFailure) {
		if (params != null) {
			LoadPageObjects loadPageObjects = new LoadPageObjects(this, getRequiredPageObjects(), getSession(), params);
			loadPageObjects.execute(onSuccess, onFailure);
		} else {
			onSuccess.run();
		}
	}
	
	/**
	 * This method is called after the page's UI has been
	 * added to the DOM tree.  This method may safely assume that
	 * the widget set with {@link #setWidget(IsWidget)} is
	 * the widget created by {@link #createWidget()}.
	 */
	public abstract void activate();
	
	/**
	 * This method is called just before the UI is removed
	 * from the client web page.  This default implementation
	 * cancels all subscriptions.  Subclasses may override this
	 * to do additional cleanup.
	 * 
	 * <em>Important</em>: implementations of this method should
	 * not assume that the page widget set with {@link #setWidget(IsWidget)}
	 * belongs to any particular class.  When an error occurs loading
	 * a page, an arbitrary error UI widget could be set.
	 */
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}
	
	/**
	 * Set the widget for this page.
	 * 
	 * @param widget the widget for this page
	 */
	public final void setWidget(IsWidget widget) {
		this.widget = widget;
	}

	/**
	 * @return the widget that is the UI for this page 
	 */
	public final IsWidget getWidget() {
		return widget;
	}
	
	/**
	 * @return the Session object
	 */
	public Session getSession() {
		return session;
	}
	
	/**
	 * Get the SubscriptionRegistrar which keeps track of subscribers
	 * for this view.
	 * 
	 * @return the SubscriptionRegistrar
	 */
	public SubscriptionRegistrar getSubscriptionRegistrar() {
		return subscriptionRegistrar;
	}
	
	/**
	 * Get currently-selected {@link Course} from the {@link Session}.
	 * 
	 * @return currently-selected {@link Course}, or null if no course is selected
	 */
	public Course getCurrentCourse() {
		CourseSelection courseSelection = session.get(CourseSelection.class);
		return courseSelection != null ? courseSelection.getCourse() : null;
	}

	/**
	 * @return the {@link PageId} for this page
	 */
	public abstract PageId getPageId();

	/**
	 * Push {@link PageId}s onto the given {@link PageStack}
	 * to create a reasonable default navigation history for this page.
	 * Pages should push all of the pages that are typically
	 * visited prior to this page.
	 * Only pages where {@link #isActivity()} returns true need
	 * to implement this method.
	 * 
	 * @param pageStack the {@link PageStack}
	 */
	public abstract void initDefaultPageStack(PageStack pageStack);

	/**
	 * Based on the {@link PageId} and the required page objects,
	 * set the URL fragment for this page.
	 */
	public final void setFragment() {
		// Update the anchor in the URL to identify the page.
		// See: http://stackoverflow.com/questions/5402732/gwt-set-url-without-submit
		// TODO: could add params here?
		String fragmentName = this.getPageId().getFragmentName();
		
		// Based on the session objects, create PageParams for this page
		CreatePageParamsFromPageAndSession createPageParams = new CreatePageParamsFromPageAndSession();
		PageParams pageParams = createPageParams.create(this);
		
		// Construct the complete URL fragment (fragment name and page params)
		String hash = fragmentName;
		String fragmentParams = pageParams.unparse();
		GWT.log("fragment params are " + fragmentParams);
		if (!fragmentParams.equals("")) {
			hash = hash + "?" + fragmentParams;
		}
		
		// Construct a new URL for the page (leaving the non-fragment part of the
		// URL unchanged, avoiding a page reload)
		String newURL = Window.Location.createUrlBuilder().setHash(hash).buildString();
		
		// When running in development mode, replacing ":" with "%3A"
		// (due to URL encoding, I guess) appears to trigger a page reload
		// on both Firefox and Chrome, completely bollixing our efforts to use
		// the original URL fragment.  So, undo that bit of unnecessary
		// manipulation of the URL.
		newURL = newURL.replace("%3A", ":");
		
		Window.Location.replace(newURL);
		GWT.log("Setting URL to " + newURL);
	}
	
}
