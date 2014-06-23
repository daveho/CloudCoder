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

package org.cloudcoder.builder2.javamethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.ArrayUtil;

/**
 * Add a test driver class to execute all {@link TestCase}s against
 * a scaffolded JAVA_METHOD submission.  This step should be
 * executed <em>after</em> {@link AddJavaMethodScaffoldingBuildStep}.
 * 
 * @author David Hovemeyer
 *
 */
public class AddJavaMethodTestDriverBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		TestCase[] testCaseList = submission.requireArtifact(this.getClass(), TestCase[].class);
		Problem problem = submission.requireArtifact(this.getClass(), Problem.class);

        StringBuilder tester = new StringBuilder();
        tester.append("public class Tester {\n");
        tester.append("\tpublic static boolean eq(Object o1, Object o2) { return o1.equals(o2); }\n");
        for (TestCase tc : testCaseList) {
            tester.append("\tpublic static Object[] ");
            tester.append(tc.getTestCaseName());
            tester.append("() {\n");
            tester.append("\t\tTest t = new Test();\n");
            tester.append("\t\tObject theresult=t."+problem.getTestname() + "(" + tc.getInput() + ");\n");
            //TODO capture the return value somehow...
            //Could print it, maybe with a unique tag, and then pull it out of stderr
            //Could have these methods return a compound type that includes the generated output
            // how to access that?
            tester.append("\t\tBoolean b=eq(theresult, " + tc.getOutput() + ");\n");
            tester.append("\t\treturn new Object[] {b, theresult.toString()};\n");
            tester.append("\t\t}\n");
        }
        tester.append("}");
        String testerCode = tester.toString();

        // Create ProgramSource for test driver
        ProgramSource testerProgramSource = new ProgramSource(testerCode);

        // Get existing ProgramSource objects
        ProgramSource[] programSourceList = submission.requireArtifact(this.getClass(), ProgramSource[].class);

		// Build array of all ProgramSource
		List<ProgramSource> allProgramSourceList = new ArrayList<ProgramSource>();
		allProgramSourceList.add(testerProgramSource);
		allProgramSourceList.addAll(Arrays.asList(programSourceList));
		
		// Add updated ProgramSource array to submission
		submission.addArtifact(ArrayUtil.toArray(allProgramSourceList, ProgramSource.class));
	}

}
