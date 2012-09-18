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

package org.cloudcoder.submitsvc.oop.builder.test;

import static org.junit.Assert.*;

import org.cloudcoder.submitsvc.oop.builder.FindJavaPackageAndClassNames;
import org.junit.Before;
import org.junit.Test;

/**
 * @author David Hovemeyer
 *
 */
public class TestFindJavaPackageAndClassNames {
	private static final String IN_DEFAULT_PACKAGE = "import java.util.Scanner;\n" +
				"\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n";

	private static final String PROGRAM_TEXT = "package org.cloudcoder.test;\n" + "\n" + IN_DEFAULT_PACKAGE;

	// With a header comment
	private static final String WITH_HEADER_COMMENT =
			"// Hey this is comment yo.\n" +
			"// It should be ignored even though it has the words\n" +
			"// package foo.bar and class Hello\n" +
			PROGRAM_TEXT;

	// With a nested class
	private static final String NESTED_CLASS =
			"package org.cloudcoder.test.nested;\n" +
			"public class Outer {\n" +
			"  public class Inner {\n";

	private FindJavaPackageAndClassNames finder;
	
	@Before
	public void setUp() {
		finder = new FindJavaPackageAndClassNames();
	}
	
	@Test
	public void testFindJavaPackageName() {
		finder.determinePackageAndClassNames(PROGRAM_TEXT);
		assertEquals("org.cloudcoder.test", finder.getPackageName());
	}
	
	@Test
	public void testFindJavaClassName() {
		finder.determinePackageAndClassNames(PROGRAM_TEXT);
		assertEquals("Main", finder.getClassName());
	}
	
	@Test
	public void testFindJavaPackageNameHeaderComment() {
		finder.determinePackageAndClassNames(WITH_HEADER_COMMENT);
		assertEquals("org.cloudcoder.test", finder.getPackageName());
	}
	
	@Test
	public void testFindJavaClassNameHeaderComment() {
		finder.determinePackageAndClassNames(WITH_HEADER_COMMENT);
		assertEquals("Main", finder.getClassName());
	}
	
	@Test
	public void testFindJavaPackageNameInDefaultPackage() {
		finder.determinePackageAndClassNames(IN_DEFAULT_PACKAGE);
		assertEquals("", finder.getPackageName());
	}
	
	@Test
	public void testFindJavaClassNameInDefaultPackage() {
		finder.determinePackageAndClassNames(IN_DEFAULT_PACKAGE);
		assertEquals("Main", finder.getClassName());
	}
	
	@Test
	public void testFindJavaPackageNameNestedClass() {
		finder.determinePackageAndClassNames(NESTED_CLASS);
		assertEquals("org.cloudcoder.test.nested", finder.getPackageName());
	}
	
	@Test
	public void testFindJavaClassNameNestedClass() {
		finder.determinePackageAndClassNames(NESTED_CLASS);
		assertEquals("Outer", finder.getClassName());
	}
	
}
