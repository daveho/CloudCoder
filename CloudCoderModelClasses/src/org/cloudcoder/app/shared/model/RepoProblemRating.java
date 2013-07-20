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

/**
 * A single rating for a {@link RepoProblem} (repository exercise).
 * 
 * @author David Hovemeyer
 */
public class RepoProblemRating implements IModelObject<RepoProblemRating> {
	public static final int MIN_VALUE = 1;
	public static final int MAX_VALUE = 5;
	
	private int id;
	private int userId;        // id of user who rated the exercise
	private int repoProblemId; // id of repo exercise
	private int value;         // rating, between MIN_VALUE and MAX_VALUE
	
	private static final ModelObjectField<RepoProblemRating, Integer> ID = new ModelObjectField<RepoProblemRating, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(RepoProblemRating obj, Integer value) { obj.id = value; }
		public Integer get(RepoProblemRating obj) { return obj.id; }
	};
	private static final ModelObjectField<RepoProblemRating, Integer> USER_ID = new ModelObjectField<RepoProblemRating, Integer>("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(RepoProblemRating obj, Integer value) { obj.userId = value; }
		public Integer get(RepoProblemRating obj) { return obj.userId; }
	};
	private static final ModelObjectField<RepoProblemRating, Integer> REPO_PROBLEM_ID = new ModelObjectField<RepoProblemRating, Integer>("repo_problem_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(RepoProblemRating obj, Integer value) { obj.repoProblemId = value; }
		public Integer get(RepoProblemRating obj) { return obj.repoProblemId; }
	};
	private static final ModelObjectField<RepoProblemRating, Integer> VALUE = new ModelObjectField<RepoProblemRating, Integer>("value", Integer.class, 0) {
		public void set(RepoProblemRating obj, Integer value) { obj.value = value; }
		public Integer get(RepoProblemRating obj) { return obj.value; }
	};
	
	private static final ModelObjectSchema<RepoProblemRating> SCHEMA_V0 = new ModelObjectSchema<RepoProblemRating>("repo_problem_rating")
			.add(ID)
			.add(USER_ID)
			.add(REPO_PROBLEM_ID)
			.add(VALUE);
	
	public static final ModelObjectSchema<RepoProblemRating> SCHEMA = SCHEMA_V0;
	
	public RepoProblemRating() {
	}
	
	@Override
	public ModelObjectSchema<? super RepoProblemRating> getSchema() {
		return SCHEMA;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getRepoProblemId() {
		return repoProblemId;
	}
	
	public void setRepoProblemId(int repoProblemId) {
		this.repoProblemId = repoProblemId;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
}
