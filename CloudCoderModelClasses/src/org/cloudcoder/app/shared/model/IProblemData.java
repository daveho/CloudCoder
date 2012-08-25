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
 * Interface describing getters/setters for {@link ProblemData}.
 * Classes that use this interface (instead of using ProblemData directly)
 * can be more flexible because they permit the use of adapter
 * objects.
 * 
 * @author David Hovemeyer
 */
public interface IProblemData {

	public abstract void setProblemType(ProblemType problemType);

	public abstract void setProblemType(int problemType);

	public abstract ProblemType getProblemType();

	/**
	 * @return the testName
	 */
	public abstract String getTestname();

	/**
	 * @param testName the testName to set
	 */
	public abstract void setTestname(String testName);

	public abstract void setBriefDescription(String briefDescription);

	public abstract String getBriefDescription();

	/**
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @param description the description to set
	 */
	public abstract void setDescription(String description);

	/**
	 * @param skeleton the skeleton to set
	 */
	public abstract void setSkeleton(String skeleton);

	/**
	 * @return the skeleton
	 */
	public abstract String getSkeleton();

	/**
	 * Set the schema version.
	 * 
	 * @param schemaVersion the schema version
	 */
	public abstract void setSchemaVersion(int schemaVersion);

	/**
	 * Get the schema version.
	 * 
	 * @return the schema version
	 */
	public abstract int getSchemaVersion();

	/**
	 * Set the name of the author.
	 * 
	 * @param authorName the name of the author
	 */
	public abstract void setAuthorName(String authorName);

	/**
	 * Get the name of the author.
	 * 
	 * @return the name of the author
	 */
	public abstract String getAuthorName();

	/**
	 * Set the author's email address.
	 * @param authorEmail the author's email address
	 */
	public abstract void setAuthorEmail(String authorEmail);

	/**
	 * Get the author's email address.
	 * 
	 * @return the author's email address
	 */
	public abstract String getAuthorEmail();

	/**
	 * Set the URL of the author's website.
	 * 
	 * @param authorWebsite the URL of the author's website
	 */
	public abstract void setAuthorWebsite(String authorWebsite);

	/**
	 * Get the URL of the author's website
	 * 
	 * @return the URL of the author's website
	 */
	public abstract String getAuthorWebsite();

	/**
	 * Get the "creation" timestamp in seconds past the epoch, UTC.
	 * 
	 * @param timestampUTC the "creation" timestamp in seconds past the epoch, UTC
	 */
	public abstract void setTimestampUtc(long timestampUTC);

	/**
	 * Get the "creation" timestamp in seconds past the epoch, UTC.
	 * 
	 * @return the "creation" timestamp in seconds past the epoch, UTC
	 */
	public abstract long getTimestampUtc();

	/**
	 * Set the license under which this problem is available.
	 * 
	 * @param license the license under which this problem is available
	 */
	public abstract void setLicense(ProblemLicense license);

	/**
	 * Get the license under which this problem is available.
	 * 
	 * @return  the license under which this problem is available
	 */
	public abstract ProblemLicense getLicense();
	
	/**
	 * Set the SHA-1 hash of the parent problem (from which this problem was derived).
	 * 
	 * @param parentHash SHA-1 hash of the parent problem
	 */
	public void setParentHash(String parentHash);
	
	/**
	 * Return the SHA-1 hash of the parent problem (from which this problem was derived).
	 * 
	 * @return SHA-1 hash of the parent problem, or an empty string if this is not a derived problem
	 */
	public String getParentHash();

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
	/** {@link ModelObjectField} for description. */
	public static final ModelObjectField<IProblemData, String> DESCRIPTION =
			new ModelObjectField<IProblemData, String>("description", String.class, 8192, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
		public void set(IProblemData obj, String value) { obj.setDescription(value); }
		public String get(IProblemData obj) { return obj.getDescription(); }
	};
	/** {@link ModelObjectField} for skeleton. */
	public static final ModelObjectField<IProblemData, String> SKELETON =
			new ModelObjectField<IProblemData, String>("skeleton", String.class, 400, ModelObjectIndexType.NONE, ModelObjectField.LITERAL) {
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
	
	/**
	 * Description of fields (version 0 schema).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA_V0 = new ModelObjectSchema<IProblemData>("problem_data")
		.add(PROBLEM_TYPE)
		.add(TESTNAME)
		.add(BRIEF_DESCRIPTION)
		.add(DESCRIPTION)
		.add(SKELETON)
		.add(SCHEMA_VERSION)
		.add(AUTHOR_NAME)
		.add(AUTHOR_EMAIL)
		.add(AUTHOR_WEBSITE)
		.add(TIMESTAMP_UTC)
		.add(LICENSE);
	
	/**
	 * Description of fields (current schema).
	 */
	public static final ModelObjectSchema<IProblemData> SCHEMA = ModelObjectSchema.deltaFrom(SCHEMA_V0)
		.addAfter(LICENSE, PARENT_HASH)
		.finishDelta();
}