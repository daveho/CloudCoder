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

package org.cloudcoder.app.shared.model;

import org.cloudcoder.app.shared.model.SubmissionReceipt;

/**
 * Interface for model classes that either are a {@link SubmissionReceipt}
 * or contain one.  Since a {@link SubmissionReceipt} represents a user's
 * score on a {@link Problem}, this interface is useful for generalizing UI
 * widgets that display a score.
 * 
 * @author David Hovemeyer
 */
public interface IHasSubmissionReceipt {
	/**
	 * Get the {@link SubmissionReceipt}.
	 * 
	 * @return the {@link SubmissionReceipt}
	 */
	public SubmissionReceipt getReceipt();
}
