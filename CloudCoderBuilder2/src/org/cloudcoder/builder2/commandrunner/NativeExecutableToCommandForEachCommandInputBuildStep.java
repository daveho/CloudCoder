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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudcoder.builder2.ccompiler.ExecuteCCompilerBuildStep;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandInput;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.NativeExecutable;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link IBuildStep} to create an array of {@link Command}s to execute
 * a {@link NativeExecutable} with no arguments in the directory
 * where the executable file is located.
 * One {@link Command} is created for each {@link CommandInput}.
 * This is useful for executing the executable resulting from
 * the {@link ExecuteCCompilerBuildStep} if it will be run without
 * arguments with a variety of inputs.  The resulting array of
 * {@link Command} objects is added to the submission as an artifact.
 * 
 * @author David Hovemeyer
 */
public class NativeExecutableToCommandForEachCommandInputBuildStep implements IBuildStep {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		CommandInput[] commandInputList = submission.requireArtifact(this.getClass(), CommandInput[].class);
		
		List<Command> commandList = new ArrayList<Command>();
		
		for (int i = 0; i < commandInputList.length; i++) {
			NativeExecutable nativeExe = submission.requireArtifact(this.getClass(), NativeExecutable.class);
			commandList.add(nativeExe.toCommand());
		}
		
		submission.addArtifact(ArrayUtil.toArray(commandList, Command.class));
		logger.debug("Added {} Commands for NativeExecutable", commandList.size());
		
	}

}
