// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, Shane Bonner
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

import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.SubmissionStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * Widget representing an item in the {@link ExerciseSummaryView}.
 * Provides a quick visual summary of the status of a single exercise.
 * 
 * @author Shane Bonner
 * @author David Hovemeyer
 */
public class ExerciseSummaryItem extends Composite {
	private int index;
	private SubmissionStatus status;
	private HTML html;
	private ICallback<ExerciseSummaryItem> clickHandler;

	/**
	 * Constructor.
	 */
	public ExerciseSummaryItem() {
		this.html = new HTML("<span></span>");
		html.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClickEvent();
			}
		});
		setStatus(SubmissionStatus.NOT_STARTED); // just a default status
		initWidget(html);
	}

	protected void onClickEvent() {
		if (clickHandler != null) {
			clickHandler.call(this);
		}
	}

	/**
	 * Set the index of this item.
	 * 
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * Set a callback to be invoked when the user clicks on this
	 * item.  The {@link ExerciseSummaryView} will use this to
	 * handle item clicks.
	 * 
	 * @param clickHandler the click handler to set
	 */
	public void setClickHandler(ICallback<ExerciseSummaryItem> clickHandler) {
		this.clickHandler = clickHandler;
	}
	
	/**
	 * @return the index of this item
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Get this item's {@link SubmissionStatus}.
	 * 
	 * @return this item's {@link SubmissionStatus}
	 */
	public SubmissionStatus getStatus() {
		return status;
	}

	/**
	 * Set the {@link SubmissionStatus} of the item.
	 * 
	 * @param status the {@link SubmissionStatus} to set
	 */
	public void setStatus(SubmissionStatus status) {
		this.status = status;
		this.html.setStyleName("cc-exerciseSummaryItem"); // reset to just the base style
		
		// Set status-specific style
		switch (status) {
		case NOT_STARTED:
			html.setStyleName("cc-exerciseNotStarted", true); break;
		case STARTED:
			html.setStyleName("cc-exerciseStarted", true); break;
		case TESTS_FAILED:
			html.setStyleName("cc-exerciseTestsFailed", true); break;
		case COMPILE_ERROR:
			html.setStyleName("cc-exerciseCompileError", true); break;
		case BUILD_ERROR:
			html.setStyleName("cc-exerciseBuildError", true); break;
		case TESTS_PASSED:
			html.setStyleName("cc-exerciseTestsPassed", true); break;
		default:
			html.setStyleName("cc-exerciseStatusUnknown", true); break;
		}
	}
	
	/**
	 * Set a tooltip for this item.
	 * The {@link ExerciseSummaryView} sets tool tips indicating the
	 * name of the exercise and the status.
	 * 
	 * @param tooltip the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		html.getElement().setAttribute("title", tooltip);
	}
}
