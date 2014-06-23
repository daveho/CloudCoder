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
import java.util.Map;
import java.util.TreeMap;

/**
 * A command to be executed with specified arguments.
 * 
 * @author David Hovemeyer
 */
public class Command {
	private File dir;
	private List<String> args;
	private Map<String, String> env;
	
	/**
	 * Constructor.
	 * The first command argument is the path to the program (executable),
	 * and the remaining arguments are command line arguments.
	 * 
	 * @param dir  the directory to run the command in
	 * @param args command arguments
	 */
	public Command(File dir, List<String> args) {
		this.dir = dir;
		this.args = args;
		this.env = new TreeMap<String, String>();
	}
	
	/**
	 * @return the directory to run the command in
	 */
	public File getDir() {
		return dir;
	}
	
	/**
	 * @return the command arguments
	 */
	public List<String> getArgs() {
		return args;
	}

	/**
	 * Set an environment variable.
	 * 
	 * @param varName name of the environment variable
	 * @param value   value of the environment variable
	 */
	public void setEnvironmentVariable(String varName, String value) {
		env.put(varName, value);
	}
	
	/**
	 * Get the environment variable map.
	 * 
	 * @return the environment variable map
	 */
	public Map<String, String> getEnv() {
		return env;
	}
}
