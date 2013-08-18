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

package org.cloudcoder.app.shared.model;

/**
 * Enumeration to represent programming languages.
 * 
 * @author David Hovemeyer
 */
public enum Language {
	/** Java programming language. */
	JAVA("Java"),
	
	/** C programming language. */
	C("C"),
	
	/** C++ programming language. */
	CPLUSPLUS("C++"),
	
	/** Python programming language. */
	PYTHON("Python"),
	
	/** Ruby programming language. */
	RUBY("Ruby"),
	;
	
	private String name;
	
	private Language(String name) {
		this.name = name;
	}
	
	/**
	 * Get a nice human-readable name for this programming language (e.g., "Java").
	 * 
	 * @return human-readable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Convert the language to the tag name used to identify the language
	 * in the exercise repository.  E.g., "c", "java", etc.
	 * 
	 * @return the language's tag name
	 */
	public String getTagName() {
		// Convert the enumeration member name to lower case.
		// The name (of the programming language, e.g., "C++", could
		// have non-alphanumeric characters.)
		return name().toLowerCase();
	}

	/**
	 * Get a literal value that is legal to compare to any value.
	 * 
	 * @return literal value that is legal to compare to any value
	 */
	public String getLiteralCompareToAnyValue() {
		switch (this) {
		case JAVA:
			return "new Object()";
		case C:
		case CPLUSPLUS:
			// C/C++ don't really have this!!!
			return "0";
		case PYTHON:
			return "None";
		case RUBY:
			return "nil";
		default:
			throw new IllegalStateException("No literal bottom value specified for language " + this);
		}
	}
}
