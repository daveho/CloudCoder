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

package org.cloudcoder.app.client;

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.CoursesAndProblemsPage2;
import org.cloudcoder.app.client.page.DevelopmentPage;
import org.cloudcoder.app.client.page.EditProblemPage;
import org.cloudcoder.app.client.page.InitErrorPage;
import org.cloudcoder.app.client.page.LoginPage;
import org.cloudcoder.app.client.page.PlaygroundPage;
import org.cloudcoder.app.client.page.ProblemAdminPage;
import org.cloudcoder.app.client.page.QuizPage;
import org.cloudcoder.app.client.page.StatisticsPage;
import org.cloudcoder.app.client.page.UserAccountPage;
import org.cloudcoder.app.client.page.UserAdminPage;
import org.cloudcoder.app.client.page.UserProblemSubmissionsPage;
import org.cloudcoder.app.client.page.UserProgressPage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Activity;
import org.cloudcoder.app.shared.model.ActivityObject;
import org.cloudcoder.app.shared.model.InitErrorException;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.DefaultSubscriptionRegistrar;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * CloudCoder entry point class.
 */
public class CloudCoder implements EntryPoint, Subscriber {
	private static CloudCoder theInstance;
	
	private Session session;
	private PageStack pageStack;
	private SubscriptionRegistrar subscriptionRegistrar;
	private CloudCoderPage currentPage;
	
	/**
	 * Get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static CloudCoder getInstance() {
		return theInstance;
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		theInstance = this;
		
		GWT.log("loading, fragment name is " + Window.Location.getHash());
		
		session = new Session();
		pageStack = new PageStack();
		session.add(pageStack);
		subscriptionRegistrar = new DefaultSubscriptionRegistrar();
		
		// Subscribe to PAGE_CHANGE events in the PageStack
		pageStack.subscribe(PageStack.Event.PAGE_CHANGE, this, subscriptionRegistrar);

		// Subscribe to all Session events
		session.subscribeToAll(Session.Event.values(), this, subscriptionRegistrar);

		// Go to whatever initial page is appropriate.
		createInitialPage();
	}

	private void createInitialPage() {
		// See if a URL fragment was specified, and if so, see if it
		// identifies a valid page.
		
		PageId linkPageId_ = null; // page id specified by the link, if any
		String linkPageParams_ = null; // page parameters specified by the link, if any
		
		String fragment = Window.Location.getHash();
		if (fragment != null && !fragment.equals("")) {
			GWT.log("URL fragment is " + fragment);
			String fragmentName = getFragmentName(fragment);
			GWT.log("Fragment name is " + fragmentName);
			
			linkPageId_ = PageId.forFragmentName(fragmentName);
			if (linkPageId_ != null) {
				linkPageParams_ = getFragmentParams(fragment);
				GWT.log("Link params: " + linkPageParams_);
			}
		}
		
		final PageId linkPageId = linkPageId_;
		final String linkPageParams = linkPageParams_;
		
		// Check to see if the user is already logged in.
		RPC.loginService.getUser(new AsyncCallback<User>() {
			@Override
			public void onFailure(Throwable caught) {
				// Special case: if this RPC call (which is the first one)
				// throws an InitErrorException, switch to the InitErrorPage
				// so that the cloudcoder admin can diagnose and resolve
				// the issue.
				if (caught instanceof InitErrorException) {
					changePage(new InitErrorPage());
				} else {
					session.add(StatusMessage.error("Could not check for current login status: " + caught.getMessage()));
					changePage(new LoginPage());
				}
			}

			@Override
			public void onSuccess(User result) {
				if (result == null) {
					// Not logged in, so show LoginPage
					LoginPage loginPage = new LoginPage();
					if (linkPageId != null) {
						GWT.log("Login page will redirect to " + linkPageId + ":" + linkPageParams);
						// A page was linked in the original URL,
						// so have the LoginPage try to navigate to it
						// on a successful login.
						loginPage.setLinkPageId(linkPageId);
						loginPage.setLinkPageParams(linkPageParams);
					}
					changePage(loginPage);
				} else {
					// User is logged in!
					final User user = result;

					// Add user to session
					session.add(user);

					// If a page id was specified as part of the original URL,
					// try to navigate to it without attempting to recover the
					// client's server-side Activity.  (The page id in the
					// link should take precedence.)
					if (linkPageId != null) {
						GWT.log("Already logged in, linking page " + linkPageId + ":" + linkPageParams);
						CloudCoderPage page = createPageForPageId(linkPageId, linkPageParams);
						changePage(page);
					} else {
						GWT.log("Already logged in, no link page id specified, checking server for Activity");
						
						// No page id was specified in the original URL.
						// See if there is a server-side Activity.
						RPC.loginService.getActivity(new AsyncCallback<Activity>() {
							@Override
							public void onFailure(Throwable caught) {
								session.add(StatusMessage.error("Error getting Activity", caught));
								changePage(new CoursesAndProblemsPage2());
							}
	
							@Override
							public void onSuccess(Activity result) {
								
								// Did we find the user's Activity?
								if (result == null) {
									// Don't know what the user's activity was, so take
									// them to the courses/problems page
									changePage(new CoursesAndProblemsPage2());
								} else {
									// We have an activity.  Find the page.
									CloudCoderPage page = getPageForActivity(result);
									
									// Restore the session objects.
									for (Object obj : result.getSessionObjects()) {
										GWT.log("Restoring activity object: " + obj.getClass().getName());
										session.add(obj);
									}
									
									changePage(page);
								}
							}
						});
					}
				}
			}
		});
	}

	/**
	 * Get the fragment name.
	 * E.g., if the fragment is "exercise?c=4,p=5", then the
	 * fragment name is "exercise".
	 * 
	 * @param fragment the fragment
	 * @return the fragment name
	 */
	private String getFragmentName(String fragment) {
		if (fragment.startsWith("#")) {
			fragment = fragment.substring(1);
		}
		int ques = fragment.indexOf('?');
		return (ques >= 0) ? fragment.substring(0, ques) : fragment;
	}

