package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class CoursesAndProblemsPage extends CloudCoderPage {
	private CoursesAndProblemsPageUI ui;
	
	public CoursesAndProblemsPage() {
	}

	@Override
	public void createWidget() {
		// Create the UI
		ui = new CoursesAndProblemsPageUI();
		ui.setPage(this);
	}
	
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
		
		// Load courses
		RPC.getCoursesAndProblemsService.getCourses(new AsyncCallback<Course[]>() {
			@Override
			public void onSuccess(Course[] result) {
				addSessionObject(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// FIXME: display error
			}
		});
	}
	
	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
		removeAllSessionObjects();
	}
	
	@Override
	public Widget getWidget() {
		return ui;
	}
}
