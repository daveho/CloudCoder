package org.cloudcoder.app.client.page;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;


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
		// Populate initial empty lists of courses and problems.
		// The UI will initiate dynamically loading them.
		addSessionObject(new Course[0]);
		addSessionObject(new Problem[0]);
		
		ui.activate(getSession(), getSubscriptionRegistrar());
	}
	
	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
		removeAllSessionObjects();
	}
	
	@Override
	public CloudCoderPageUI getWidget() {
		return ui;
	}
}
