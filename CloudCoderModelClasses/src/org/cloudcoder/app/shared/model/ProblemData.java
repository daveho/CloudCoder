// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015,2018 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.Serializable;

/**
 * The course- and institution-independent data in a {@link Problem}.
 * This class represents the information about a problem that will
 * be exported when an instructor shares a problem to the repository.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class ProblemData implements Serializable, IProblemData {
	private static final long serialVersionUID = 1L;

	//
	// IMPORTANT: if you add any fields, make sure that you
	// update the copyFrom() method so that they are copied
	// into the destination object.
	//
	private ProblemType problemType;
	private String testName;
	private String briefDescription;
	private String description;
	private String skeleton;
	private int schemaVersion;
	private String authorName;
	private String authorEmail;
	private String authorWebsite;
	private long timestampUTC;
	private ProblemLicense license;
	private String parentHash;
	private String externalLibraryUrl;
	private String externalLibraryMD5;
	private String equalityPredicate;

	// Schema version 0 fields
	
	/** {@link ModelObjectField} for problem type. */
	public static final ModelObjectField<IProblemData, ProblemType> PROBLEM_TYPE =
			new ModelObjectField<IProblemData, ProblemType>("problem_type", ProblemType.class, 0) {
		public void set(IProblemData obj, ProblemType value) { obj.setProblemType(value); }
		public ProblemType get(IProblemData obj) { return obj.getProblemType(); }
	};
	/** {@link ModelObjectField} for test name. */
	public static final ModelObjectField<IProblemData, String> TESTNAME =
			new ModelObjectField<IProblemData, String>("testname", String.class, 255) {
		public void set(IProblemData obj, String value) { obj.setTestname(value); }
		public String get(IProblemData obj) { return obj.getTestname(); }
	};
	/** {@link ModelObjectField} for brief description. */
	public static final ModelObjectField<IProblemData, String> BRIEF_DESCRIPTION =
			new ModelObjectField<IProblemData, String>("brief_description", String.class, 60) {
		public void set(IProblemData obj, String value) { obj.setBriefDescription(value); }
		public String get(IProblemData obj) { return obj.getBriefDescription(); }
	};
	/** {@link ModelObjectField} for description (schema versions 0-2). */
	public static final ModelObjectField<IProblemData, String> DESCRIPTION_V0_V2 =
			new ModelObjectField<IProblemData, String>("description", String.class, 8192, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
		public void set(IProblemData obj, String value) { obj.setDescription(value); }
		public String get(IProblemData obj) { return obj.getDescription(); }
	};
	/** {@link ModelObjectField} for description (schema verions 3-6). */
	public static final ModelObjectField<IProblemData, String> DESCRIPTION_V3_V6 =
			new ModelObjectField<IProblemData, String>("description", String.class, 16384, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
		public void set(IProblemData obj, String value) { obj.setDescription(value); }
		public String get(IProblemData obj) { return obj.getDescription(); }
	};
	/** {@link ModelObjectField} for description. */
	public static final ModelObjectField<IProblemData, String> DESCRIPTION =
			new ModelObjectField<IProblemData, String>("description", String.class, 131072, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
		public void set(IProblemData obj, String value) { obj.setDescription(value); }
		public String get(IProblemData obj) { return obj.getDescription(); }
	};
	/** {@link ModelObjectField} for skeleton (schema versions 0-1). */
	public static final ModelObjectField<IProblemData, String> SKELETON_V0_V1 =
			new ModelObjectField<IProblemData, String>("skeleton", String.class, 400, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
		public void set(IProblemData obj, String value) { obj.setSkeleton(value); }
		public String get(IProblemData obj) { return obj.getSkeleton(); }
	};
	/** {@link ModelObjectField} for skeleton. */
	public static final ModelObjectField<IProblemData, String> SKELETON =
			new ModelObjectField<IProblemData, String>("skeleton", String.class, 2000, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
		public void set(IProblemData obj, String value) { obj.setSkeleton(value); }
		public String get(IProblemData obj) { return obj.getSkeleton(); }
	};
	/** {@link ModelObjectField} for schema version. */
	public static final ModelObjectField<IProblemData, Integer> SCHEMA_VERSION =
			new ModelObjectField<IProblemData, Integer>("schema_version", Integer.class, 0) {
		public void set(IProblemData obj, Integer value) { obj.setSchemaVersion(value); }
		public Integer get(IProblemData obj) { return obj.getSchemaVersion(); }
	};
	/** {@link ModelObjectField} for author name. */
	public static final ModelObjectField<IProblemData, String> AUTHOR_NAME =
			new ModelObjectField<IProblemData, String>("author_name", String.class, 80, ModelObjectIndexType.NON_UNIQUE) {
		public void set(IProblemData obj, String value) { obj.setAuthorName(value); }
		public String get(IProblemData obj) { return obj.getAuthorName(); }
	};
	/** {@link ModelObjectField} for author email. */
	public static final ModelObjectField<IProblemData, String> AUTHOR_EMAIL =
			new ModelObjectField<IProblemData, String>("author_email", String.class, 80) {
		public void set(IProblemData obj, String value) { obj.setAuthorEmail(value); }
		public String get(IProblemData obj) { return obj.getAuthorEmail(); }
	};
	/** {@link ModelObjectField} for author website. */
	public static final ModelObjectField<IProblemData, String> AUTHOR_WEBSITE =
			new ModelObjectField<IProblemData, String>("author_website", String.class, 100) {
		public void set(IProblemData obj, String value) { obj.setAuthorWebsite(value); }
		public String get(IProblemData obj) { return obj.getAuthorWebsite(); }
	};
	/** {@link ModelObjectField} for creation timestamp. */
	public static final ModelObjectField<IProblemData, Long> TIMESTAMP_UTC =
			new ModelObjectField<IProblemData, Long>("timestamp_utc", Long.class, 0) {
		public void set(IProblemData obj, Long value) { obj.setTimestampUtc(value); }
		public Long get(IProblemData obj) { return obj.getTimestampUtc(); }
	};
	/** {@link ModelObjectField} for problem license. */
	public static final ModelObjectField<IProblemData, ProblemLicense> LICENSE =
			new ModelObjectField<IProblemData, ProblemLicense>("license", ProblemLicense.class, 0) {
		public void set(IProblemData obj, ProblemLicense value) { obj.setLicense(value); }
		public ProblemLicense get(IProblemData obj) { return obj.getLicense(); }
	};
	
	// Schema version 1 fields
	
	public static final ModelObjectField<IProblemData, String> PARENT_HASH = new ModelObjectField<IProblemData, String>("parent_hash", String.class, 40, ModelObjectIndexType.NON_UNIQUE) {
		public String get(IProblemData obj) { return obj.getParentHash(); }
		public void set(IProblemData obj, String value) { obj.setParentHash(value); }
	};
	
	// Schema version 4 fields
	
	public static final ModelObjectField<IProblemData, String> EXTERNAL_LIBRARY_URL = new ModelObjectField<IProblemData, String>("external_library_url", String.class, 200, ModelObjectIndexType.NONE, 0, "''") {
		public void set(IProblemData obj, String value) { obj.setExternalLibraryUrl(value); }
		public String get(IProblemData obj) { return obj.getExternalLibraryUrl(); }
	};
	public static final ModelObjectField<IProblemData, String> EXTERNAL_LIBRARY_MD5 = new ModelObjectField<IProblemData, String>("external_library_md5", String.class, 32, ModelObjectIndexType.NONE, 0, "''") {
		public void set(IProblemData obj, String value) { obj.setExternalLibraryMD5(value); }
		public String get(IProblemData obj) { return obj.getExternalLibraryMD5(); }
	};
	
	// Schema version 8 fields
	
	public static final ModelObjectField<IProblemData, String> EQUALITY_PREDICATE = new ModelObjectField<IProblemData, String>("equality_predicate", String.class, 4096, ModelObjectIndexType.NONE, 0, "''") {
		public void set(IProblemData obj, String value) { obj.setEqualityPredicate(value); }
		public String get(IProblemData obj) { return obj.getEqualityPredicate(); }
	};
	
	/**
	 * Description of fields (version 0 schema).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V0 = new ModelObjectSchema<IProblemData>("problem_data", ProblemData.class)
		.add(PROBLEM_TYPE)
		.add(TESTNAME)
		.add(BRIEF_DESCRIPTION)
		.add(DESCRIPTION_V0_V2)
		.add(SKELETON_V0_V1)
		.add(SCHEMA_VERSION)
		.add(AUTHOR_NAME)
		.add(AUTHOR_EMAIL)
		.add(AUTHOR_WEBSITE)
		.add(TIMESTAMP_UTC)
		.add(LICENSE);
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V1 = ModelObjectSchema.basedOn(SCHEMA_V0, ProblemData.class)
		.addAfter(LICENSE, PARENT_HASH)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 2).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V2 = ModelObjectSchema.basedOn(SCHEMA_V1, ProblemData.class)
		.increaseFieldSize(SKELETON)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 3).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V3 = ModelObjectSchema.basedOn(SCHEMA_V2, ProblemData.class)
		.increaseFieldSize(DESCRIPTION_V3_V6)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 4).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V4 = ModelObjectSchema.basedOn(SCHEMA_V3, ProblemData.class)
		.addAfter(PARENT_HASH, EXTERNAL_LIBRARY_URL)
		.addAfter(EXTERNAL_LIBRARY_URL, EXTERNAL_LIBRARY_MD5)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 5).
	 * Note that we are not actually changing any fields.
	 * Instead, we are forcing a new schema version because
	 * the representation of test cases changed in ITestCaseData
	 * (version 0 to version 1), and this needs to force a new
	 * schema version for {@link Problem} and {@link RepoProblem}.
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V5 = ModelObjectSchema.basedOn(SCHEMA_V4, ProblemData.class)
		.finishDelta();

	/**
	 * Description of fields (schema version 6).
	 * The size of the description field is increasing from
	 * 16K to 128K. 
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V6 = ModelObjectSchema.basedOn(SCHEMA_V5, ProblemData.class)
		.increaseFieldSize(DESCRIPTION)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 7).
	 * No field changes, but new {@link ProblemType} members
	 * have been added.
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V7 = ModelObjectSchema.basedOn(SCHEMA_V6, ProblemData.class)
		.finishDelta();
	
	/**
	 * Description of fields (schema version 8).
	 * The <code>equalityPredicate</code> field was added.
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V8 = ModelObjectSchema.basedOn(SCHEMA_V7, ProblemData.class)
		.add(EQUALITY_PREDICATE)
		.finishDelta();

	/**
	 * Description of fields (current schema).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA = SCHEMA_V8;

	/**
	 * Constructor.
	 */
	public ProblemData() {
		super();
		externalLibraryUrl = "";
		externalLibraryMD5 = "";
		equalityPredicate = "";
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setProblemType(org.cloudcoder.app.shared.model.ProblemType)
	 */
	@Override
	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setProblemType(int)
	 */
	@Override
	public void setProblemType(int problemType) {
		this.problemType = ProblemType.values()[problemType];
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getProblemType()
	 */
	@Override
	public ProblemType getProblemType() {
		return problemType;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getTestName()
	 */
	@Override
	public String getTestname() {
		return testName;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setTestName(java.lang.String)
	 */
	@Override
	public void setTestname(String testName) {
		this.testName = testName;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setBriefDescription(java.lang.String)
	 */
	@Override
	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getBriefDescription()
	 */
	@Override
	public String getBriefDescription() {
		return briefDescription;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setSkeleton(java.lang.String)
	 */
	@Override
	public void setSkeleton(String skeleton) {
		this.skeleton = skeleton;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getSkeleton()
	 */
	@Override
	public String getSkeleton() {
		return skeleton;
	}

	/**
	 * @return true if this problem has a skeleton, false if not
	 */
	public boolean hasSkeleton() {
		return skeleton != null;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setSchemaVersion(int)
	 */
	@Override
	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getSchemaVersion()
	 */
	@Override
	public int getSchemaVersion() {
		return schemaVersion;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setAuthorName(java.lang.String)
	 */
	@Override
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getAuthorName()
	 */
	@Override
	public String getAuthorName() {
		return authorName;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setAuthorEmail(java.lang.String)
	 */
	@Override
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getAuthorEmail()
	 */
	@Override
	public String getAuthorEmail() {
		return authorEmail;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setAuthorWebsite(java.lang.String)
	 */
	@Override
	public void setAuthorWebsite(String authorWebsite) {
		this.authorWebsite = authorWebsite;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getAuthorWebsite()
	 */
	@Override
	public String getAuthorWebsite() {
		return authorWebsite;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setTimestampUTC(long)
	 */
	@Override
	public void setTimestampUtc(long timestampUTC) {
		this.timestampUTC = timestampUTC;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getTimestampUTC()
	 */
	@Override
	public long getTimestampUtc() {
		return timestampUTC;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#setLicense(org.cloudcoder.app.shared.model.ProblemLicense)
	 */
	@Override
	public void setLicense(ProblemLicense license) {
		this.license = license;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ProblemData#getLicense()
	 */
	@Override
	public ProblemLicense getLicense() {
		return license;
	}
	
	@Override
	public void setParentHash(String parentHash) {
		this.parentHash = parentHash;
	}
	
	@Override
	public String getParentHash() {
		return parentHash;
	}
	
	@Override
	public void setExternalLibraryUrl(String externalLibraryUrl) {
		this.externalLibraryUrl = externalLibraryUrl;
	}
	
	@Override
	public String getExternalLibraryUrl() {
		return this.externalLibraryUrl;
	}
	
	@Override
	public void setExternalLibraryMD5(String externalLibraryMD5) {
		this.externalLibraryMD5 = externalLibraryMD5;
	}
	
	@Override
	public String getExternalLibraryMD5() {
		return this.externalLibraryMD5;
	}
	
	@Override
	public void setEqualityPredicate(String equalityPredicate) {
		this.equalityPredicate = equalityPredicate;
	}
	
	@Override
	public String getEqualityPredicate() {
		return this.equalityPredicate;
	}
	
	/**
	 * Copy all data in the given ProblemData object into this one.
	 * 
	 * @param other another ProblemData object
	 */
	public void copyFrom(ProblemData other) {
		this.problemType = other.problemType;
		this.testName = other.testName;
		this.briefDescription = other.briefDescription;
		this.description = other.description;
		this.skeleton = other.skeleton;
		this.schemaVersion = other.schemaVersion;
		this.authorName = other.authorName;
		this.authorEmail = other.authorEmail;
		this.authorWebsite = other.authorWebsite;
		this.timestampUTC = other.timestampUTC;
		this.license = other.license;
		this.parentHash = other.parentHash;
		this.externalLibraryUrl = other.externalLibraryUrl;
		this.externalLibraryMD5 = other.externalLibraryMD5;
		this.equalityPredicate = other.equalityPredicate;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ProblemData)) {
			return false;
		}
		ProblemData other = (ProblemData) obj;
		return ModelObjectUtil.equals(this.problemType, other.problemType)
				&& ModelObjectUtil.equals(this.testName, other.testName)
				&& ModelObjectUtil.equals(this.briefDescription, other.briefDescription)
				&& ModelObjectUtil.equals(this.description, other.description)
				&& ModelObjectUtil.equals(this.skeleton, other.skeleton)
				&& this.schemaVersion == other.schemaVersion
				&& ModelObjectUtil.equals(this.authorName, other.authorName)
				&& ModelObjectUtil.equals(this.authorEmail, other.authorEmail)
				&& ModelObjectUtil.equals(this.authorWebsite, other.authorWebsite)
				&& this.timestampUTC == other.timestampUTC
				&& ModelObjectUtil.equals(this.license, other.license)
				&& ModelObjectUtil.equals(this.parentHash, other.parentHash)
				&& ModelObjectUtil.equals(this.externalLibraryUrl, other.externalLibraryUrl)
				&& ModelObjectUtil.equals(this.externalLibraryMD5, other.externalLibraryMD5)
				&& ModelObjectUtil.equals(this.equalityPredicate, other.equalityPredicate);
	}

	/*
	 * Initialize given {@link ProblemData} so that it is in an "empty"
	 * state, appropriate for editing as a new problem.
	 * 
	 * @param empty the {@link ProblemData} to initialize to an empty state
	 */
	public static void initEmpty(ProblemData empty) {
		empty.setProblemType(ProblemType.JAVA_METHOD);
		empty.setTestname("");
		empty.setBriefDescription("");
		empty.setDescription("");
		empty.setSkeleton("");
		empty.setSchemaVersion(ProblemData.SCHEMA.getVersion());
		empty.setAuthorName("");
		empty.setAuthorEmail("");
		empty.setAuthorWebsite("");
		empty.setTimestampUtc(System.currentTimeMillis());
		empty.setLicense(ProblemLicense.NOT_REDISTRIBUTABLE);
		empty.setParentHash("");
		empty.setEqualityPredicate("");
	}
}