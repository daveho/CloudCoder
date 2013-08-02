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
import java.util.List;

/**
 * An index on one or more fields of a model object.
 * Used as a hint to the persistence layer.
 * 
 * @author David Hovemeyer
 *
 * @param <ModelObjectType>
 */
public class ModelObjectIndex<ModelObjectType> {
	private final List<ModelObjectField<? super ModelObjectType, ?>> fieldList;
	private final ModelObjectIndexType indexType;
	private int indexNumber;
	
	/**
	 * Constructor.
	 * 
	 * @param indexType the index type
	 */
	public ModelObjectIndex(ModelObjectIndexType indexType) {
		this.fieldList = new ArrayList<ModelObjectField<? super ModelObjectType,?>>();
		this.indexType = indexType;
		this.indexNumber = -1;
	}
	
	/**
	 * Add a field to the index.
	 * Returns a reference to this object, allowing calls to be chained.
	 * 
	 * @param field the field to add
	 */
	public ModelObjectIndex<ModelObjectType> addField(ModelObjectField<? super ModelObjectType, ?> field) {
		fieldList.add(field);
		return this;
	}
	
	/**
	 * Get list of fields covered by this index.
	 * 
	 * @return list of fields covered by this index
	 */
	public List<ModelObjectField<? super ModelObjectType, ?>> getFieldList() {
		return fieldList;
	}
	
	/**
	 * Get the index type.
	 * @return the index type
	 */
	public ModelObjectIndexType getIndexType() {
		return indexType;
	}
	
	/**
	 * Get the index number. The index numbers are assigned sequentially
	 * within the schema the index belongs to.
	 * 
	 * @return the index number
	 */
	public int getIndexNumber() {
		return indexNumber;
	}
	
	void setIndexNumber(int indexNumber) {
		this.indexNumber = indexNumber;
	}
}
