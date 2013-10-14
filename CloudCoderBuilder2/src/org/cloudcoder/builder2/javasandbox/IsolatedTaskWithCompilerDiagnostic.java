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

package org.cloudcoder.builder2.javasandbox;

import org.cloudcoder.app.shared.model.CompilerDiagnostic;

/**
 * Extends {@link IsolatedTask}, but adds the capability of returning a
 * {@link CompilerDiagnostic}.  This is useful for dynamic JVM languages,
 * where errors like referring to a nonexistent variable or method may
 * be found at runtime (when a test is executed) rather than at "compile" time.
 * 
 * @author David Hovemeyer
 *
 * @param <T>
 */
public interface IsolatedTaskWithCompilerDiagnostic<T> extends IsolatedTask<T> {
	/**
	 * Return the {@link CompilerDiagnostic}, if any.  For certain types
	 * of runtime errors, such as referring to a nonexistent variable or method,
	 * we create a CompilerDiagnostic because the error really indicates a
	 * "static" bug in the program.
	 * 
	 * @return the {@link CompilerDiagnostic}, or null if there is none
	 */
	public CompilerDiagnostic getCompilerDiagnostic();
}
