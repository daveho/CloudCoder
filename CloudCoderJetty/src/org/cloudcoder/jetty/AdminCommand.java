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

package org.cloudcoder.jetty;

/**
 * An administrative command.
 * Specifies a command name, main class, and description.
 * 
 * @author David Hovemeyer
 */
public class AdminCommand {
	private final String name;
	private final String mainClass;
	private final String description;
	
	/**
	 * Constructor.
	 * 
	 * @param name        command name (e.g., "createuser")
	 * @param mainClass   main class to run (e.g., "org.cloudcoder.app.server.persist.CreateUser")
	 * @param description brief description of the admin command
	 */
	public AdminCommand(String name, String mainClass, String description) {
		this.name = name;
		this.mainClass = mainClass;
		this.description = description;
	}
	
	/**
	 * @return the command name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the main class name
	 */
	public String getMainClass() {
		return mainClass;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
}
