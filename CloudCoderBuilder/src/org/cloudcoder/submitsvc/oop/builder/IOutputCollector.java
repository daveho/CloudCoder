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

package org.cloudcoder.submitsvc.oop.builder;

import java.util.List;

/**
 * Interface implemented by objects that collect text output
 * from a running process.
 * 
 * @author David Hovemeyer
 */
public interface IOutputCollector {
	/**
	 * Start collecting output (asynchronously).
	 */
	public abstract void start();

	/**
	 * Interrupt the thread that is collecting output.
	 */
	public abstract void interrupt();

	/**
	 * Wait for output collector thread to finish.
	 * 
	 * @throws InterruptedException
	 */
	public abstract void join() throws InterruptedException;

	/**
	 * Get the collected output as a list of strings.
	 * @return
	 */
	public abstract List<String> getCollectedOutput();
}