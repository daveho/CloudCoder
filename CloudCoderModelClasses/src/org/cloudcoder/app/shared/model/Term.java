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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Model object representing an academic term (Fall, Spring, etc.)
 * @author David Hovemeyer
 */
public class Term implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private int seq;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema SCHEMA = new ModelObjectSchema(Arrays.asList(
			new ModelObjectField("id", Integer.class, 0, ModelObjectIndexType.IDENTITY),
			new ModelObjectField("name", String.class, 20),
			new ModelObjectField("seq", Integer.class, 0)
	));

	/**
	 * Number of database fields.
	 */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();
	
	public Term() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public int getSeq() {
		return seq;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		Term other = (Term) obj;
		return id == other.id && name.equals(other.name) && seq == other.seq;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
