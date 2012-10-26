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
 * Enum to represent the problem authorship status of a {@link Problem}.
 * This helps us keep track of the provenance of problems as they
 * are imported from and exported from the exercise repository.
 * 
 * @author David Hovemeyer
 */
public enum ProblemAuthorship {
	/**
	 * Original problem: was created within the local CloudCoder instance.
	 */
	ORIGINAL,
	
	/**
	 * A problem imported from the exercise repository, but not changed.
	 */
	IMPORTED,
	
	/**
	 * A problem imported from the exercise repository and modified locally.
	 * Its parent hash specifies the original version of the problem.
	 */
	IMPORTED_AND_MODIFIED,
}
