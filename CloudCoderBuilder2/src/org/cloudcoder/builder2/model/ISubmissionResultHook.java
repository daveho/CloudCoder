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

package org.cloudcoder.builder2.model;

import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionResultAnnotation;

/**
 * Callback interface for post-processing a {@link SubmissionResult}
 * before it is delivered to the webapp (or whatever application
 * requested the testing of the submission.)  One intended use
 * of this callback is to allow the addition of
 * {@link SubmissionResultAnnotation}s to the SubmissionResult.
 * 
 * @author David Hovemeyer
 */
public interface ISubmissionResultHook {
	/**
	 * Called to post-process the {@link SubmissionResult}.
	 * 
	 * @param result the {@link SubmissionResult}
	 */
	public void invoke(SubmissionResult result);
}
