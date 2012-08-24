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
	/**
	 * Type of delta for a derived schema.
	 */
	public enum DeltaType {
		/**
		 * Add a field after a field defined in a previous schema version.
		 */
		ADD_FIELD_AFTER,
	}
	
	/**
	 * A delta describing a change to a schema to produce a derived schema.
	 */
	public class Delta {
		private DeltaType type;
		private ModelObjectField<? super ModelObjectType, ?> previousField, field;
		
		/**
		 * Constructor.
		 * 
		 * @param type           the {@link DeltaType}
		 * @param previousField  a field in a previous schema version
		 * @param field          a new field to be added (or modified?)
		 */
		public Delta(DeltaType type, ModelObjectField<? super ModelObjectType, ?> previousField, ModelObjectField<? super ModelObjectType, ?> field) {
			this.type = type;
			this.previousField = previousField;
			this.field = field;
		}
		
		/**
		 * @return the {@link DeltaType}
		 */
		public DeltaType getType() {
			return type;
		}
		
		/**
		 * @return the field from a previous schema version
		 */
		public ModelObjectField<? super ModelObjectType, ?> getPreviousField() {
			return previousField;
		}
		
		/**
		 * @return the new field to add (or modify?)
		 */
		public ModelObjectField<? super ModelObjectType, ?> getField() {
			return field;
		}
	}
	
	private final ModelObjectSchema<ModelObjectType> previous;
	private final String name;
	private final List<ModelObjectField<? super ModelObjectType, ?>> fieldList;
	private final Map<String, ModelObjectField<? super ModelObjectType, ?>> nameToFieldList;
	private final List<ModelObjectIndex<ModelObjectType>> indexList;
	private final List<Delta> deltaList;

	/**
	 * Constructor.
	 * 
	 * @param name the name of the schema: can be used to derive a database table name,
	 *             XML element name, etc.
	 */
	public ModelObjectSchema(String name) {
		this(null, name);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param previous the previous schema version (null if this is not a derived schema)
	 * @param name the name of the schema: can be used to derive a database table name,
	 *             XML element name, etc.
	 */
	private ModelObjectSchema(ModelObjectSchema<ModelObjectType> previous, String name) {
		this.previous = previous;
		this.name = name;
		this.fieldList = new ArrayList<ModelObjectField<? super ModelObjectType, ?>>();
		this.nameToFieldList = new HashMap<String, ModelObjectField<? super ModelObjectType,?>>();
		this.indexList = new ArrayList<ModelObjectIndex<ModelObjectType>>();
		this.deltaList = new ArrayList<ModelObjectSchema<ModelObjectType>.Delta>();
	}
	
	/**
	 * Get the previous-version schema.
	 * 
	 * @return the previous-version schema
	 */
	public ModelObjectSchema<ModelObjectType> getPrevious() {
		return previous;
	}
	
	/**
	 * Get the schema version number.
	 * 
	 * @return the schema version number
	 */
	public int getVersion() {
		// Count back how many derived versions there are.
		int count = 0;
		ModelObjectSchema<ModelObjectType> schema = this;
		while (schema != null) {
			count++;
			schema = schema.getPrevious();
		}
		return count;
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
	 * Add a {@link ModelObjectIndex} to the schema.
	 * Returns a reference to the schema object, so calls
	 * can be chained.
	 * 
	 * @param index the index to add
	 * @return a reference to this object
	 */
	public ModelObjectSchema<ModelObjectType> addIndex(ModelObjectIndex<ModelObjectType> index) {
		indexList.add(index);
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
	 * Get list of indices.
	 * @return the list of indices
	 */
	public List<ModelObjectIndex<ModelObjectType>> getIndexList() {
		return indexList;
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

	/**
	 * Create a new derived schema based on a previous schema.
	 * 
	 * @param previous the previous schema
	 * @return the new derived schema
	 */
	public static<E> ModelObjectSchema<E> deltaFrom(ModelObjectSchema<E> previous) {
		return new ModelObjectSchema<E>(previous, previous.getName());
	}

	/**
	 * In a derived schema, add a new field after a field specified in a previous version.
	 * Returns a reference to this object, so calls can be chained.
	 * 
	 * @param previousField field specified in a previous version
	 * @param fieldToAdd    the field to add
	 * @return reference to this object, so calls can be chained
	 */
	public<E> ModelObjectSchema<ModelObjectType> addAfter(
			ModelObjectField<? super ModelObjectType, ?> previousField,
			ModelObjectField<? super ModelObjectType, ?> fieldToAdd) {
		
		if (previous == null) {
			throw new IllegalStateException("This method is only for derived schemas");
		}
		
		deltaList.add(new Delta(DeltaType.ADD_FIELD_AFTER, previousField, fieldToAdd));
		
		return this;
	}

	/**
	 * This method must be called after all deltas (e.g., {@link #addAfter(ModelObjectField, ModelObjectField)})
	 * are applied to a derived schema.
	 * 
	 * @return a reference to the completed derived schema object
	 */
	public ModelObjectSchema<ModelObjectType> finishDelta() {
		// Add all fields from previous schema
		for (ModelObjectField<? super ModelObjectType, ?> previousField : previous.getFieldList()) {
			add(previousField);
		}
		
		// Apply all deltas
		for (Delta delta : deltaList) {
			if (delta.getType() == DeltaType.ADD_FIELD_AFTER) {
				int index = fieldList.indexOf(delta.getPreviousField());
				fieldList.add(index + 1, delta.getField());
			} else {
				throw new IllegalStateException("Unknown delta type: " + delta.getType());
			}
		}
		
		return this;
	}
}
