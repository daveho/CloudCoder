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

package org.cloudcoder.app.server.submitsvc;

import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * Interface that implementations of problem submission services
 * must implement.  A problem submission service accepts a problem
 * and submitted program text, and (asynchronously) returns a
 * {@link SubmissionResult} containing compilation result and
 * diagnostics, test results, warnings from static code analysis,
 * etc.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public interface ISubmitService {
	/**
	 * Submit a problem and program text.
	 * An {@link IFutureSubmissionResult} will be returned, which
	 * eventually will yield a {@link SubmissionResult}
	 * (which will contain a list of TestResults,
	 * a CompilationResult, and in future versions could contain results
	 * from static error checkers).
	 * 
	 * @param problem      a Problem
	 * @param programText  program text
	 * @return an {@link IFutureSubmissionResult}, which will eventually yield
	 *         a {@link SubmissionResult}
	 */
	public IFutureSubmissionResult submitAsync(Problem problem, List<TestCase> testCaseList, String programText) throws SubmissionException;
}
