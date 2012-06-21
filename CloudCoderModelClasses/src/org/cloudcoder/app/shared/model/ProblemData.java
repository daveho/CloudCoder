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
public class ProblemData implements ActivityObject {
	private static final long serialVersionUID = 1L;

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
	
	/**
	 * Number of fields.
	 */
	public static final int NUM_FIELDS = 12;
	
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

	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}

	public void setProblemType(int problemType) {
		this.problemType = ProblemType.values()[problemType];
	}

	public ProblemType getProblemType() {
		return problemType;
	}

	/**
	 * @return the testName
	 */
	public String getTestName() {
		return testName;
	}

	/**
	 * @param testName the testName to set
	 */
	public void setTestName(String testName) {
		this.testName = testName;
	}

	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}

	public String getBriefDescription() {
		return briefDescription;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param skeleton the skeleton to set
	 */
	public void setSkeleton(String skeleton) {
		this.skeleton = skeleton;
	}

	/**
	 * @return the skeleton
	 */
	public String getSkeleton() {
		return skeleton;
	}

	/**
	 * @return true if this problem has a skeleton, false if not
	 */
	public boolean hasSkeleton() {
		return skeleton != null;
	}

	/**
	 * Set the schema version.
	 * 
	 * @param schemaVersion the schema version
	 */
	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}
	
	/**
	 * Get the schema version.
	 * 
	 * @return the schema version
	 */
	public int getSchemaVersion() {
		return schemaVersion;
	}

	/**
	 * Set the name of the author.
	 * 
	 * @param authorName the name of the author
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	/**
	 * Get the name of the author.
	 * 
	 * @return the name of the author
	 */
	public String getAuthorName() {
		return authorName;
	}
	
	/**
	 * Set the author's email address.
	 * @param authorEmail the author's email address
	 */
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}
	
	/**
	 * Get the author's email address.
	 * 
	 * @return the author's email address
	 */
	public String getAuthorEmail() {
		return authorEmail;
	}
	
	/**
	 * Set the URL of the author's website.
	 * 
	 * @param authorWebsite the URL of the author's website
	 */
	public void setAuthorWebsite(String authorWebsite) {
		this.authorWebsite = authorWebsite;
	}
	
	/**
	 * Get the URL of the author's website
	 * 
	 * @return the URL of the author's website
	 */
	public String getAuthorWebsite() {
		return authorWebsite;
	}
	
	/**
	 * Get the "creation" timestamp in seconds past the epoch, UTC.
	 * 
	 * @param timestampUTC the "creation" timestamp in seconds past the epoch, UTC
	 */
	public void setTimestampUTC(long timestampUTC) {
		this.timestampUTC = timestampUTC;
	}
	
	/**
	 * Get the "creation" timestamp in seconds past the epoch, UTC.
	 * 
	 * @return the "creation" timestamp in seconds past the epoch, UTC
	 */
	public long getTimestampUTC() {
		return timestampUTC;
	}
	
	/**
	 * Set the license under which this problem is available.
	 * 
	 * @param license the license under which this problem is available
	 */
	public void setLicense(ProblemLicense license) {
		this.license = license;
	}
	
	/**
	 * Get the license under which this problem is available.
	 * 
	 * @return  the license under which this problem is available
	 */
	public ProblemLicense getLicense() {
		return license;
	}
	
	/**
	 * Set the SHA-1 hash of the problem from which this problem was derived.
	 * An empty string means that this is not a derived problem.
	 * 
	 * @param parentHash the SHA-1 hash of the problem from which this problem was derived
	 */
	public void setParentHash(String parentHash) {
		this.parentHash = parentHash;
	}
	
	/**
	 * Get the SHA-1 hash of the problem from which this problem was derived.
	 * An empty string means that this is not a derived problem.
	 * 
	 * @return the SHA-1 hash of the problem from which this problem was derived
	 */
	public String getParentHash() {
		return parentHash;
	}
}