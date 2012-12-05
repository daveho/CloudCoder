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

package org.cloudcoder.builder2.javacompiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compile Java source code into class files (bytecode) in memory.
 * 
 * @author Jaime Spacco
 */
public class InMemoryJavaCompiler
{
	private static final Logger logger=LoggerFactory.getLogger(InMemoryJavaCompiler.class);

	private MemoryFileManager fm;
	private JavaCompiler compiler;
	private CompilationResult compileResult;
	private List<JavaFileObject> sources;

	/**
	 * Constructor.
	 */
	public InMemoryJavaCompiler() {
		compiler = ToolProvider.getSystemJavaCompiler();
		fm = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
		sources = new ArrayList<JavaFileObject>();
	}

	/**
	 * Add a source file to be compiled.
	 * 
	 * @param className  the full class name of the top-level class
	 * @param classText  the full text of the source file
	 */
	public void addSourceFile(String className, String classText) {
		logger.trace(className);
		logger.trace(classText);
		sources.add(MemoryFileManager.makeSource(className, classText));
	}

	/**
	 * Compile all of the classes added with {@link #addSourceFile(String, String)}.
	 * 
	 * @return true if the compilation succeeded, false otherwise
	 */
	public boolean compile() {
		DiagnosticCollector<JavaFileObject> collector= new DiagnosticCollector<JavaFileObject>();

		CompilationTask task = compiler.getTask(null, fm, collector, null, null, sources);
		if (!task.call()) {
			// Compiler error
			compileResult=new CompilationResult(CompilationOutcome.FAILURE);
			List<CompilerDiagnostic> diagnosticList=new LinkedList<CompilerDiagnostic>();
			for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
				// convert Java-specific diagnostics to the language-independent diagnostics
				// we could also 
				diagnosticList.add(InMemoryJavaCompiler.convertJavaxDiagnostic(d));
			}
			compileResult.setCompilerDiagnosticList(diagnosticList.toArray(new CompilerDiagnostic[diagnosticList.size()]));
			logger.warn("Unable to compile: "+compileResult);
			return false;
		} else {
			// Successful compilation
			compileResult=new CompilationResult(CompilationOutcome.SUCCESS);
			return true;
		}
	}

	/**
	 * Get the {@link MemoryFileManager} that is keeping track of sources
	 * and compiled classes.
	 * 
	 * @return the {@link MemoryFileManager}
	 */
	public MemoryFileManager getFileManager() {
		return fm;
	}

	/**
	 * @return the {@link CompilationResult} describing whether the compilation succeeded
	 *         or failed, and what compiler diagnostics were emitted
	 */
	public CompilationResult getCompileResult() {
		return compileResult;
	}

	/**
	 * Convert a Java {@link Diagnostic} to a CloudCoder {@link CompilerDiagnostic}.
	 * 
	 * @param d the {@link Diagnostic}
	 * @return the {@link CompilerDiagnostic}
	 */
	private static CompilerDiagnostic convertJavaxDiagnostic(Diagnostic<?> d) {
		return new CompilerDiagnostic(d.getLineNumber(), d.getLineNumber(),
				d.getColumnNumber(), d.getColumnNumber(), d.getMessage(null));
	}
}
