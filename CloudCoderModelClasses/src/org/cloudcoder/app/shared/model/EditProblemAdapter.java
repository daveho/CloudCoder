// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012,2018 David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * Adapter class for editing an {@link IProblem}.
 * Subclasses must implement the {@link #onChange()} method,
 * which is called whenever the underlying IProblem object is
 * modified with a setter method.
 */
public abstract class EditProblemAdapter implements IProblem {
	private final IProblem delegate;

	/**
	 * Constructor.
	 * 
	 * @param delegate an IProblem object to which getter/setter calls will be delegated
	 */
	public EditProblemAdapter(IProblem delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * Get the delegate.
	 * This can be useful if it is necessary to modify the
	 * underlying {@link IProblem} without triggering a call
	 * to {@link #onChange()}.
	 * 
	 * @return the delegate
	 */
	public IProblem getDelegate() {
		return delegate;
	}

	/**
	 * Downcall method: is called whenever a setter method has been called
	 * and the IProblem object to which the adapter is delegating
	 * might have changed.
	 */
	protected abstract void onChange();

	@Override
	public void setProblemType(ProblemType problemType) {
		delegate.setProblemType(problemType);
		onChange();
	}

	@Override
	public void setProblemType(int problemType) {
		delegate.setProblemType(problemType);
		onChange();
	}

	@Override
	public ProblemType getProblemType() {
		return delegate.getProblemType();
	}

	@Override
	public String getTestname() {
		return delegate.getTestname();
	}

	@Override
	public void setTestname(String testName) {
		delegate.setTestname(testName);
		onChange();
	}

	@Override
	public void setBriefDescription(String briefDescription) {
		delegate.setBriefDescription(briefDescription);
		onChange();
	}

	@Override
	public String getBriefDescription() {
		return delegate.getBriefDescription();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public void setDescription(String description) {
		delegate.setDescription(description);
		onChange();
	}

	@Override
	public void setSkeleton(String skeleton) {
		delegate.setSkeleton(skeleton);
		onChange();
	}

	@Override
	public String getSkeleton() {
		return delegate.getSkeleton();
	}

	@Override
	public void setSchemaVersion(int schemaVersion) {
		delegate.setSchemaVersion(schemaVersion);
		onChange();
	}

	@Override
	public int getSchemaVersion() {
		return delegate.getSchemaVersion();
	}

	@Override
	public void setAuthorName(String authorName) {
		delegate.setAuthorName(authorName);
		onChange();
	}

	@Override
	public String getAuthorName() {
		return delegate.getAuthorName();
	}

	@Override
	public void setAuthorEmail(String authorEmail) {
		delegate.setAuthorEmail(authorEmail);
		onChange();
	}

	@Override
	public String getAuthorEmail() {
		return delegate.getAuthorEmail();
	}

	@Override
	public void setAuthorWebsite(String authorWebsite) {
		delegate.setAuthorWebsite(authorWebsite);
		onChange();
	}

	@Override
	public String getAuthorWebsite() {
		return delegate.getAuthorWebsite();
	}

	@Override
	public void setTimestampUtc(long timestampUTC) {
		delegate.setTimestampUtc(timestampUTC);
		onChange();
	}

	@Override
	public long getTimestampUtc() {
		return delegate.getTimestampUtc();
	}

	@Override
	public void setLicense(ProblemLicense license) {
		delegate.setLicense(license);
		onChange();
	}

	@Override
	public ProblemLicense getLicense() {
		return delegate.getLicense();
	}
	
	@Override
	public void setParentHash(String parentHash) {
		delegate.setParentHash(parentHash);
		onChange();
	}
	
	@Override
	public String getParentHash() {
		return delegate.getParentHash();
	}
	
	@Override
	public void setExternalLibraryUrl(String externalLibraryUrl) {
		delegate.setExternalLibraryUrl(externalLibraryUrl);
		onChange();
	}
	
	@Override
	public String getExternalLibraryUrl() {
		return delegate.getExternalLibraryUrl();
	}
	
	@Override
	public void setExternalLibraryMD5(String md5Hash) {
		delegate.setExternalLibraryMD5(md5Hash);
		onChange();
	}
	
	@Override
	public String getExternalLibraryMD5() {
		return delegate.getExternalLibraryMD5();
	}

	@Override
	public Integer getProblemId() {
		return delegate.getProblemId();
	}

	@Override
	public void setProblemId(Integer id) {
		delegate.setProblemId(id);
		onChange();
	}

	@Override
	public Integer getCourseId() {
		return delegate.getCourseId();
	}

	@Override
	public void setCourseId(Integer courseId) {
		delegate.setCourseId(courseId);
		onChange();
	}

	@Override
	public long getWhenAssigned() {
		return delegate.getWhenAssigned();
	}

	@Override
	public Date getWhenAssignedAsDate() {
		return delegate.getWhenAssignedAsDate();
	}

	@Override
	public void setWhenAssigned(long whenAssigned) {
		delegate.setWhenAssigned(whenAssigned);
		onChange();
	}

	@Override
	public long getWhenDue() {
		return delegate.getWhenDue();
	}

	@Override
	public Date getWhenDueAsDate() {
		return delegate.getWhenDueAsDate();
	}

	@Override
	public void setWhenDue(long whenDue) {
		delegate.setWhenDue(whenDue);
		onChange();
	}

	@Override
	public void setVisible(boolean visible) {
		delegate.setVisible(visible);
		onChange();
	}

	@Override
	public boolean isVisible() {
		return delegate.isVisible();
	}
	
	@Override
	public void setProblemAuthorship(ProblemAuthorship problemAuthorship) {
		delegate.setProblemAuthorship(problemAuthorship);
		onChange();
	}
	
	@Override
	public ProblemAuthorship getProblemAuthorship() {
		return delegate.getProblemAuthorship();
	}
	
	@Override
	public void setDeleted(boolean deleted) {
		delegate.setDeleted(deleted);
		onChange();
	}
	
	@Override
	public boolean isDeleted() {
		return delegate.isDeleted();
	}
	
	@Override
	public void setModuleId(int moduleId) {
		delegate.setModuleId(moduleId);
		onChange();
	}
	
	@Override
	public int getModuleId() {
		return delegate.getModuleId();
	}

	@Override
	public void setShared(boolean shared) {
		delegate.setShared(shared);
		onChange();
	}
	
	@Override
	public boolean isShared() {
		return delegate.isShared();
	}
	
	@Override
	public void setEqualityPredicate(String equalityPredicate) {
		delegate.setEqualityPredicate(equalityPredicate);
		onChange();
	}
	
	@Override
	public String getEqualityPredicate() {
		return delegate.getEqualityPredicate();
	}
}
