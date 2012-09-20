package org.cloudcoder.app.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for model objects.
 * 
 * @author David Hovemeyer
 */
public class ModelObjectUtil {
	/**
	 * Compare two potentially null references for equality.
	 *  
	 * @param a  a reference
	 * @param b  another reference
	 * @return true if a and b are either equal or both null, false otherwise
	 */
	public static<E> boolean equals(E a, E b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return a.equals(b);
	}

	/**
	 * Compare two arrays of objects for equality.
	 * 
	 * @param a   an array of objects
	 * @param b   another array of objects
	 * @return true if a and b are the same size and have contents that compare
	 *         as the same element-wise, or are both null, false otherwise
	 */
	public static boolean arrayEquals(Object[] a, Object[] b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (!equals(a[i], b[i])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Combine lists.
	 * 
	 * @param a the first list
	 * @param b the second list
	 * @return the result of combining both lists
	 */
	public static<E> List<E> combineLists(List<E> a, List<E> b) {
		ArrayList<E> result = new ArrayList<E>();

		result.addAll(a);
		result.addAll(b);
		
		return result;
	}
	
	/**
	 * Convert a string into an object of given type.
	 * 
	 * @param s    a string
	 * @param cls  the type to convert the string to
	 * @return object of given type
	 */
	public static Object convertString(String s, Class<?> cls) {
		if (cls == Integer.class) {
			return Integer.valueOf(s);
		} else if (cls == String.class){
			return s;
		} else {
			throw new IllegalArgumentException("Don't know how to convert string to " + cls.getName());
		}
	}
}
