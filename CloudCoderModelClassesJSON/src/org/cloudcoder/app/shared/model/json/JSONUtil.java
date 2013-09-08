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

package org.cloudcoder.app.shared.model.json;

import java.util.Map;

/**
 * Utility methods for working with JSON objects and values.
 * 
 * @author David Hovemeyer
 */
public class JSONUtil {
	/**
	 * Assert that the given JSON value is an object, and return it as a map
	 * of field names to values.
	 * 
	 * @param jsonValue the JSON value
	 * @return the JSON value as an object (map)
	 * @throws IllegalArgumentException if the JSON value is not an object
	 */
	public static Map<?, ?> expectObject(Object jsonValue) throws IllegalArgumentException {
		if (!(jsonValue instanceof Map)) {
			throw new IllegalArgumentException("Expected JSON object");
		}
		return (Map<?, ?>) jsonValue;
	}
	
	/**
	 * Assert that the given JSON value is a value of the given Java type,
	 * and return it as a value of that type.
	 * 
	 * @param cls        the Java type
	 * @param jsonValue  the JSON value
	 * @return the JSON value as the Java type
	 * @throws IllegalArgumentException if the JSON value does not an instance of the Java type
	 */
	public static<E> E expect(Class<E> cls, Object jsonValue) throws IllegalArgumentException {
		if (!cls.isAssignableFrom(jsonValue.getClass())) {
			throw new IllegalArgumentException("Expected " + cls.getSimpleName() + ", saw " + jsonValue.getClass().getSimpleName());
		}
		return cls.cast(jsonValue);
	}
	
	/**
	 * Assert that the given JSON value is an integer, and return it as a Java
	 * integer.
	 * 
	 * @param jsonValue a JSON value
	 * @return the JSON value as an integer
	 * @throws IllegalArgumentException if the JSON value is not an integer
	 */
	public static Integer expectInteger(Object jsonValue) throws IllegalArgumentException {
		if (jsonValue instanceof Integer) {
			return (Integer) jsonValue;
		}
		if (jsonValue instanceof Long) {
			return Integer.valueOf((int) ((Long)jsonValue).longValue());
		}
		throw new IllegalArgumentException("Expected Integer, saw " + jsonValue.getClass().getSimpleName());
	}
	
	/**
	 * Assert that a JSON object has a field with the given name, and
	 * return the value of the field.
	 * 
	 * @param jsonObj   the JSON object
	 * @param fieldName the field name
	 * @return the value of the field
	 * @throws IllegalArgumentException if the JSON object has no field with the specified name
	 */
	public static Object requiredField(Map<?, ?> jsonObj, String fieldName) throws IllegalArgumentException {
		Object value = jsonObj.get(fieldName);
		if (value == null) {
			throw new IllegalArgumentException("Missing field: " + fieldName);
		}
		return value;
	}

}
