// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.shared.model.IHasSubmissionReceipt;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Cell displaying a bar representation of a student's best submission
 * for a particular {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class BestScoreBarCell<E extends IHasSubmissionReceipt> extends AbstractCell<E> {
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, E value, SafeHtmlBuilder sb) {
		if (value == null) {
			// Data not available, or student did not attempt this problem
			appendNoInfoBar(sb);
			return;
		}
		
		// If we don't know the total number of test cases, don't display anything
		SubmissionReceipt receipt = value.getReceipt();
		if (receipt == null || receipt.getNumTestsAttempted() == 0) {
			appendNoInfoBar(sb);
			return;
		}
		
		int numTests = receipt.getNumTestsAttempted();
		int numPassed = receipt.getNumTestsPassed();
		
		StringBuilder buf = new StringBuilder();
		buf.append("<div class=\"cc-barOuter\"><div class=\"cc-barInner\" style=\"width: ");
		int pct = (numPassed * 10000) / (numTests * 100);
		buf.append(pct);
		buf.append("%\"></div></div>");
		
		String s= buf.toString();
		
		sb.append(SafeHtmlUtils.fromSafeConstant(s));
	}

	private void appendNoInfoBar(SafeHtmlBuilder sb) {
		sb.append(SafeHtmlUtils.fromSafeConstant("<div class=\"cc-barNoInfo\"></div>"));
	}
}