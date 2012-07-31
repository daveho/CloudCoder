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

package org.cloudcoder.app.server.persist;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Get and set bean properties via reflection.
 * This is basically a poor man's version of
 * commons-beanutils.  Should not be used for any
 * performance-critical code.
 * 
 * @author David Hovemeyer
 */
public class BeanUtil {
	/**
	 * Set the value of a property of a given bean object.
	 * 
	 * @param bean          the bean object
	 * @param propertyName  the property name
	 * @param value         the property value to set 
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static void setProperty(Object bean, String propertyName, Object value)
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String methodName = getMethodName("set", propertyName);
		
		// Find the first public setter method whose parameter is assignable
		// from the type of value being set.
		Method setter = null;
		for (Method m : bean.getClass().getMethods()) {
			if ((m.getModifiers() & Modifier.PUBLIC) == 0
					|| (m.getModifiers() & Modifier.STATIC) != 0
					|| !m.getName().equals(methodName)) {
				continue;
			}
			
			Class<?>[] paramTypes = m.getParameterTypes();
			if (paramTypes.length != 1) {
				continue;
			}
			
			if (paramTypes[0].isAssignableFrom(value.getClass())
					|| compatiblePrimitiveType(paramTypes[0], value.getClass())) {
				setter = m;
				break;
			}
		}
		
		if (setter == null) {
			throw new NoSuchMethodException("No setter found in " + bean.getClass().getName() + " for " + propertyName);
		}
		
		setter.invoke(bean, value);
	}

	// Determine whether an object value may be assigned
	// via a setter parameter that is a primitive type: legal when
	// the object belongs to one of the java.lang wrapper classes.
	private static boolean compatiblePrimitiveType(Class<?> paramType, Class<?> valueType) {
		if (!paramType.isPrimitive()) {
			return false;
		}
		
		return (paramType == Boolean.TYPE && valueType == Boolean.class)
				|| (paramType == Byte.TYPE && valueType == Byte.class)
				|| (paramType == Double.TYPE && valueType == Double.class)
				|| (paramType == Float.TYPE && valueType == Float.class)
				|| (paramType == Integer.TYPE && valueType == Integer.class)
				|| (paramType == Long.TYPE && valueType == Long.class)
				|| (paramType == Short.TYPE && valueType == Short.class);
	}

	/**
	 * Get the value of a given property of a bean object.
	 * 
	 * @param bean          the bean object
	 * @param propertyName  the name of the property to get
	 * @return              the property value
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object getProperty(Object bean, String propertyName)
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String methodName = getMethodName("get", propertyName);
		String boolPropertyMethodName = getMethodName("is", propertyName);
		
		// Because a getter will have no parameters, we should not find
		// any overloaded variants.  Thus, the first method we find
		// should be the one we need.
		Method getter = null;
		for (Method m : bean.getClass().getMethods()) {
			if ((m.getModifiers() & Modifier.PUBLIC) == 0
					|| (m.getModifiers() & Modifier.STATIC) != 0
					|| !(m.getName().equals(methodName) || m.getName().equals(boolPropertyMethodName))) {
				continue;
			}
			
			Class<?>[] paramTypes = m.getParameterTypes();
			if (paramTypes.length != 0) {
				continue;
			}
			
			// Make sure the return type matches the method name.
			// (Only boolean or Boolean properties should be returned
			// by an "isXXX" getter method, and vice versa.)
			Class<?> returnType = m.getReturnType();
			boolean isBoolReturnType = returnType == Boolean.TYPE || returnType == Boolean.class;
			if ((isBoolReturnType && !m.getName().startsWith("is"))
					|| (!isBoolReturnType && m.getName().startsWith("is"))) {
				continue;
			}
			
			getter = m;
			break;
		}
		
		if (getter == null) {
			throw new NoSuchMethodException("No getter found in " + bean.getClass().getName() + " for " + propertyName);
		}
		
		return getter.invoke(bean, new Object[0]);
	}
	
	private static String getMethodName(String prefix, String propertyName) {
		propertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
		return prefix + propertyName;
	}
}
