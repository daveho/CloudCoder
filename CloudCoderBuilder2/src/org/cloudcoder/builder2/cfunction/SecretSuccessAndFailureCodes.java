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

package org.cloudcoder.builder2.cfunction;

import java.util.Random;

import org.cloudcoder.app.shared.model.ProblemType;

/**
 * Secret success and failure codes for testing {@link ProblemType#C_FUNCTION}
 * submissions.  These are passed to each test executable as command line arguments,
 * and the test scaffolding exits with these values as appropriate depending
 * on whether the test succeeded or failed.
 * 
 * @author David Hovemeyer
 */
public class SecretSuccessAndFailureCodes {
	private final int successCode;
	private final int failureCode;
	
	private SecretSuccessAndFailureCodes(int successCode, int failureCode) {
		this.successCode = successCode;
		this.failureCode = failureCode;
	}
	
	/**
	 * @return process exit code indicating a successful (passed) test
	 */
	public int getSuccessCode() {
		return successCode;
	}
	
	/**
	 * @return process exit code indicating a failed test
	 */
	public int getFailureCode() {
		return failureCode;
	}

	/**
	 * Create a randomly-generated {@link SecretSuccessAndFailureCodes} object.
	 * 
	 * @return a randomly-generated {@link SecretSuccessAndFailureCodes} object
	 */
	public static SecretSuccessAndFailureCodes create() {
		int success = chooseRandom();
		int failure = chooseRandom();
		while (failure == success) {
			failure = chooseRandom();
		}
		
		return new SecretSuccessAndFailureCodes(success, failure);
	}
	
	private static final Random random = new Random();

	private static int chooseRandom() {
		// Only the values 0-127 can be used as process exit codes.
		// Also, we use 99 as a special code to indicate that the
		// test executable was invoked with an invalid test name.
		int n;
		do {
			n = random.nextInt(127) + 1;
		} while (n == 99);
		return n;
	}
}
