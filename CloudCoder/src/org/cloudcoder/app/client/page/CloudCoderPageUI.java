package org.cloudcoder.app.client.page;


import com.google.gwt.user.client.ui.IsWidget;

public interface CloudCoderPageUI extends IsWidget, SessionObserver {
	public void setPage(CloudCoderPage page);
}
