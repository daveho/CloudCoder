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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.ProblemSubmissionHistory;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * View the {@link ProblemSubmissionHistory} using a slider (allowing
 * the user to the slider to navigate the submissions).
 * Also has forward and back buttons. 
 * 
 * @author David Hovemeyer
 */
public class ProblemSubmissionHistorySliderView extends Composite implements SessionObserver, Subscriber {
	public static final double HEIGHT_PX = 28.0;

	private ProblemSubmissionHistory problemSubmissionHistory;
	private SubscriptionRegistrar registrar;

	private Slider submissionSlider;
	private Button prevSubmissionButton;
	private Button nextSubmissionButton;


	public ProblemSubmissionHistorySliderView() {
		LayoutPanel panel = new LayoutPanel();
		
		this.submissionSlider = new Slider("ccSubSlider");
		submissionSlider.setMinimum(1);
		submissionSlider.setMaximum(10);
		submissionSlider.addListener(new SliderListener() {

			@Override
			public void onStop(SliderEvent e) {
			}
			
			@Override
			public void onStart(SliderEvent e) {
			}
			
			@Override
			public boolean onSlide(SliderEvent e) {
				return true;
			}
			
			@Override
			public void onChange(SliderEvent e) {
				if (problemSubmissionHistory == null) {
					return;
				}
				
				int selected = submissionSlider.getValue();
				GWT.log("Slider value is " + selected);
				
				// Only change the selection in the submission history if it's
				// a value that differs from the slider.
				int selectedInHistory = problemSubmissionHistory.getSelected();
				if (selected != selectedInHistory) {
					problemSubmissionHistory.setSelected(selected);
				}
			}
		});
		panel.add(submissionSlider);
		panel.setWidgetLeftRight(submissionSlider, (38.0 * 2), Unit.PX, 0, Unit.PX);
		panel.setWidgetTopHeight(submissionSlider, 0.0, Unit.PX, HEIGHT_PX, Unit.PX);

		this.prevSubmissionButton = new Button("<");
		panel.add(prevSubmissionButton);
		panel.setWidgetLeftWidth(prevSubmissionButton, 0.0, Unit.PX, 30, Unit.PX);
		panel.setWidgetTopHeight(prevSubmissionButton, 0.0, Unit.PX, HEIGHT_PX, Unit.PX);
		prevSubmissionButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (problemSubmissionHistory != null) {
					problemSubmissionHistory.back();
				}
			}
		});

		this.nextSubmissionButton = new Button(">");
		panel.add(nextSubmissionButton);
		panel.setWidgetLeftWidth(nextSubmissionButton, 38.0, Unit.PX, 30, Unit.PX);
		panel.setWidgetTopHeight(nextSubmissionButton, 0.0, Unit.PX, HEIGHT_PX, Unit.PX);
		nextSubmissionButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (problemSubmissionHistory != null) {
					problemSubmissionHistory.forward();
				}
			}
		});

		initWidget(panel);
	}
	
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.registrar = subscriptionRegistrar;
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		this.problemSubmissionHistory = session.get(ProblemSubmissionHistory.class);
		if (problemSubmissionHistory != null) {
			attach();
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof ProblemSubmissionHistory) {
			if (problemSubmissionHistory != null) {
				detach();
			}
			
			// Subscribe to all ProblemSubmissionHistory events
			this.problemSubmissionHistory = (ProblemSubmissionHistory) hint;
			attach();
		} else if (key == ProblemSubmissionHistory.Event.SET_SUBMISSION_RECEIPT_LIST) {
			// Set slider min/max
			submissionSlider.setMinimum(0);
			submissionSlider.setMaximum(problemSubmissionHistory.getNumSubmissions() - 1);
		} else if (key == ProblemSubmissionHistory.Event.SET_SELECTED) {
			int selected = problemSubmissionHistory.getSelected();
			if (submissionSlider.getValue() != selected) {
				submissionSlider.setValue(selected);
			}
		}
	}

	private void attach() {
		problemSubmissionHistory.subscribeToAll(ProblemSubmissionHistory.Event.values(), this, registrar);
	}

	private void detach() {
		// Unsubscribe from previous ProblemSubmissionHistory
		problemSubmissionHistory.unsubscribeFromAll(this);
	}
}
