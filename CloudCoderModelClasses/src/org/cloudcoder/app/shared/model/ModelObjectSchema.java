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

import java.util.List;

/**
 * Descriptor for the schema (field types) of a model object
 * class.
 * 
 * @author David Hovemeyer
 */
public class ModelObjectSchema {
	private final List<ModelObjectField> fieldList;
	
	/**
	 * Constructor.
	 * 
	 * @param fieldList descriptors for the schema's fields
	 */
	public ModelObjectSchema(List<ModelObjectField> fieldList) {
		this.fieldList = fieldList;
	}
	
	/**
	 * Get a field descriptor.
	 * 
	 * @param index the index of the field descriptor (0 for the first)
	 * @return the field descriptor
	 */
	public ModelObjectField getField(int index) {
		return fieldList.get(index);
	}
	
	/**
	 * Get the number of fields in the schema.
	 * 
	 * @return number of fields in the schema
	 */
	public int getNumFields() {
		return fieldList.size();
	}
	
//	/**
//	 * Get placeholders for an insert statement.
//	 * 
//	 * @return placeholders for an insert or update statement
//	 */
//	public String getInsertPlaceholders() {
//		return getInsertPlaceholders(getNumColumns());
//	}
//
//	/**
//	 * Get given number of placeholders for an insert statement.
//	 * 
//	 * @param num number of placeholders (which must be less than or equal to
//	 *            the number of fields)
//	 * @return the placeholders
//	 */
//	public String getInsertPlaceholders(int num) {
//		if (num > getNumColumns()) {
//			throw new IllegalArgumentException();
//		}
//		
//		StringBuilder buf = new StringBuilder();
//		
//		for (int i = 0; i < num; i++) {
//			if (buf.length() > 0) {
//				buf.append(", ");
//			}
//			buf.append("?");
//		}
//		
//		return buf.toString();
//	}
//	
//	/**
//	 * Get placeholders for an update statement where all fields in the schema
//	 * will be updated.
//	 * 
//	 * @return placeholders for an update statement where all fields in the schema will be updated
//	 */
//	public String getUpdatePlaceholders() {
//		return doGetUpdatePlaceholders(true);
//	}
//	
//	/**
//	 * Get placeholders for an update statement where all fields except for
//	 * the unique id field will be updated.
//	 * 
//	 * @return placeholders for an update statement where all fields except the
//	 *         unique id field will be update
//	 */
//	public String getUpdatePlaceholdersNoId() {
//		return doGetUpdatePlaceholders(false);
//	}
//
//	private String doGetUpdatePlaceholders(boolean includeUniqueId) {
//		StringBuilder buf = new StringBuilder();
//		
//		for (ModelObjectField dbColumn : fieldList) {
//			if (!dbColumn.isUniqueId() || includeUniqueId) {
//				if (buf.length() > 0) {
//					buf.append(", ");
//				}
//				buf.append(dbColumn.getName());
//				buf.append(" = ?");
//			}
//		}
//		
//		return buf.toString();
//	}

	/**
	 * Get the list of field descriptors.
	 * 
	 * @return list of field descriptors
	 */
	public List<ModelObjectField> getFieldList() {
		return fieldList;
	}
}
