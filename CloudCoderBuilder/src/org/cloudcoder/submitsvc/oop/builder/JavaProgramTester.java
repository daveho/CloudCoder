// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.submitsvc.oop.builder;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;

/**
 * Tester to compile and run complete Java programs
 * (consisting of a top level class with a main method).
 * Output is judged for correctness by testing each line
 * against a regexp; the same technique as {@link CProgramTester}.
 * 
 * @author David Hovemeyer
 */
public class JavaProgramTester implements ITester {

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.ITester#testSubmission(org.cloudcoder.app.shared.model.Submission)
	 */
	@Override
	public SubmissionResult testSubmission(Submission submission) {
		Problem problem = submission.getProblem();
		
		String programText = submission.getProgramText();

		FindJavaPackageAndClassNames packageAndClassNames = new FindJavaPackageAndClassNames();
		packageAndClassNames.determinePackageAndClassNames(programText);
		
		//InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();
		
//		System.out.println(packageAndClassNames.getLeft());
//		System.out.println(packageAndClassNames.getRight());
		
		return null;
	}
}
