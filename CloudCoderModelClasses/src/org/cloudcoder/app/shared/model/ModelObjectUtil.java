package org.cloudcoder.app.shared.model;

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
}
