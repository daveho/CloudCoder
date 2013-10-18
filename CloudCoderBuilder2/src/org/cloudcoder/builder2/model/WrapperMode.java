// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2010-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.model;

import org.cloudcoder.builder2.process.ProcessRunner;

/**
 * "Wrapper mode" for {@link ProcessRunner}.  Specifies which means to
 * use in order to wrap a subprocess to allow more control (process limits,
 * sandboxing, etc.)
 * 
 * @author David Hovemeyer
 */
public enum WrapperMode {
	/**
	 * Use the wrapper script (runProcess.sh).
	 * This is guaranteed to be available.
	 */
	SCRIPT,
	
	/**
	 * Use the native executable wrapper.  This will require explicitly
	 * compiling its source code to produce an exe.
	 */
	NATIVE_EXE,
}
