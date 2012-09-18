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

import java.io.File;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * A test case executor for {@link ProblemType#JAVA_PROGRAM} test cases.
 * 
 * @author David Hovemeyer
 */
public class JavaRegexTestCaseExecutor extends CRegexTestCaseExecutor {
	/**
	 * Constructor.
	 * 
	 * @param tempDir    directory containing compiled class files
	 * @param testCase   the {@link TestCase} to execute
	 * @param mainClass  fully-qualified class name of the class containing the main method
	 */
	public JavaRegexTestCaseExecutor(File tempDir, TestCase testCase, String mainClass) {
		super(tempDir, testCase);
		
		// This is the most appalling hack job.
		// The Builder really needs a major refactoring to properly separate
		// the notions of
		//
		//    - compilation
		//    - executable artifact
		//    - running an executable artifact
		//    - execution result
		//    - correctness checking of execution result
		//
		// in a way that supports reuse.  This class more or less hard-codes
		// all of these concerns into a single class!
		
		// For now, just hard-code an appropriate command for executing the JVM
		// with the name of the compiled program's main class.
		
		arguments.clear();
		arguments.add("java");
		arguments.add("-classpath");
		arguments.add(".");
		arguments.add(mainClass);
	}
}
