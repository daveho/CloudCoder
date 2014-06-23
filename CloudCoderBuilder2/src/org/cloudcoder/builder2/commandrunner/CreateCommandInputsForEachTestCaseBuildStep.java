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

package org.cloudcoder.builder2.commandrunner;

import java.util.Properties;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CommandInput;
import org.cloudcoder.builder2.model.IBuildStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a {@link CommandInput} with the input from each {@link TestCase}.
 * 
 * @author David Hovemeyer
 */
public class CreateCommandInputsForEachTestCaseBuildStep implements IBuildStep {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		TestCase[] testCaseList = submission.requireArtifact(this.getClass(), TestCase[].class);
		
		CommandInput[] commandInputList = new CommandInput[testCaseList.length];
		for (int i = 0; i < testCaseList.length; i++) {
			commandInputList[i] = new CommandInput(testCaseList[i].getInput());
		}
		submission.addArtifact(commandInputList);
		
		logger.debug("Added {} CommandInputs", commandInputList.length);
		
	}

}
