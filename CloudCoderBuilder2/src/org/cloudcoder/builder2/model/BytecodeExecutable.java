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
import java.util.List;

/**
 * A bytecode executable consisting of some number of files.
 * 
 * @author David Hovemeyer
 */
public class BytecodeExecutable {
	private File dir;
	private List<String> fileNameList;
	private String mainClass;
	
	/**
	 * Constructor.
	 * 
	 * @param dir           directory containing bytecode files
	 * @param fileNameList  list of bytecode filenames
	 */
	public BytecodeExecutable(File dir, List<String> fileNameList) {
		this.fileNameList = fileNameList;
	}
	
	/**
	 * @return directory containing bytecode filenames
	 */
	public File getDir() {
		return dir;
	}
	
	/**
	 * @return list of bytecode filenames
	 */
	public List<String> getFileNameList() {
		return fileNameList;
	}

	/**
	 * Set fully-qualified name of main class.
	 * 
	 * @param mainClass fully-qualified name of main class
	 */
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	
	/**
	 * @return fully-qualified name of main class
	 */
	public String getMainClass() {
		return mainClass;
	}
}
