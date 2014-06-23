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

import java.util.Properties;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;

/**
 * Add scaffolding to the {@link ProgramSource} of a
 * {@link ProblemType#JAVA_METHOD} submission.  This step must execute
 * <em>before</em> {@link AddJavaMethodTestDriverBuildStep}.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class AddJavaMethodScaffoldingBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		ProgramSource[] programSourceList = submission.requireArtifact(this.getClass(), ProgramSource[].class);
		
		if (programSourceList.length > 1) {
			throw new InternalBuilderException(this.getClass(), "JAVA_METHOD testing can't handle multiple source files");
		}
		ProgramSource programSource = programSourceList[0];

		StringBuilder test = new StringBuilder();
		test.append("public class Test {\n");
		test.append(programSource.getProgramText() + "\n");
		test.append("}\n");

		String testCode = test.toString();

		ProgramSource scaffoldedProgramSource = new ProgramSource(testCode, 1, 2);

		// The scaffolded program source replaces the original program source
		submission.addArtifact(new ProgramSource[]{scaffoldedProgramSource});
	}

}
