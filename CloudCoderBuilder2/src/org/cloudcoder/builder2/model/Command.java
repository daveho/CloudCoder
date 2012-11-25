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

import java.util.List;

/**
 * A command to be executed with specified arguments.
 * 
 * @author David Hovemeyer
 */
public class Command {
	private List<String> args;
	
	/**
	 * Constructor.
	 * The first command argument is the path to the program (executable),
	 * and the remaining arguments are command line arguments.
	 * 
	 * @param args command arguments
	 */
	public Command(List<String> args) {
		this.args = args;
	}
	
	/**
	 * @return the command arguments
	 */
	public List<String> getArgs() {
		return args;
	}
}
