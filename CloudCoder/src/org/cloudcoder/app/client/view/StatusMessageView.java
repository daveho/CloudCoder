package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

public class StatusMessageView extends Composite implements Subscriber, SessionObserver {
	public static final double HEIGHT = 24.0;
	public static final Unit HEIGHT_UNIT = Unit.PX;
	
	private InlineLabel label;

	public StatusMessageView() {
		label = new InlineLabel();
		label.setStylePrimaryName("ccStatusMessageInformation");
		initWidget(label);
	}

	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		StatusMessage statusMessage = session.get(StatusMessage.class);
		if (statusMessage != null) {
			setStatusMessage(statusMessage);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof StatusMessage) {
			setStatusMessage((StatusMessage) hint);
		}
	}
	
	private void setStatusMessage(StatusMessage statusMessage) {
		switch (statusMessage.getCategory()) {
		case INFORMATION:
			label.setStyleName("cc-statusMessageInformation");
			break;
		case ERROR:
			label.setStyleName("cc-statusMessageError");
			break;
		}
		label.setText(statusMessage.getMessage());
	}
}
