// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.pythonfunction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PySyntaxError;
import org.python.core.PyTraceback;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.__builtin__;

/**
 * Utility methods for Python.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class PythonUtil {
	
	/**
	 * Set of dynamic exceptions (PyExceptions) that we treat as
	 * "compilation" exceptions that should be treated as
	 * compiler diagnostics.
	 */
	private static final Set<String> compilationExceptionTypes = new HashSet<>();
	static {
		compilationExceptionTypes.add("exceptions.NameError");
		compilationExceptionTypes.add("exceptions.ImportError");
	}

	/**
	 * Convert a PySyntaxError into a list of {@link CompilerDiagnostic}s.
	 * 
	 * @param e the PySyntaxError
	 * @return list of {@link CompilerDiagnostic}s
	 */
	public static List<CompilerDiagnostic> convertPySyntaxError(PySyntaxError e) {
		List<CompilerDiagnostic> diagnostics=new LinkedList<CompilerDiagnostic>();
	
		//
		// Based on the source for Jython-2.5.2, here's code the 
		// value field of a PySyntaxError:
		//
		//   PyObject[] tmp = new PyObject[] {
		//       new PyString(filename), new PyInteger(line),
		//       new PyInteger(column), new PyString(text)
		//   };
		// 
		//   this.value = new PyTuple(new PyString(s), new PyTuple(tmp));
		//
		// We're going to pull this apart to get out the values we want
		//
	
		PyTuple tuple=(PyTuple)e.value;
	
		String msg=tuple.get(0).toString();
		PyTuple loc=(PyTuple)tuple.get(1);
		//String filename=(String)loc.get(0);
		int lineNum=(Integer)loc.get(1);
		int colNum=(Integer)loc.get(2);
		//String text=(String)loc.get(3);
	
		CompilerDiagnostic d=new CompilerDiagnostic(lineNum, lineNum, colNum, colNum, msg);
	
		diagnostics.add(d);
		return diagnostics;
	}

	// The following method (getExceptionMessage) is originally from:
	//    http://python.6.x6.nabble.com/Getting-PyException-details-from-Java-td1762496.html
	// I (DHH) updated it to work with more recent Jython versions, and
	// refactored to provide explicit methods for extracting the error type
	// and the error message.

	/** 
	 * Returns the exception message, akin to java exception's getMessage() 
	 * method (not supported properly in Jython). 
	 * @param pye a python exception instance 
	 * @return a string containing the python exception's message 
	 */ 
	public static String getExceptionMessage(PyException pye) { 
		// derivative of Jython's Py.formatException() method 
	
		StringBuffer buf = new StringBuffer(128);
		buf.append(PythonUtil.getErrorType(pye)); 
		if (pye.value != Py.None) { 
			buf.append(": ");
			buf.append(PythonUtil.getErrorMessage(pye)); 
		} 
		return buf.toString(); 
	}

	/**
	 * Get an error message out of a PyException.
	 * 
	 * @param pye the PyException
	 * @return the error message
	 */
	public static String getErrorMessage(PyException pye) {
		if (pye.value == Py.None) {
			return "Unknown error";
		}
		if (__builtin__.isinstance(pye.value, (PyType) Py.SyntaxError)) { 
			return pye.value.__getitem__(0).__str__().toString();
		} else { 
			return pye.value.__str__().toString();
		}
	}

	/**
	 * Get the error type (e.g., "exceptions.NameError") from a PyException.
	 * 
	 * @param pye the PyException
	 * @return the error type
	 */
	public static String getErrorType(PyException pye) {
		if (pye.type instanceof PyType) { 
			return ((PyType) pye.type).fastGetName();
		} else { 
			return pye.type.__str__().toString();
		}
	}

//	/**
//	 * Determine if the error type returned from {@link getErrorType} is a NameError.
//	 * 
//	 * @param pye a PyException
//	 * @return true if the PyException represents a NameError
//	 */
//	public static boolean isNameError(PyException pye) {
//		return getErrorType(pye).equals("exceptions.NameError");
//	}

	/**
	 * Determin if the given PyException is a "compilation" exception
	 * that should be reported as a compiler diagnostic.
	 * 
	 * @param pye the PyException
	 * @return true if the PyException is a "compilation" exception
	 */
	public static boolean isCompilationException(PyException pye) {
		String errorType = getErrorType(pye);
		return compilationExceptionTypes.contains(errorType);
	}

	/**
	 * Convert a PyException into a {@link CompilerDiagnostic}.
	 * 
	 * @param pye the PyException
	 * @return a {@link CompilerDiagnostic}
	 */
	public static CompilerDiagnostic pyExceptionToCompilerDiagnostic(PyException pye) {
		int line;
	
		// Use the traceback to get the line number where the error occurred.
		PyTraceback top = PythonUtil.getTracebackTop(pye.traceback);
		line = top.tb_lineno;
		
		return new CompilerDiagnostic(line, line, 1, 1, getErrorMessage(pye));
	}

	//
	// From experimentation, it seems that a PyTraceback is a stack trace
	// starting with the bottom of the call stack.  Each tb_next link
	// advances to the next higher (inner) stack frame.  This seems backwards
	// to me (seems like you would start with the top and work down),
	// but I'm assuming there's a good reason.  In any case, we assume
	// that the top item on the stack frame identifies the real error.
	//
	public static PyTraceback getTracebackTop(PyTraceback traceback) {
		for (;;) {
			if (traceback.tb_next == null || !(traceback.tb_next instanceof PyTraceback)) {
				return traceback;
			}
			traceback = (PyTraceback) traceback.tb_next;
		}
	}

}
