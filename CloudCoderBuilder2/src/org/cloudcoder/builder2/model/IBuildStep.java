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

import java.util.Properties;

/**
 * Interface describing an object which implements one step of
 * building an testing a {@link BuilderSubmission}.
 * 
 * @author David Hovemeyer
 */
public interface IBuildStep {
	/**
	 * Execute this build step on the given {@link BuilderSubmission}.
	 * When this method returns, the caller should call
	 * {@link BuilderSubmission#isComplete()} to check whether building/testing
	 * of the submission has completed (either successfully or with an error.)
	 * 
	 * @param submission the {@link BuilderSubmission}
     * @param config     configuration properties: i.e., properties from cloudcoder.properties file
	 */
	public void execute(BuilderSubmission submission, Properties config);
}