	/**
	 * Get the fragment parameters.
	 * E.g., if the fragment is "exercise?c=4,p=5", then the
	 * parameters are "c=4,p=5".
	 * 
	 * @param fragment the fragment
	 * @return the fragment parameters
	 */
	private String getFragmentParams(String fragment) {
		int ques = fragment.indexOf('?');
		return ques >= 0 ? fragment.substring(ques+1) : "";
	}

	protected CloudCoderPage getPageForActivity(Activity result) {
		String name = result.getName();
		
		// The activity name must be the string representation of a PageId.
		PageId pageId;
		try {
			pageId = PageId.valueOf(name);
		} catch (IllegalArgumentException e) {
			GWT.log("Illegal activity name: " + name);
			pageId = PageId.COURSES_AND_PROBLEMS;
		}

		return createPageForPageId(pageId, null);
	}

	protected CloudCoderPage createPageForPageId(PageId pageId, String pageParams) {
		CloudCoderPage page = createPageForPageId(pageId);
		
		// Create a reasonable PageStack.
		// (Note that we need to disable notifications while we do this,
		// since we're not actually navigating pages.)
		PageStack pageStack = session.get(PageStack.class);
		pageStack.setNotifications(false);
		page.initDefaultPageStack(pageStack);
		pageStack.push(page.getPageId());
		pageStack.setNotifications(true);
		
		// Set initial page parameters (if any)
		if (pageParams != null) {
			page.setUrlFragmentParams(pageParams);
		}
		
		return page;
	}

	private CloudCoderPage createPageForPageId(PageId pageId) {
		CloudCoderPage page;
		switch (pageId) {
		case COURSES_AND_PROBLEMS:
			page = new CoursesAndProblemsPage2();
			break;
		case DEVELOPMENT:
		    page = new DevelopmentPage();
//		    page = new DevelopmentPage2();
			break;
		case PROBLEM_ADMIN:
			page = new ProblemAdminPage();
			break;
		case EDIT_PROBLEM:
			page= new EditProblemPage();
			break;
		case USER_ADMIN:
			page = new UserAdminPage();
			break;
		case STATISTICS:
			page = new StatisticsPage();
			break;
		case USER_PROGRESS:
			page = new UserProgressPage();
			break;
		case QUIZ:
			page = new QuizPage();
			break;
		case USER_ACCOUNT:
			page = new UserAccountPage();
			break;
		case USER_PROBLEM_SUBMISSIONS:
			page = new UserProblemSubmissionsPage();
			break;
		case PLAYGROUND_PAGE:
		    page = new PlaygroundPage();
		    break;
		default:
			// This shouldn't happen (can't find page for Activity),
			// but if it does, go to the courses and problems page.
			GWT.log("Don't know what kind of page to create for " + pageId);
			page = new CoursesAndProblemsPage2();
			break;
		}
		return page;
	}
	
