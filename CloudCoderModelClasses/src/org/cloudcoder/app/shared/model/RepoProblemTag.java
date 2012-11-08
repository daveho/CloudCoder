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
 * A tag on a {@link RepoProblem}.
 * 
 * @author David Hovemeyer
 */
public class RepoProblemTag {
	private int userId;
	private int repoProblemId;
	private String name;

	/** {@link ModelObjectField} for user id. */
	public static final ModelObjectField<RepoProblemTag, Integer> USER_ID = new ModelObjectField<RepoProblemTag, Integer>("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(RepoProblemTag obj, Integer value) { obj.setUserId(value); }
		public Integer get(RepoProblemTag obj) { return obj.getUserId(); }
	};
	/** {@link ModelObjectField} for repo problem id. */
	public static final ModelObjectField<RepoProblemTag, Integer> REPO_PROBLEM_ID = new ModelObjectField<RepoProblemTag, Integer>("repo_problem_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(RepoProblemTag obj, Integer value) { obj.setRepoProblemId(value); }
		public Integer get(RepoProblemTag obj) { return obj.getRepoProblemId(); }
	};
	/** {@link ModelObjectField} for tag name. */
	public static final ModelObjectField<RepoProblemTag, String> NAME = new ModelObjectField<RepoProblemTag, String>("name", String.class, 40, ModelObjectIndexType.NON_UNIQUE) {
		public void set(RepoProblemTag obj, String value) { obj.setName(value); }
		public String get(RepoProblemTag obj) { return obj.getName(); }
	};
	
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<RepoProblemTag> SCHEMA_V0 = new ModelObjectSchema<RepoProblemTag>("repo_problem_tag")
			.add(USER_ID)
			.add(REPO_PROBLEM_ID)
			.add(NAME)
			// Add a unique index on all three fields:
			// this ensures that a user can add a particular tag to a repo problem
			// only once.
			.addIndex(new ModelObjectIndex<RepoProblemTag>(ModelObjectIndexType.UNIQUE)
					.addField(USER_ID)
					.addField(REPO_PROBLEM_ID)
					.addField(NAME)
					);
	
	/**
	 * Description of fields (current schema version).
	 */
	public static final ModelObjectSchema<RepoProblemTag> SCHEMA = SCHEMA_V0;
	
	/**
	 * Constructor.
	 */
	public RepoProblemTag() {
	}
	
	/**
	 * Set the user id for this tag.
	 * @param userId the user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Get the user id for this tag.
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Set the repo problem id for this tag.
	 * @param repoProblemId the repo problem id
	 */
	public void setRepoProblemId(int repoProblemId) {
		this.repoProblemId = repoProblemId;
	}
	
	/**
	 * Get the repo problem id for this tag.
	 * @return the repo problem id
	 */
	public int getRepoProblemId() {
		return repoProblemId;
	}
	
	/**
	 * Set the name of this tag.
	 * @param name the name of this tag
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the name of this tag.
	 * @return the name of this tag
	 */
	public String getName() {
		return name;
	}
}
