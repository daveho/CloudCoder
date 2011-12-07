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

package org.cloudcoder.submitsvc.oop.builder;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Compile a String into loadable Java class files.
 * 
 * @see http://www.java2s.com/Tutorial/Java/0120__Development/CompileString.htm
 * @see http://www.google.com/codesearch/p?hl=en&sa=N&cd=1&ct=rc#S2Cc76FDuM4/test/tools/javac/api/evalexpr/CompileFromString.java&q=evalexpr%20CompileFromString
 */
public class CompileString {
	
	public static void main(String[] args) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String program =
				"public class Test{" +
				"   public static void main (String [] args){" +
				"      System.out.println (\"Hello, World\");" +
				"      System.out.println (args.length);" +
				"   }" +
				"}";

		MemoryFileManager fm = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
		
		compiler.getTask(null, fm, null, null, null, Arrays.asList(MemoryFileManager.makeSource("Test", program))).call();
		System.out.println("Compiled " + fm.getNumClassesCreated() + " class(es)");
		
		ClassLoader cl = fm.getClassLoader(javax.tools.StandardLocation.CLASS_OUTPUT);
		Class<?> clazz = cl.loadClass("Test");
	    Method m = clazz.getMethod("main", new Class[] { String[].class });
	    Object[] _args = new Object[] { new String[0] };
	    m.invoke(null, _args);
	}
}
