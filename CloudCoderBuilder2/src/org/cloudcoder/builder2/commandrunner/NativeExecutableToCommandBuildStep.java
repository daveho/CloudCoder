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

import org.cloudcoder.builder2.ccompiler.CCompilerBuildStep;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.NativeExecutable;

/**
 * An {@link IBuildStep} to create a {@link Command} to execute
 * a {@link NativeExecutable} with no arguments in the directory
 * where the executable file is located.
 * This is useful for executing the executable resulting from
 * the {@link CCompilerBuildStep} if it will be run without
 * arguments.
 * 
 * @author David Hovemeyer
 */
public class NativeExecutableToCommandBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		NativeExecutable nativeExe = submission.getArtifact(NativeExecutable.class);
		if (nativeExe == null) {
			throw new InternalBuilderException(this.getClass(), "No NativeExecutable");
		}
		
		List<String> args = new ArrayList<String>();
		args.add(nativeExe.getExeFileName());
		
		Command command = new Command(nativeExe.getDir(), args);
		submission.addArtifact(command);
	}

}
