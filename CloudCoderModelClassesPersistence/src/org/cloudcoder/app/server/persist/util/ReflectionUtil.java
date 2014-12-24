// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper methods for creating objects and calling methods by reflection.
 * 
 * @author David Hovemeyer
 */
public abstract class ReflectionUtil {
	/**
	 * Find named class.
	 * 
	 * @param clsName a class name
	 * @return the Class object for the class, or null if the named class
	 *         can't be found
	 */
	public static Class<?> findClass(String clsName) {
		try {
			return Class.forName(clsName);
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Create an object.
	 * 
	 * @param cls   the type of the object to create
	 * @param args  arguments to pass to the class's constructor: note that the
	 *              argument types must match the constructor parameter types
	 *              exactly
	 * @return the created object
	 */
	public static Object createExact(Class<?> cls, Object... args) {
		try {
			Constructor<?> ctor = cls.getConstructor(ReflectionUtil.getExactTypes(args));
			return ctor.newInstance(args);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Error creating " + cls.getName() + " object", e);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Error creating " + cls.getName() + " object", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error creating " + cls.getName() + " object", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Error creating " + cls.getName() + " object", e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Error creating " + cls.getName() + " object", e);
		}
	}

	/**
	 * Call a method on given receiver object.
	 * 
	 * @param receiver    the receiver object
	 * @param methodName  the name of the method to call
	 * @param types       the exact types of the called method's parameters
	 * @param args        the arguments to pass to the called method
	 * @return the result of the method call
	 */
	public static Object call(Object receiver, String methodName, Class<?>[] types, Object... args) {
		Class<?> cls = receiver.getClass();
		try {
			Method method = cls.getMethod(methodName, types);
			return method.invoke(receiver, args);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Error calling method " + methodName + " on " + cls.getName(), e);
		} catch (SecurityException e) {
			throw new IllegalStateException("Error calling method " + methodName + " on " + cls.getName(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error calling method " + methodName + " on " + cls.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Error calling method " + methodName + " on " + cls.getName(), e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Error calling method " + methodName + " on " + cls.getName(), e);
		}
	}

	/**
	 * Call a method.
	 * 
	 * @param receiver    the receiver object
	 * @param methodName  the name of the method to call
	 * @param args        the arguments to pass to the method: note that these
	 *                    must match the method's parameter types exactly
	 * @return the result of the method call
	 */
	public static Object callExact(Object receiver, String methodName, Object... args) {
		return call(receiver, methodName, getExactTypes(args), args);
	}
	
	/**
	 * Get an array of class objects corresponding to the types
	 * of the arguments in the given argument array.
	 * 
	 * @param args an argument array
	 * @return the class objects for the argument array
	 */
	public static Class<?>[] getExactTypes(Object[] args) {
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			types[i] = args[i].getClass();
		}
		return types;
	}

}
