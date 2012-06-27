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

import java.nio.charset.Charset;

/**
 * Compute a hash of the data in a {@link ProblemAndTestCaseData} object.
 * The hash should be "unique enough" that collisions will not arise
 * in practice.
 * 
 * @author David Hovemeyer
 */
public class HashProblemAndTestCaseData {
	private ProblemAndTestCaseData problemAndTestCaseData;
	private SHA1 sha1;
	private Charset utf8;
	
	/**
	 * Constructor.
	 * 
	 * @param problemAndTestCaseData the ProblemData to compute a hash for
	 */
	public HashProblemAndTestCaseData(ProblemAndTestCaseData problemAndTestCaseData) {
		this.problemAndTestCaseData = problemAndTestCaseData;
		this.sha1 = new SHA1();
		this.utf8 = Charset.forName("UTF-8");
	}
	
	/**
	 * Compute the hash.  The result is a 40-character hex string (160 bits of data).
	 * 
	 * @return the hash
	 */
	public String compute() {
		// Incorporate the ProblemData
		hashProblemData(problemAndTestCaseData.getProblemData());

		// Incorporate each TestCase
		for (TestCaseData testCaseData : problemAndTestCaseData.getTestCaseList()) {
			hashTestCaseData(testCaseData);
		}

		// Return the computed hash as a hex string
		byte[] digest = sha1.digest();
		return new ConvertBytesToHex(digest).convert();
	}

	private void hashProblemData(ProblemData problemData) {
		// Fields present in schema version 0 and later.
		updateString(problemData.getProblemType().toString());
		updateString(problemData.getTestName());
		updateString(problemData.getBriefDescription());
		updateString(problemData.getDescription());
		updateString(problemData.getSkeleton());
		updateInt(problemData.getSchemaVersion());
		updateString(problemData.getAuthorName());
		updateString(problemData.getAuthorEmail());
		updateString(problemData.getAuthorWebsite());
		updateLong(problemData.getTimestampUTC());
		updateString(problemData.getLicense().toString());
		// Note: parent hash is NOT digested.
		
		// TODO: based on schema version, may need to digest additional fields
	}

	private void hashTestCaseData(TestCaseData testCaseData) {
		updateString(testCaseData.getTestCaseName());
		updateString(testCaseData.getInput());
		updateString(testCaseData.getOutput());
		updateBoolean(testCaseData.isSecret());
	}

	private void updateString(String s) {
		// Note: we always digest the trimmed version of the string,
		// and always using UTF-8 to convert to bytes.
		sha1.update(s.trim().getBytes(utf8));
	}
	
	private void updateInt(int value) {
		// Digest an integer value by first converting it to a string.
		updateString(String.valueOf(value));
	}
	
	private void updateLong(long value) {
		// Digest an integer value by first converting it to a string.
		updateString(String.valueOf(value));
	}

	private void updateBoolean(boolean value) {
		// Digest a boolean value by first converting it to a string.
		updateString(String.valueOf(value));
	}
}
