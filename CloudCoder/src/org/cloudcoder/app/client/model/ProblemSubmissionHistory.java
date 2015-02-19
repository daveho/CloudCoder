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

package org.cloudcoder.app.client.model;

import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;

import com.google.gwt.core.client.GWT;

/**
 * Model object containing a list of {@link SubmissionReceipt}s
 * representing a user's work on a problem.  One of the
 * submission receipts is the selected one - i.e., the one
 * being viewed in the UI.
 * 
 * @author David Hovemeyer
 */
public class ProblemSubmissionHistory extends Publisher {
	public enum Event {
		SET_SUBMISSION_RECEIPT_LIST,
		SET_SELECTED,
	}
	
	private SubmissionReceipt[] submissionReceiptList;
	private int selected;
	
	/**
	 * Constructor.
	 */
	public ProblemSubmissionHistory() {
		
	}

	/**
	 * Get the number of submissions in the submission history.
	 * 
	 * @return number of submissions
	 */
	public int getNumSubmissions() {
		return submissionReceiptList != null ? submissionReceiptList.length : 0;
	}
	
	/**
	 * Get the {@link SubmissionReceipt} at given index.
	 * 
	 * @param index the index
	 * @return the {@link SubmissionReceipt}
	 */
	public SubmissionReceipt getSubmissionReceipt(int index) {
		SubmissionReceipt result = submissionReceiptList[index];
		
		if (result == null) {
			GWT.log("Getting submission receipt " + index + ", but it's null, WTF?");
		}
		
		return result;
	}
	
	/**
	 * Set the list of {@link SubmissionReceipt}s.
	 * 
	 * @param submissionReceiptList the submissionReceiptList to set
	 */
	public void setSubmissionReceiptList(SubmissionReceipt[] submissionReceiptList) {
		System.out.println("Setting submission receipt list");
		boolean foundNull = false;
		for (int i = 0; i < submissionReceiptList.length; i++) {
			if (submissionReceiptList[i] == null) {
				GWT.log("Found a null value!");
				foundNull = true;
			}
		}
		if (!foundNull) {
			GWT.log("No null values in submission receipt list");
		}
		this.submissionReceiptList = submissionReceiptList;
		notifySubscribers(Event.SET_SUBMISSION_RECEIPT_LIST, submissionReceiptList);
	}
	
//	/**
//	 * Get the list of {@link SubmissionReceipt}s
//	 * 
//	 * @return the submissionReceiptList
//	 */
//	public SubmissionReceipt[] getSubmissionReceiptList() {
//		return submissionReceiptList;
//	}
	
	/**
	 * Set the index of the selected {@link SubmissionReceipt}.
	 * 
	 * @param selected the selected to set
	 */
	public void setSelected(int selected) {
		this.selected = selected;
		notifySubscribers(Event.SET_SELECTED, (Integer) selected);
	}
	
	/**
	 * Set the index of the selected {@link SubmissionReceipt}.
	 * 
	 * @return the selected
	 */
	public int getSelected() {
		return selected;
	}

	/**
	 * Change selected submission by going back one submission.
	 */
	public void back() {
		if (submissionReceiptList != null && selected > 0) {
			setSelected(selected - 1);
		}
	}

	/**
	 * Change selected submission by going forward on submission.
	 */
	public void forward() {
		if (submissionReceiptList != null && selected < submissionReceiptList.length - 1) {
			setSelected(selected + 1);
		}
	}
}
