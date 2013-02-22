// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.batch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;
import org.cloudcoder.builder2.server.Builder2;
import org.cloudcoder.daemon.IOUtil;

/**
 * Front-end for batch-mode testing.
 * 
 * @author David Hovemeyer
 */
public class BatchMain {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: java -jar cloudcoderBuilder.jar batch <exercise JSON> <source file list>");
			System.exit(1);
		}
		
		String exerciseJSON = args[0];
		String sourceFileListFilename = args[1];

		ProblemAndTestCaseList exercise = new ProblemAndTestCaseList();
		
		// Read the exercise (Problem and TestCases)
		Reader r = null;
		try {
			r = new BufferedReader(new FileReader(exerciseJSON));
			JSONConversion.readProblemAndTestCaseData(
					exercise,
					ReflectionFactory.forClass(Problem.class),
					ReflectionFactory.forClass(TestCase.class),
					r);
		} finally {
			IOUtil.closeQuietly(r);
		}
		TestCase[] testCaseList = exercise.getTestCaseList();
		
		// Read the list of source files to test
		List<String> sourceFileList = new ArrayList<String>();
		BufferedReader r2 = null;
		try {
			r2 = new BufferedReader(new FileReader(sourceFileListFilename));
			while (true) {
				String sourceFile = r2.readLine();
				if (sourceFile == null) {
					break;
				}
				sourceFileList.add(sourceFile);
			}
		} finally {
			IOUtil.closeQuietly(r2);
		}
		
		// Test each submission
		for (String sourceFile : sourceFileList) {
			SubmissionResult result =
					Builder2.testSubmission(exercise.getProblem(), Arrays.asList(testCaseList), sourceFile);
			
			// FIXME: For now, just a hard-coded output format summarizing compilation status and test results
			System.out.print(result.getCompilationResult().getOutcome());
			TestResult[] testResults = result.getTestResults();
			for (int i = 0; i < testCaseList.length; i++) {
				System.out.print(",");
				if (i >= testResults.length) {
					System.out.print("false");
				} else {
					System.out.print(String.valueOf(testResults[i].getOutcome() == TestOutcome.PASSED));
				}
			}
			System.out.println();
		}
	}
}
