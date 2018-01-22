// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015,2018 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.Date;

/**
 * A problem that has been assigned in a Course.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class Problem extends ProblemData implements IProblem, IModelObject<Problem>
{
	private static final long serialVersionUID = 1L;

	private Integer problemId;
	private Integer courseId;
	private long whenAssigned;
	private long whenDue;
	private boolean visible;
	private ProblemAuthorship problemAuthorship;
	private boolean deleted;
	private int moduleId;
	private boolean shared;
	
	/** {@link ModelObjectField} for problem id. */
	public static final ModelObjectField<IProblem, Integer> PROBLEM_ID =
			new ModelObjectField<IProblem, Integer>("problem_id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(IProblem obj, Integer value) { obj.setProblemId(value); }
		public Integer get(IProblem obj) { return obj.getProblemId(); }
	};
	/** {@link ModelObjectField} for course id. */
	public static final ModelObjectField<IProblem, Integer> COURSE_ID =
			new ModelObjectField<IProblem, Integer>("course_id", Integer.class, 0) {
		public void set(IProblem obj, Integer value) { obj.setCourseId(value); }
		public Integer get(IProblem obj) { return obj.getCourseId(); }
	};
	/** {@link ModelObjectField} for assigned date/time. */
	public static final ModelObjectField<IProblem, Long> WHEN_ASSIGNED =
			new ModelObjectField<IProblem, Long>("when_assigned", Long.class, 0) {
		public void set(IProblem obj, Long value) { obj.setWhenAssigned(value); }
		public Long get(IProblem obj) { return obj.getWhenAssigned(); }
	};
	/** {@link ModelObjectField} for due date/time. */
	public static final ModelObjectField<IProblem, Long> WHEN_DUE =
			new ModelObjectField<IProblem, Long>("when_due", Long.class, 0) {
		public void set(IProblem obj, Long value) { obj.setWhenDue(value); }
		public Long get(IProblem obj) { return obj.getWhenDue(); }
	};
	/** {@link ModelObjectField} for visibility to students. */
	public static final ModelObjectField<IProblem, Boolean> VISIBLE =
			new ModelObjectField<IProblem, Boolean>("visible", Boolean.class, 0) {
		public void set(IProblem obj, Boolean value) { obj.setVisible(value); }
		public Boolean get(IProblem obj) { return obj.isVisible(); }
	};
	
	/** {@link ModelObjectField} for problem authorship. */ 
	public static final ModelObjectField<IProblem, ProblemAuthorship> PROBLEM_AUTHORSHIP =
			new ModelObjectField<IProblem, ProblemAuthorship>("problem_authorship", ProblemAuthorship.class, 0) {
		public void set(IProblem obj, ProblemAuthorship value) { obj.setProblemAuthorship(value); }
		public ProblemAuthorship get(IProblem obj) { return obj.getProblemAuthorship(); }
	};
	
	/** {@link ModelObjectField} for deleted flag. */
	public static final ModelObjectField<IProblem, Boolean> DELETED = 
			new ModelObjectField<IProblem, Boolean>("deleted", Boolean.class, 0) {
		public void set(IProblem obj, Boolean value) { obj.setDeleted(value); }
		public Boolean get(IProblem obj) { return obj.isDeleted(); }
	};

	/** 
	 * {@link ModelObjectField} for shared flag (true if the problem has been shared
	 * to the exercise repository.
	 */
	public static final ModelObjectField<IProblem, Boolean> SHARED = 
			new ModelObjectField<IProblem, Boolean>("shared", Boolean.class, 0) {
		public void set(IProblem obj, Boolean value) { obj.setShared(value); }
		public Boolean get(IProblem obj) { return obj.isShared(); }
	};
			
	
	/**
	 * {@link ModelObjectField} for module id. Note that default value is "1",
	 * which is the id of the "Uncategorized" module. 
	 */
	public static final ModelObjectField<IProblem, Integer> MODULE_ID = new ModelObjectField<IProblem, Integer>("module_id", Integer.class, 0, ModelObjectIndexType.NONE, 0, "1") {
		public void set(IProblem obj, Integer value) { obj.setModuleId(value); }
		public Integer get(IProblem obj) { return obj.getModuleId(); }
	};
	
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V0 = new ModelObjectSchema<IProblem>("problem", Problem.class)
			.add(PROBLEM_ID)
			.add(COURSE_ID)
			.add(WHEN_ASSIGNED)
			.add(WHEN_DUE)
			.add(VISIBLE)
			.addAll(ProblemData.SCHEMA_V0.getFieldList());
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V1 =
			// Based on Problem schema version 0...
			ModelObjectSchema.basedOn(SCHEMA_V0, Problem.class)
			// With the v1 deltas from ProblemData
			.addDeltasFrom(ProblemData.SCHEMA_V1)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 2).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V2 =
			ModelObjectSchema.basedOn(SCHEMA_V1, Problem.class)
			.addAfter(VISIBLE, PROBLEM_AUTHORSHIP)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 3).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V3 =
			ModelObjectSchema.basedOn(SCHEMA_V2, Problem.class)
			.addAfter(PROBLEM_AUTHORSHIP, DELETED)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 4).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V4 =
			ModelObjectSchema.basedOn(SCHEMA_V3, Problem.class)
			// Note: the default value of the module_id field is 1, which
			// is the default "Uncategorized" module.  This object is guaranteed
			// to exist because it is persisted by version 0 of the
			// Module schema.
			.addAfter(DELETED, MODULE_ID)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 5).
	 * This version incorporates schema changes from version 2
	 * of {@link ProblemData}'s schema.
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V5 =
			ModelObjectSchema.basedOn(Problem.SCHEMA_V4, Problem.class)
			.addDeltasFrom(ProblemData.SCHEMA_V2)
			.finishDelta();

	/**
	 * Description of fields (schema version 6).
	 * This version incorporates schema changes from version 3
	 * of {@link ProblemData}'s schema.
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V6 =
			ModelObjectSchema.basedOn(SCHEMA_V5, Problem.class)
			.addDeltasFrom(ProblemData.SCHEMA_V3)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 7).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V7 =
			ModelObjectSchema.basedOn(SCHEMA_V6, Problem.class)
			.addAfter(MODULE_ID, SHARED)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 8).
	 * This version incorporates schema changes from version 4
	 * of {@link IProblemData}'s schema.
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V8 =
			ModelObjectSchema.basedOn(SCHEMA_V7, Problem.class)
			.addDeltasFrom(ProblemData.SCHEMA_V4)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 9).
	 * Note that we are not actually changing any fields.
	 * Instead, we are forcing a new schema version because
	 * the representation of test cases changed in ITestCaseData
	 * (version 0 to version 1), and this needs to force a new
	 * schema version for {@link Problem} and {@link RepoProblem}.
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V9 = ModelObjectSchema.basedOn(SCHEMA_V8, Problem.class)
		.addDeltasFrom(ProblemData.SCHEMA_V5)
		.finishDelta();

	/**
	 * Description of fields (schema version 10).
	 * The size of the problem description field increased.
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V10 = ModelObjectSchema.basedOn(SCHEMA_V9, Problem.class)
		.addDeltasFrom(ProblemData.SCHEMA_V6)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 11).
	 * No fields have chagned, but {@link ProblemData}'s schema has
	 * changed (due to new {@link ProblemType} members being added.)
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V11 = ModelObjectSchema.basedOn(SCHEMA_V10, Problem.class)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 12).
	 * The <code>equalityPredicate</code> field was added to
	 * {@link ProblemData} in {@link ProblemData#SCHEMA_V8}.
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA_V12 = ModelObjectSchema.basedOn(SCHEMA_V11, Problem.class)
		.addDeltasFrom(ProblemData.SCHEMA_V8)
		.finishDelta();
	
	/**
	 * Description of fields (current schema version).
	 */
	public static final ModelObjectSchema<IProblem> SCHEMA = SCHEMA_V12;
	
	/**
	 * Number of fields.
	 */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();
	
	/**
	 * Constructor.
	 */
	public Problem() {
		moduleId = 1; // id of the "Uncategorized" module
	}
	
	@Override
	public ModelObjectSchema<IProblem> getSchema() {
		return SCHEMA;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#getProblemId()
	 */
	@Override
	public Integer getProblemId(){
		return problemId;
	}
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#setProblemId(java.lang.Integer)
	 */
	@Override
	public void setProblemId(Integer id){
		this.problemId = id;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#getCourseId()
	 */
	@Override
	public Integer getCourseId() {
		return courseId;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#setCourseId(java.lang.Integer)
	 */
	@Override
	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#getWhenAssigned()
	 */
	@Override
	public long getWhenAssigned() {
		return whenAssigned;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#getWhenAssignedAsDate()
	 */
	@Override
	public Date getWhenAssignedAsDate() {
		return new Date(whenAssigned);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#setWhenAssigned(long)
	 */
	@Override
	public void setWhenAssigned(long whenAssigned) {
		this.whenAssigned = whenAssigned;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#getWhenDue()
	 */
	@Override
	public long getWhenDue() {
		return whenDue;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#getWhenDueAsDate()
	 */
	@Override
	public Date getWhenDueAsDate() {
		return new Date(whenDue);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#setWhenDue(long)
	 */
	@Override
	public void setWhenDue(long whenDue) {
		this.whenDue = whenDue;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.Problem#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public ProblemAuthorship getProblemAuthorship() {
		return problemAuthorship;
	}
	
	@Override
	public void setProblemAuthorship(ProblemAuthorship problemAuthorship) {
		this.problemAuthorship = problemAuthorship;
	}
	
	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	@Override
	public boolean isDeleted() {
		return deleted;
	}
	
	@Override
	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}
	
	@Override
	public int getModuleId() {
		return moduleId;
	}
	
	@Override
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	@Override
	public boolean isShared() {
		return this.shared;
	}

	@Override
	public String toString() {
		return getProblemId()+" testName: "+getTestname()+" "+getDescription();
	}

	/**
	 * @return a "nice" string consisting of the testname and the brief description
	 */
	public String toNiceString() {
		return getTestname() + " - " + getBriefDescription();
	}

	/**
	 * Copy all data in the given Problem object into this one.
	 * 
	 * @param other another Problem object
	 */
	public void copyFrom(Problem other) {
		super.copyFrom(other);
		this.problemId = other.problemId;
		this.courseId = other.courseId;
		this.whenAssigned = other.whenAssigned;
		this.whenDue = other.whenDue;
		this.visible = other.visible;
		this.problemAuthorship = other.problemAuthorship;
		this.deleted = other.deleted;
		this.moduleId = other.moduleId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Problem)) {
			return false;
		}
		Problem other = (Problem) obj;
		return super.equals(other)
				&& ModelObjectUtil.equals(this.problemId, other.problemId)
				&& ModelObjectUtil.equals(this.courseId, other.courseId)
				&& this.whenAssigned == other.whenAssigned
				&& this.whenDue == other.whenDue
				&& this.visible == other.visible
				&& this.problemAuthorship == other.problemAuthorship
				&& this.deleted == other.deleted
				&& this.moduleId == other.moduleId;
	}

	/*
	 * Initialize given {@link Problem} so that it is in an "empty"
	 * state, appropriate for editing as a new problem.
	 * 
	 * @param empty the {@link Problem} to initialize to an empty state
	 */
	public static void initEmpty(Problem empty) {
		empty.setProblemId(-1);
		empty.setCourseId(-1);
		empty.setWhenAssigned(0L);
		empty.setWhenDue(0L);
		empty.setVisible(false);
		empty.setProblemAuthorship(ProblemAuthorship.ORIGINAL);
		empty.setDeleted(false);
		empty.setModuleId(1); // the id of the "Uncategorized" module
		ProblemData.initEmpty(empty);
	}
}
