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

package org.cloudcoder.submitsvc.oop.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaimespacco
 *
 */
public class InMemoryJavaCompiler
{
    private static final Logger logger=LoggerFactory.getLogger(InMemoryJavaCompiler.class);
    
    private MemoryFileManager fm;
    private JavaCompiler compiler;
    private CompilationResult compileResult;
    private ClassLoader classLoader;
    private List<JavaFileObject> sources;
    private Map<String, Class<?>> classMap=new HashMap<String, Class<?>>();
    
    public Class<?> getClass(String className) {
        return classMap.get(className);
    }
    
    public InMemoryJavaCompiler() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fm = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
        sources = new ArrayList<JavaFileObject>();
    }
    
    public void addClassFile(String className, String classText) {
        logger.trace(className);
        logger.trace(classText);
        sources.add(MemoryFileManager.makeSource(className, classText));
    }
    
    /**
     * Compile all of the classes added with {@link #addClassFile(String, String)}
     * and load them into memory.
     * 
     * @return true if the compilation succeeded, false if the code could
     *         not be compiled or the compiled classes could not be loaded
     */
    public boolean compile()
    {
        // Compile, but don't load the classes yet
        if (!compileWithoutLoadingClasses()) {
        	// Code does not compile
        	return false;
        }

        // Load the classes
        classLoader = fm.getClassLoader(StandardLocation.CLASS_OUTPUT);
        if (!loadClasses()) {
        	// Classes could not be loaded
            return false;
        }
        
        return true;
    }

    /**
     * Compile all of the classes added with {@link #addClassFile(String, String)},
     * but do not load them into memory.
     * 
     * @return true if the compilation succeeded, false otherwise
     */
    public boolean compileWithoutLoadingClasses() {
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
    
    private boolean loadClasses() {
        // Make sure that we can load the classfile we just compiled
        for (JavaFileObject jfo : sources) {
            try {
                String className=jfo.getName().replace("/", "").replace(".java","");
                Class<?> theClass=classLoader.loadClass(className);
                classMap.put(className, theClass);
            } catch (ClassNotFoundException e) {
                compileResult=new CompilationResult(
                        CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
                //compileResult.setException(e);
                return false;
            }
        }
        return true;
    }

    /**
     * @return the compileResult
     */
    public CompilationResult getCompileResult() {
        return compileResult;
    }

    /**
     * @return the classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Putting this here, I think because things like javax.tools.Diagnostic cannot 
     * be converted into Javascript by GWT?
     * @param d
     * @return
     */
    public static CompilerDiagnostic convertJavaxDiagnostic(Diagnostic d) {
        return new CompilerDiagnostic(d.getLineNumber(), d.getLineNumber(),
                d.getColumnNumber(), d.getColumnNumber(), d.getMessage(null));
    }
}
