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
 * Descriptor for a field of a model class.  Describes the
 * name, Java type, size (such as the max length for a String field),
 * and whether the field value is a unique object id.
 * This can be used by a persistence layer to map model object
 * properties into a persistent store (such as a database).
 * 
 * @author David Hovemeyer
 */
public class ModelObjectField {
	private final String name;
	private final Class<?> type;
	private final int size;
//	private final boolean isUniqueId;
	private ModelObjectIndexType indexType;
	
	/**
	 * Constructor for fields which are not a unique object id.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 */
	public ModelObjectField(String name, Class<?> type, int size) {
		this(name, type, size, ModelObjectIndexType.NONE);
	}

	/**
	 * Constructor for fields which could be a unique object id.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 * @param indexType true if the field is the unique object id
	 */
	public ModelObjectField(String name, Class<?> type, int size, ModelObjectIndexType indexType) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.indexType = indexType;
	}
	
	/**
	 * @return the field name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the Java type of the field
	 */
	public Class<?> getType() {
		return type;
	}
	
	/**
	 * @return the size of the field (e.g., max string length for a string field)
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * @return true if the field is a unique object id
	 */
	public boolean isUniqueId() {
		return indexType == ModelObjectIndexType.IDENTITY;
	}
	
	/**
	 * Get the field's index type.
	 * @return the field's index type
	 */
	public ModelObjectIndexType getIndexType() {
		return indexType;
	}
}
