// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * Interface that implementations of problem submission services
 * must implement.  A problem submission service accepts a problem
 * and submitted program text, and returns a list of test results.
 * 
 * @author David Hovemeyer
 */
public interface ISubmitService {
	/**
	 * Submit a problem and program text.
	 * The program text will be compiled and executed on each test,
	 * and a list of TestResults will describe the outcome
	 * of each test.
	 * 
	 * @param problem      a Problem
	 * @param programText  program text
	 * @return A SubmissionResult (which will contain a list of TestResults, a CompilationResult, 
	 *  and in future versions could contain results from static error checkers)
	 */
	public SubmissionResult submit(Problem problem, List<TestCase> testCaseList, String programText) throws SubmissionException;
}
