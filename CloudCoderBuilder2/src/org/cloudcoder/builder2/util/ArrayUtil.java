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

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Array utility methods.
 * 
 * @author David Hovemeyer
 */
public class ArrayUtil {
	/**
	 * Convert a Collection to an array.
	 * 
	 * @param c    the collection
	 * @param cls  the element type
	 * @return array containing all of the elements in the collection
	 */
	public static<E> E[] toArray(Collection<E> c, Class<E> cls) {
		@SuppressWarnings("unchecked")
		E[] result = (E[]) Array.newInstance(cls, c.size());
		int i = 0;
		for (E element : c) {
			result[i++] = element;
		}
		return result;
	}

	/**
	 * Determine whether given array has any null elements.
	 * 
	 * @param a an array
	 * @return true if the array has any null elements, false if all elements are non-null
	 */
	public static boolean hasNullElements(Object[] a) {
		return countNullElements(a) > 0;
	}

	/**
	 * Return number of null elements in array.
	 * 
	 * @param a an array
	 * @return number of null elements in the array
	 */
	public static int countNullElements(Object[] a) {
		int count = 0;
		for (Object o : a) {
			if (o == null) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Return a copy of the given array that has the same element values
	 * in the same order, except that all null values are removed.
	 * 
	 * @param a an array
	 * @return an array with the same values with the null values removed
	 */
	public static<E> E[] stripNullElements(E[] a) {
		@SuppressWarnings("unchecked")
		E[] result = (E[]) Array.newInstance(a.getClass().getComponentType(), a.length - countNullElements(a));
		int added = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != null) {
				result[added++] = a[i];
			}
		}
		return result;
	}
}
