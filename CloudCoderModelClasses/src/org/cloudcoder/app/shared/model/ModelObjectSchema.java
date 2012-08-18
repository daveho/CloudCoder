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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor for the schema (field types) of a model object
 * class.
 * 
 * @author David Hovemeyer
 */
public class ModelObjectSchema<ModelObjectType> {
	private final String name;
	private final List<ModelObjectField<? super ModelObjectType, ?>> fieldList;
	private final Map<String, ModelObjectField<? super ModelObjectType, ?>> nameToFieldList;

	/**
	 * Constructor.
	 * 
	 * @param name the name of the schema: can be used to derive a database table name,
	 *             XML element name, etc.
	 */
	public ModelObjectSchema(String name) {
		this.name = name;
		this.fieldList = new ArrayList<ModelObjectField<? super ModelObjectType, ?>>();
		this.nameToFieldList = new HashMap<String, ModelObjectField<? super ModelObjectType,?>>();
	}
	
	/**
	 * Get the schema name.
	 * @return the schema name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the database table name.
	 * 
	 * @return the database table name
	 */
	public String getDbTableName() {
		return "cc_" + name + "s";
	}

	/**
	 * Get a name to use for a list of objects of this schema's type
	 * in a serialized format (such as XML or JSON).
	 * 
	 * @return list element name
	 */
	public String getListElementName() {
		return name + "_list";
	}
	
	/**
	 * Add a {@link ModelObjectField} to the schema.
	 * Returns a reference to the schema object, so calls
	 * can be chained.
	 * 
	 * @param field the field to add to the schema
	 * @return a reference to this object
	 */
	public ModelObjectSchema<ModelObjectType> add(ModelObjectField<? super ModelObjectType, ?> field) {
		fieldList.add(field);
		nameToFieldList.put(field.getName(), field);
		return this;
	}
	
	/**
	 * Add all {@link ModelObjectField}s in given list to this schema.
	 * Returns a reference to the schema object, so calls
	 * can be chained.
	 * 
	 * @param otherFieldList a list of {@link ModelObjectField}s
	 * @return a reference to this object
	 */
	public ModelObjectSchema<ModelObjectType> addAll(List<? extends ModelObjectField<? super ModelObjectType, ?>> otherFieldList) {
		for (ModelObjectField<? super ModelObjectType, ?> field : otherFieldList) {
			add(field);
		}
		return this;
	}

	/**
	 * Get a field descriptor.
	 * 
	 * @param index the index of the field descriptor (0 for the first)
	 * @return the field descriptor
	 */
	public ModelObjectField<? super ModelObjectType, ?> getField(int index) {
		return fieldList.get(index);
	}
	
	/**
	 * Get a field descriptor by name.
	 * 
	 * @param name the name of the field
	 * @return the field descriptor, or null if there is no such field
	 */
	public ModelObjectField<? super ModelObjectType, ?> getFieldByName(String fieldName) {
		return nameToFieldList.get(fieldName);
	}
	
	/**
	 * Get the number of fields in the schema.
	 * 
	 * @return number of fields in the schema
	 */
	public int getNumFields() {
		return fieldList.size();
	}

	/**
	 * Get the list of field descriptors.
	 * 
	 * @return list of field descriptors
	 */
	public List<ModelObjectField<? super ModelObjectType, ?>> getFieldList() {
		return fieldList;
	}

	/**
	 * Determine whether this schema has a unique id field.
	 * 
	 * @return true if the schema has a unique id field, false otherwise
	 */
	public boolean hasUniqueId() {
		for (ModelObjectField<? super ModelObjectType, ?> field : fieldList) {
			if (field.isUniqueId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the unique id field of this schema.
	 * 
	 * @return the unique id field
	 */
	public ModelObjectField<? super ModelObjectType, ?> getUniqueIdField() {
		for (ModelObjectField<? super ModelObjectType, ?> field : fieldList) {
			if (field.isUniqueId()) {
				return field;
			}
		}
		throw new IllegalArgumentException("Schema has no unique id field");
	}
}
