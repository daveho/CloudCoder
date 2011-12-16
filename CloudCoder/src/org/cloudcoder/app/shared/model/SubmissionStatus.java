// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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
 * Enum describing the general status of a submission.
 * 
 * @author David Hovemeyer
 */
public enum SubmissionStatus {
	/** All tests passed.  This is a completely working submission. */
	TESTS_PASSED,
	
	/** At least one test failed. */
	TESTS_FAILED,
	
	/** Submission did not compile because there was a syntax or semantic error. */
	COMPILE_ERROR,
	
	/** Submission could not be built/tested for some unspecified reason. */
	BUILD_ERROR,
	
	/**
	 * When the user starts working on a problem, we add a Submission
	 * entry with this status.
	 */
	STARTED,
	
	/**
	 * A special submission status that indicates that a problem
	 * has not been started yet.
	 */
	NOT_STARTED,
}
