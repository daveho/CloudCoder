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
		
		/**
		 * Persist a model object.
		 */
		PERSIST_MODEL_OBJECT,
		
		/**
		 * Increase the size of a field.
		 */
		INCREASE_FIELD_SIZE,
		
		/**
		 * Add an index.
		 */
		ADD_INDEX,
	}
	
	/**
	 * A delta applied to a {@link ModelObjectSchema} to produce a derived
	 * schema.
	 */
	public static class Delta<E> {
		private DeltaType type;
		
		/**
		 * Constructor.
		 * 
		 * @param type the {@link DeltaType}
		 */
		public Delta(DeltaType type) {
			this.type = type;
		}
		
		/**
		 * @return the {@link DeltaType}
		 */
		public DeltaType getType() {
			return type;
		}
	}
	
	/**
	 * A {@link Delta} involving a field.
	 */
	public abstract static class FieldDelta<ModelObjectType> extends Delta<ModelObjectType> {
		private ModelObjectField<? super ModelObjectType, ?> field;
		
		/**
		 * Constructor.
		 * 
		 * @param type           the {@link DeltaType}
		 * @param field          the field
		 */
		public FieldDelta(DeltaType type, ModelObjectField<? super ModelObjectType, ?> field) {
			super(type);
			this.field = field;
		}
		
		/**
		 * @return the field
		 */
		public ModelObjectField<? super ModelObjectType, ?> getField() {
			return field;
		}
	}
	
	/**
	 * A {@link Delta} adding a field to the schema.
	 */
	public static class AddFieldDelta<ModelObjectType> extends FieldDelta<ModelObjectType> {
		private ModelObjectField<? super ModelObjectType, ?> previousField;
		
		/**
		 * Constructor.
		 * 
		 * @param type           the {@link DeltaType}
		 * @param previousField  a field in a previous schema version
		 * @param field          a new field to be added
		 */
		public AddFieldDelta(ModelObjectField<? super ModelObjectType, ?> previousField, ModelObjectField<? super ModelObjectType, ?> field) {
			super(DeltaType.ADD_FIELD_AFTER, field);
			this.previousField = previousField;
		}
		
		/**
		 * @return the field from a previous schema version
		 */
		public ModelObjectField<? super ModelObjectType, ?> getPreviousField() {
			return previousField;
		}
	}
	
	/**
	 * A {@link Delta} for increasing the size of a model object field.
	 * Note that the {@link ModelObjectField} passed to the constructor is assumed to have the
	 * increased size, and will <em>replace</em> the identically-named field
	 * from the previous schema version.
	 */
	public static class IncreaseFieldSizeDelta<ModelObjectType> extends FieldDelta<ModelObjectType> {
		/**
		 * Constructor.
		 * 
		 * @param field the field with the updated (larger) size
		 */
		public IncreaseFieldSizeDelta(ModelObjectField<? super ModelObjectType, ?> field) {
			super(DeltaType.INCREASE_FIELD_SIZE, field);
		}
	}
	
	/**
	 * A {@link Delta} specifying a model object to be persisted.
	 */
	public static class PersistModelObjectDelta<ModelObjectType, E extends IModelObject<E>> extends Delta<ModelObjectType> {
		private E obj;
		
		/**
		 * Constructor.
		 * 
		 * @param obj the model object to be persisted
		 */
		public PersistModelObjectDelta(E obj) {
			super(DeltaType.PERSIST_MODEL_OBJECT);
			this.obj = obj;
		}
		
		/**
		 * @return the model object
		 */
		public E getObj() {
			return obj;
		}
	}

	/**
	 * A {@link Delta} specifying an index to be added to the schema.
	 */
	public static class AddIndexToFieldDelta<ModelObjectType> extends Delta<ModelObjectType> {
		private ModelObjectIndex<? super ModelObjectType> index;

		public AddIndexToFieldDelta(ModelObjectIndex<? super ModelObjectType> index) {
			super(DeltaType.ADD_INDEX);
			this.index = index;
		}
		
		public ModelObjectIndex<? super ModelObjectType> getIndex() {
			return index;
		}
	}
	
	private final ModelObjectSchema<ModelObjectType> previous;
	private final int version;
	private final String name;
	private final List<ModelObjectField<? super ModelObjectType, ?>> fieldList;
	private final Map<String, ModelObjectField<? super ModelObjectType, ?>> nameToFieldList;
	private final List<ModelObjectIndex<? super ModelObjectType>> indexList;
	private final List<Delta<? super ModelObjectType>> deltaList;
	private final Map<ModelObjectField<? super ModelObjectType, ?>, ModelObjectIndexType> indexTypeOverrideMap;

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
		this.version = (previous == null) ? 0 : previous.version + 1;
		this.name = name;
		this.fieldList = new ArrayList<ModelObjectField<? super ModelObjectType, ?>>();
		this.nameToFieldList = new HashMap<String, ModelObjectField<? super ModelObjectType,?>>();
		this.indexList = new ArrayList<ModelObjectIndex<? super ModelObjectType>>();
		this.deltaList = new ArrayList<Delta<? super ModelObjectType>>();
		this.indexTypeOverrideMap = new HashMap<ModelObjectField<? super ModelObjectType,?>, ModelObjectIndexType>();
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
		return version;
	}

	/**
	 * Get schema with given version number, which must be less than or equal
	 * to this schema's version number.  This method is useful for getting
	 * a particular previous version of the schema for a model object class.
	 * 
	 * @param version the version to get
	 * @return the schema with the requested version
	 */
	public ModelObjectSchema<ModelObjectType> getSchemaWithVersion(int version) {
		if (version > this.version) {
			throw new IllegalArgumentException("No schema for " + name + " with version " + version);
		}
		
		ModelObjectSchema<ModelObjectType> schema = this;
		while (schema.version != version) {
			schema = schema.previous;
		}
		
		return schema;
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
		if (name.endsWith("z")) {
			// Create appropriate plural for "quiz" schema
			return "cc_" + name + "zes";
		} else if (name.endsWith("criterion")) {
			// Create appropriate plural for "achievement_criterion" schema
			return "cc_" + name.substring(0, name.length() - 2) + "a";
		} else {
			return "cc_" + name + "s";
		}
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
	 * Change the index type on given field.
	 * 
	 * @param field     a field
	 * @param indexType the index type to set on the field
	 * @return a reference to this object
	 */
	public ModelObjectSchema<ModelObjectType> setIndexOn(ModelObjectField<? super ModelObjectType, ?> field, ModelObjectIndexType indexType) {
		indexTypeOverrideMap.put(field, indexType);
		return this;
	}

	/**
	 * Add a {@link ModelObjectIndex} to the schema.
	 * Returns a reference to the schema object, so calls
	 * can be chained.
	 * <em>Important</em>: this method should only be used when creating
	 * a new (non-derived) schema.
	 * 
	 * @param index the index to add
	 * @return a reference to this object
	 */
	public ModelObjectSchema<ModelObjectType> addIndex(ModelObjectIndex<? super ModelObjectType> index) {
		// Set the index number
		int indexNumber = indexList.size();
		index.setIndexNumber(indexNumber);
		
		// Add it to the index list
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
	 * Get the index type for a given field.
	 */
	public ModelObjectIndexType getIndexType(ModelObjectField<? super ModelObjectType, ?> field) {
		// See if the index type has been overridden.
		ModelObjectIndexType indexType = indexTypeOverrideMap.get(field);
		if (indexType == null) {
			// Index type is not overridden: just use the index type defined in the field.
			indexType = field.getIndexType();
		}
		return indexType;
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
	public List<ModelObjectIndex<? super ModelObjectType>> getIndexList() {
		return indexList;
	}
	
	/**
	 * Get the list of {@link AddFieldDelta}s relative to the previous schema version.
	 * 
	 * @return list of {@link AddFieldDelta}s
	 */
	public List<Delta<? super ModelObjectType>> getDeltaList() {
		return deltaList;
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
	public static<E> ModelObjectSchema<E> basedOn(ModelObjectSchema<E> previous) {
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
	public ModelObjectSchema<ModelObjectType> addAfter(
			ModelObjectField<? super ModelObjectType, ?> previousField,
			ModelObjectField<? super ModelObjectType, ?> fieldToAdd) {
		
		if (previous == null) {
			throw new IllegalStateException("This method is only for derived schemas");
		}
		
		deltaList.add(new AddFieldDelta<ModelObjectType>(previousField, fieldToAdd));
		
		return this;
	}

	/**
	 * Apply all deltas in given schema to this schema.
	 * This is useful when creating a new version of a subclass schema
	 * based on a new version of a superclass schema.
	 *  
	 * @param schema the schema containing the deltas to apply
	 * @return this object
	 */
	public ModelObjectSchema<ModelObjectType> addDeltasFrom(ModelObjectSchema<? super ModelObjectType> schema) {
		deltaList.addAll(schema.getDeltaList());
		return this;
	}
	
	/**
	 * Add a {@link PersistModelObjectDelta} specifying that the given
	 * model object should be persisted.
	 * 
	 * @param obj the model object to persist
	 * @return this object, for method chaining
	 */
	public<E extends IModelObject<E>> ModelObjectSchema<ModelObjectType> addPersistedModelObject(E obj) {
		deltaList.add(new PersistModelObjectDelta<ModelObjectType, E>(obj));
		return this;
	}
	
	/**
	 * Add a {@link IncreaseFieldSizeDelta} specifying that the size
	 * of a field should be increased.
	 * 
	 * @param field the field with the increased size: will replace identically-named
	 *              field from previous schema version
	 * @return this object, for method chaining
	 */
	public ModelObjectSchema<ModelObjectType> increaseFieldSize(ModelObjectField<? super ModelObjectType, ?> field) {
		deltaList.add(new IncreaseFieldSizeDelta<ModelObjectType>(field));
		return this;
	}

	/**
	 * Add an index to the table as a delta.
	 * This method should only be called when creating a derived schema.
	 * 
	 * @param index the index to add
	 * @return this object, for method chaining
	 */
	public ModelObjectSchema<ModelObjectType> addIndexDelta(ModelObjectIndex<? super ModelObjectType> index) {
		deltaList.add(new AddIndexToFieldDelta<ModelObjectType>(index));
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
		for (Delta<? super ModelObjectType> delta_ : deltaList) {
			switch (delta_.getType()) {
			case ADD_FIELD_AFTER:
				// Insert the new field.
				{
					AddFieldDelta<? super ModelObjectType> delta = (AddFieldDelta<? super ModelObjectType>)delta_;
					int index = fieldList.indexOf(delta.getPreviousField());
					fieldList.add(index + 1, delta.getField());
				}
				break;
				
			case PERSIST_MODEL_OBJECT:
				// This kind of delta requires no changes to the ModelObjectSchema object
				break;
				
			case INCREASE_FIELD_SIZE:
				// Replace previous version of the field.
				{
					IncreaseFieldSizeDelta<? super ModelObjectType> delta = (IncreaseFieldSizeDelta<? super ModelObjectType>) delta_;
					int index = fieldList.indexOf(getFieldByName(delta.getField().getName()));
					fieldList.set(index, delta.getField());
				}
				break;
				
			case ADD_INDEX:
				// Add the index to the index list.
				{
					AddIndexToFieldDelta<? super ModelObjectType> delta = (AddIndexToFieldDelta<? super ModelObjectType>)delta_;
//					indexList.add(delta.getIndex());
					addIndex(delta.getIndex());
				}
				break;
			}
		}
		
		return this;
	}
}
