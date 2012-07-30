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
	private boolean allowNull;
	private String beanPropertyName;
	
	/**
	 * Constructor for fields which are not a unique object id.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 */
	public ModelObjectField(String name, Class<?> type, int size) {
		this(name, type, size, ModelObjectIndexType.NONE, false);
	}

	/**
	 * Constructor for fields which might require an index.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 * @param indexType true if the field is the unique object id
	 */
	public ModelObjectField(String name, Class<?> type, int size, ModelObjectIndexType indexType) {
		this(name, type, size, indexType, false);
	}

	/**
	 * Constructor for fields which might require an index.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 * @param indexType true if the field is the unique object id
	 * @param allowNull true if the field allows null values
	 */
	public ModelObjectField(String name, Class<?> type, int size, ModelObjectIndexType indexType, boolean allowNull) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.indexType = indexType;
		this.beanPropertyName = getBeanPropertyName(name);
		this.allowNull = allowNull;
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

	/**
	 * Check whether or not the field is allowed to have NULL values.
	 * @return true if the field is allowed to have NULL values, false otherwise
	 */
	public boolean isAllowNull() {
		return allowNull;
	}
	
	/**
	 * Get this field's bean property name.
	 * 
	 * @return this field's bean property name 
	 */
	public String getPropertyName() {
		return beanPropertyName;
	}
	
	private static String getBeanPropertyName(String name) {
		String[] tokens = name.split("_");
		StringBuilder buf = new StringBuilder();
		
		for (String token : tokens) {
			if (buf.length() > 0) {
				token = Character.toUpperCase(token.charAt(0)) + token.substring(1);
			}
			
			buf.append(token);
		}
		
		return buf.toString();
	}
}
