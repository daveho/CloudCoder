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

package org.cloudcoder.app.shared.model;

/**
 * Enumeration of license types.
 * 
 * @author David Hovemeyer
 */
public enum ProblemLicense {
	/**
	 * License type for problems that are not redistributable.
	 */
	NOT_REDISTRIBUTABLE(
			"Not redistributable",
			""),
	
	/**
	 * Creative commons attrib/sharealike (copyleft).
	 */
	CC_ATTRIB_SHAREALIKE_3_0(
			"Creative Commons Attribution-ShareAlike 3.0",
			"http://creativecommons.org/licenses/by-sa/3.0/"),
	
	/**
	 * GNU Free Documentation License without exceptions
	 * (i.e., no invariant sections or cover texts.)
	 */
	GNU_FDL_1_3_NO_EXCEPTIONS(
			"GNU Free Documentation License (version 1.3, no exceptions)",
			"http://www.gnu.org/licenses/fdl.html");
	
	private final String name;
	private final String url;
	private final boolean permissive;
	
	private ProblemLicense(String name, String url) {
		this(name, url, true);
	}
	
	private ProblemLicense(String name, String url, boolean permissive) {
		this.name = name;
		this.url = url;
		this.permissive = permissive;
	}
	
	/**
	 * @return the human-readable name of this license.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the URL of a web page explaining the terms of this license
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return true if this license is permissive (allows free redistribution),
	 *         false if not
	 */
	public boolean isPermissive() {
		return permissive;
	}
	
	/**
	 * Get a ProblemLicense given its ordinal value.
	 * 
	 * @param ordinal an ordinal value
	 * @return the ProblemLicense with that ordinal value
	 */
	public static ProblemLicense fromOrdinal(int ordinal) {
		ProblemLicense[] values = values();
		return values[ordinal];
	}
}
