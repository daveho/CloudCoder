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
 * The course- and institution-independent data in a {@link Problem}.
 * This class represents the information about a problem that will
 * be exported when an instructor shares a problem to the repository.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class ProblemData implements ActivityObject, IProblemData {
	private static final long serialVersionUID = 1L;

	//
	// IMPORTANT: if you add any fields, make sure that you
	// update the duplicateProblemData() method so that they are copied
	// into the returned object.
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
	
	/**
	 * Number of fields.
	 */
	public static final int NUM_FIELDS = 11;
	
	/**
	 * The current ProblemData schema version.
	 */
	public static final int CURRENT_SCHEMA_VERSION = 0;

	/**
	 * Constructor.
	 */
	public ProblemData() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setProblemType(org.cloudcoder.app.shared.model.ProblemType)
	 */
	@Override
	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setProblemType(int)
	 */
	@Override
	public void setProblemType(int problemType) {
		this.problemType = ProblemType.values()[problemType];
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getProblemType()
	 */
	@Override
	public ProblemType getProblemType() {
		return problemType;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getTestName()
	 */
	@Override
	public String getTestName() {
		return testName;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setTestName(java.lang.String)
	 */
	@Override
	public void setTestName(String testName) {
		this.testName = testName;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setBriefDescription(java.lang.String)
	 */
	@Override
	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getBriefDescription()
	 */
	@Override
	public String getBriefDescription() {
		return briefDescription;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setSkeleton(java.lang.String)
	 */
	@Override
	public void setSkeleton(String skeleton) {
		this.skeleton = skeleton;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getSkeleton()
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
	 * @see org.cloudcoder.app.shared.model.IProblemData#setSchemaVersion(int)
	 */
	@Override
	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getSchemaVersion()
	 */
	@Override
	public int getSchemaVersion() {
		return schemaVersion;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setAuthorName(java.lang.String)
	 */
	@Override
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getAuthorName()
	 */
	@Override
	public String getAuthorName() {
		return authorName;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setAuthorEmail(java.lang.String)
	 */
	@Override
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getAuthorEmail()
	 */
	@Override
	public String getAuthorEmail() {
		return authorEmail;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setAuthorWebsite(java.lang.String)
	 */
	@Override
	public void setAuthorWebsite(String authorWebsite) {
		this.authorWebsite = authorWebsite;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getAuthorWebsite()
	 */
	@Override
	public String getAuthorWebsite() {
		return authorWebsite;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setTimestampUTC(long)
	 */
	@Override
	public void setTimestampUTC(long timestampUTC) {
		this.timestampUTC = timestampUTC;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getTimestampUTC()
	 */
	@Override
	public long getTimestampUTC() {
		return timestampUTC;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#setLicense(org.cloudcoder.app.shared.model.ProblemLicense)
	 */
	@Override
	public void setLicense(ProblemLicense license) {
		this.license = license;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblemData#getLicense()
	 */
	@Override
	public ProblemLicense getLicense() {
		return license;
	}

	/**
	 * Create a ProblemData object that contains exactly the same
	 * field values as this one.  Note that the object returned
	 * will be precisely of type ProblemData, not a subclass.
	 * (This method doesn't return a clone of the current
	 * object if it is a subclass of ProblemData.)
	 * 
	 * @return ProblemData object with the same field values as this one
	 */
	public ProblemData duplicateProblemData() {
		ProblemData dup = new ProblemData();

		dup.problemType = this.problemType;
		dup.testName = this.testName;
		dup.briefDescription = this.briefDescription;
		dup.description = this.description;
		dup.skeleton = this.skeleton;
		dup.schemaVersion = this.schemaVersion;
		dup.authorName = this.authorName;
		dup.authorEmail = this.authorEmail;
		dup.authorWebsite = this.authorWebsite;
		dup.timestampUTC = this.timestampUTC;
		dup.license = this.license;
		
		return dup;
	}
}