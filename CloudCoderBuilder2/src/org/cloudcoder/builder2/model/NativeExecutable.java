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

package org.cloudcoder.builder2.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Submission artifact representing a native executable
 * (for example, the result of compiling a C/C++ program.)
 * 
 * @author David Hovemeyer
 */
public class NativeExecutable {
	private final File dir;
	private final String exeFileName;
	
	/**
	 * Constructor.
	 * 
	 * @param dir          directory containing the native executable
	 * @param exeFileName  the unqualified filename of the native executable
	 */
	public NativeExecutable(File dir, String exeFileName) {
		this.dir = dir;
		this.exeFileName = exeFileName;
	}
	
	/**
	 * @return directory containing the native executable
	 */
	public File getDir() {
		return dir;
	}
	
	/**
	 * @return the unqualified filename of the native executable
	 */
	public String getExeFileName() {
		return exeFileName;
	}
	
	/**
	 * Return a {@link Command} to execute this executable with the specified arguments.
	 * 
	 * @param args the command arguments
	 * @return a {@link Command} to execute this executable with the specified arguments
	 */
	public Command toCommand(String... args) {
		List<String> argList = new ArrayList<String>();
		argList.add("./" + this.getExeFileName());
		argList.addAll(Arrays.asList(args));
		
		Command command = new Command(this.getDir(), argList);
		return command;
	}
}
