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

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * Display Philip Guo's fantastic {@link PythonTutor http://www.pythontutor.com},
 * using student's current Python code along with a driver to run it using
 * the non-secret test inputs.
 * 
 * @author David Hovemeyer
 */
public class PythonTutorView extends Composite {
	private String code;
	private Problem problem;
	private TestCase[] nonSecretTestCases;

	/**
	 * Constructor.
	 * 
	 * @param code                the student's Python code
	 * @param problem             the {@link Problem}
	 * @param nonSecretTestCases  the non-secret {@link TestCase}s for the problem
	 * @param width               the width of the visualizer iframe, in pixels
	 * @param height              the height of the visualizer iframe, in pixels
	 */
	public PythonTutorView(String code, Problem problem, TestCase[] nonSecretTestCases, int width, int height) {
		this.code = code;
		this.problem = problem;
		this.nonSecretTestCases = nonSecretTestCases;

		// Generate "scaffolded" code with all of the non-secret test cases
		String scaffoldedCode = getScaffoldedCode();
		String encodedScaffoldedCode = URL.encodeQueryString(scaffoldedCode);
		
		// See: https://github.com/pgbovine/OnlinePythonTutor/blob/master/v3/docs/embedding-HOWTO.md
		HTML iframe = new HTML("<iframe width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\"" +
				" src=\"http://pythontutor.com/iframe-embed.html#code=" + encodedScaffoldedCode + "&py=2\"></iframe>");
		initWidget(iframe);
	}

	private String getScaffoldedCode() {
		StringBuilder buf = new StringBuilder();
		
		buf.append(code);
		buf.append("\n\n");
		
		if (nonSecretTestCases.length > 0) {
			buf.append("whichTest = '" + nonSecretTestCases[0].getTestCaseName() + "'\n");
			
			for (TestCase tc : nonSecretTestCases) {
				buf.append("\n");
				buf.append("if whichTest == '" + tc.getTestCaseName() + "':\n");
				buf.append("    result = " + problem.getTestname() + "(" + tc.getInput() + ")\n");
			}
		}
		
		return buf.toString();
	}
}
