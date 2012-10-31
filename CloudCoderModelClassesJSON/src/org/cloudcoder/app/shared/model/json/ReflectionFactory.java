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

package org.cloudcoder.app.shared.model.json;

import org.cloudcoder.app.shared.model.IFactory;

/**
 * Implementation of {@link IFactory} that creates objects by reflection.
 * @author David Hovemeyer
 *
 * @param <E> type of object to create
 */
public class ReflectionFactory<E> implements IFactory<E> {
	private final Class<E> cls;
	
	protected ReflectionFactory(Class<E> cls) {
		this.cls = cls;
	}

	/**
	 * Create a factory for creating objects of given type.
	 * 
	 * @param cls the type of object the factory should create
	 * @return factory for creating objects of given type
	 */
	public static<T> IFactory<T> forClass(Class<T> cls) {
		return new ReflectionFactory<T>(cls);
	}

	@Override
	public E create() {
		try {
			return cls.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Could not create object", e);
		}
	}

}