	protected Activity getActivityForPage(CloudCoderPage page) {
		return getActivityForSessionAndPage(page, session);
	}

	/**
	 * Create an {@link Activity} for current page and session.
	 *  
	 * @param page     current page
	 * @param session  current session
	 * @return the {@link Activity}
	 */
	public static Activity getActivityForSessionAndPage(CloudCoderPage page, Session session) {
		// The activity name is the page's PageId (as a string)
		Activity activity = new Activity(page.getPageId().toString());
		
		// Record the Session objects (the ones that are ActivityObjects)
		for (Object obj : session.getObjects()) {
			if (obj instanceof ActivityObject) {
				GWT.log("Adding " + obj.getClass().getName() + " to Activity");
				activity.addSessionObject((ActivityObject) obj);
			}
		}
		
		return activity;
	}
	
	private void changePage(CloudCoderPage page) {
		if (currentPage != null) {
			currentPage.deactivate();
			RootLayoutPanel.get().remove(currentPage.getWidget());
			
			// make sure there is no StatusMessage from the previous page
			session.remove(StatusMessage.class);
		}
		page.setSession(session);

		// Create the page's Widget and add it to the DOM tree.
		// Leave a 10 pixel border around the page widget.
		page.createWidget();
		IsWidget w = page.getWidget();
		RootLayoutPanel.get().add(w);
		RootLayoutPanel.get().setWidgetLeftRight(w, 10.0, Unit.PX, 10.0, Unit.PX);
		RootLayoutPanel.get().setWidgetTopBottom(w, 10.0, Unit.PX, 10.0, Unit.PX);
		
		// Update the anchor in the URL to identify the page.
		// See: http://stackoverflow.com/questions/5402732/gwt-set-url-without-submit
		// TODO: could add params here?
		String hash = page.getPageId().getFragmentName();
		String newURL = Window.Location.createUrlBuilder().setHash(hash).buildString();
		
		// When running in development mode, replacing ":" with "%3A"
		// (due to URL encoding, I guess) appears to trigger a page reload
		// on both Firefox and Chrome, completely bollixing our efforts to use
		// the original URL fragment.  So, undo that bit of unnecessary
		// manipulation of the URL.
		newURL = newURL.replace("%3A", ":");
		
		Window.Location.replace(newURL);
		GWT.log("Setting URL to " + newURL);
		
		// Now it is safe to activate the page
		page.loadPageObjectsAndActivate();
		currentPage = page;
		
		// Inform the server of the Activity (page) that the user is now working on,
		// if the page requests it.  Otherwise set the activity to null.
		Activity activity = page.isActivity() ? getActivityForPage(page) : null;
		RPC.loginService.setActivity(activity, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				// There's not really anything useful we can do here.
				GWT.log("Couldn't set activity on server?", caught);
			}

			@Override
			public void onSuccess(Void result) {
				// Nothing to do
			}
		});
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		// This is where we monitor for events that indicate page changes.
		// The PageStack makes this pretty straightforward.
		if (key == PageStack.Event.PAGE_CHANGE) {
			PageId current = session.get(PageStack.class).getTop();
			changePage(createPageForPageId(current));
		} else if (key == Session.Event.LOGOUT) {
			// On logout, clear the Session and PageStack,
			// add the PageStack back to the Session,
			// and go back to the LoginPage.
			session.clear();
			pageStack.clear();
			session.add(pageStack);
			changePage(new LoginPage());
		}
	}
	
	/**
	 * Create and activate the specified page following a successful
	 * login.
	 * 
	 * @param pageId     the page id
	 * @param pageParams the page params, if any
	 */
	public void createPostLoginPage(PageId pageId, String pageParams) {
		GWT.log("Post-login: go to page " + pageId + (pageParams != null ? (", params=" + pageParams) : ""));
		CloudCoderPage page = createPageForPageId(pageId, pageParams);
		changePage(page);
	}
}
