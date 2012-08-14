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
 * RepoProblem represents a problem in the exercise repository.
 * As such, it contains just a unique id, the unique id of the user who
 * posted it, and the information in {@link ProblemData}.
 * 
 * @author David Hovemeyer
 */
public class RepoProblem extends ProblemData {
	private static final long serialVersionUID = 1L;

	private int id;
	private int userId;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<RepoProblem> SCHEMA = new ModelObjectSchema<RepoProblem>()
		.add(new ModelObjectField<RepoProblem, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
			public void set(RepoProblem obj, Integer value) { obj.setId(value); }
			public Integer get(RepoProblem obj) { return obj.getId(); }
		})
		.add(new ModelObjectField<RepoProblem, Integer>("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
			public void set(RepoProblem obj, Integer value) { obj.setUserId(value); }
			public Integer get(RepoProblem obj) { return obj.getUserId(); }
		})
		.addAll(ProblemData.SCHEMA.getFieldList());
	
	/** Number of fields. */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();
	
	/**
	 * Constructor.
	 */
	public RepoProblem() {
		
	}
	
	/**
	 * Set the unique id.
	 * @param id the unique id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the user id of the user who published the problem.
	 * @param userId the user id of the user who published the problem
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * @return the user id of the user who published the problem
	 */
	public int getUserId() {
		return userId;
	}
}
