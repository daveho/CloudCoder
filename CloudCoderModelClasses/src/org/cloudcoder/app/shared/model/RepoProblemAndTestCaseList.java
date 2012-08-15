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

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a {@link RepoProblem} and its associated
 * {@link RepoTestCase}s.
 * 
 * <em>Important</em>: before storing in the database, be sure to call
 * {@link #computeHash()} so that the hash is properly stored in the
 * {@link RepoProblem} object.
 * 
 * @author David Hovemeyer
 */
public class RepoProblemAndTestCaseList implements IProblemAndTestCaseData<RepoProblem, RepoTestCase> {
	private RepoProblem repoProblem;
	private List<RepoTestCase> repoTestCaseList;
	
	/**
	 * Constructor.
	 */
	public RepoProblemAndTestCaseList() {
		repoTestCaseList = new ArrayList<RepoTestCase>();
	}
	
	/**
	 * Set the RepoProblem.
	 * @param repoProblem the RepoProblem
	 */
	public void setRepoProblem(RepoProblem repoProblem) {
		this.repoProblem = repoProblem;
	}
	
	/**
	 * Add a RepoTestCase.
	 * @param repoTestCase the RepoTestCase to add
	 */
	public void addRepoTestCase(RepoTestCase repoTestCase) {
		repoTestCaseList.add(repoTestCase);
	}
	
	/**
	 * Compute the hash of the problem and its associated test cases.
	 * Sets the hash in the {@link RepoProblem} object.
	 */
	public void computeHash() {
		HashProblemAndTestCaseData<RepoProblemAndTestCaseList> computeHash =
				new HashProblemAndTestCaseData<RepoProblemAndTestCaseList>(this);
		String hash = computeHash.compute();
		repoProblem.setHash(hash);
	}
	
	@Override
	public RepoProblem getProblem() {
		return repoProblem;
	}

	@Override
	public List<RepoTestCase> getTestCaseData() {
		return repoTestCaseList;
	}

}
