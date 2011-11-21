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
