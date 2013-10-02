// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.util;

/**
 * Thread-safe holder/factory for a singleton object.
 * Ensures that only one instance of the singleton is created.
 * A downcall to {@link #onCreate(Object)} is made when the singleton
 * needs to be created.
 * 
 * @author David Hovemeyer
 *
 * @param <E> the type of the singleton
 * @param <ArgType> the type of the argument to {@link #get(Object)} and {@link #onCreate(Object)}
 */
public abstract class SingletonHolder<E, ArgType> {
	private E theInstance;
	private Object lock;
	
	/**
	 * Constructor.
	 */
	public SingletonHolder() {
		this.theInstance = null;
		this.lock = new Object();
	}
	
	/**
	 * Check whether the singleton has been created.
	 * 
	 * @return true if the singleton has been created, false otherwise
	 */
	public boolean isCreated() {
		synchronized (lock) {
			return theInstance != null;
		}
	}
	
	/**
	 * Get the singleton instance, creating it if necessary.
	 * 
	 * @param arg the argument specifying any information that is needed to create the singleton object
	 * @return the singleton instance
	 */
	public E get(ArgType arg) {
		synchronized (lock) {
			if (theInstance == null) {
				theInstance = onCreate(arg);
			}
			return theInstance;
		}
	}
	
	/**
	 * Downcall method to create the singleton: will be called exactly once.
	 * @param arg 
	 * 
	 * @param arg the argument specifying any information that is needed to create the singleton object
	 * @return the singleton
	 */
	protected abstract E onCreate(ArgType arg);
}
