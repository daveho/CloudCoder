// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * View to show the current {@link StatusMessage}.
 * 
 * @author David Hovemeyer
 */
public class StatusMessageView extends Composite implements Subscriber, SessionObserver {
	public static final double HEIGHT_PX = 32.0;
	
	private static final String BLANK_ICON_URL = "cloudcoder/images/blank-icon-sm.png";
	private static final String INFO_ICON_URL = "cloudcoder/images/info-icon-sm.png";
	private static final String ERROR_ICON_URL = "cloudcoder/images/error-icon-sm.png";
	private static final String CHECK_MARK_ICON_URL = "cloudcoder/images/check-mark-icon-sm.png";
	private static final String ROLLER_ICON_URL = "cloudcoder/images/roller-sm.gif";
	
	private Image icon;
	private InlineLabel label;

	public StatusMessageView() {
		FlowPanel panel = new FlowPanel();
		
		icon = new Image();
		icon.setUrl(BLANK_ICON_URL);
		icon.setStylePrimaryName("cc-statusIcon");
		panel.add(icon);

		label = new InlineLabel();
		label.setStylePrimaryName("cc-statusMessageNone");
		panel.add(label);
		
		initWidget(panel);
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
			icon.setUrl(INFO_ICON_URL);
			label.setStyleName("cc-statusMessageInformation");
			break;
		case ERROR:
			icon.setUrl(ERROR_ICON_URL);
			label.setStyleName("cc-statusMessageError");
			break;
		case GOOD_NEWS:
			icon.setUrl(CHECK_MARK_ICON_URL);
			label.setStyleName("cc-statusMessageGoodNews");
			break;
		case PENDING:
			icon.setUrl(ROLLER_ICON_URL);
			label.setStyleName("cc-statusMessagePending");
			break;
		}
		label.setText(statusMessage.getMessage());
	}
}
