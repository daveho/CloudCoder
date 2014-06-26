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

package org.cloudcoder.builder2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Tester executes a series of {@link IBuildStep}s on a
 * {@link BuilderSubmission}.
 * 
 * @author David Hovemeyer
 */
public class Tester {
	private static final Logger logger = LoggerFactory.getLogger(Tester.class);
	
	private List<IBuildStep> buildStepList;
	
	/**
	 * Constructor.
	 */
	public Tester() {
		buildStepList = new ArrayList<IBuildStep>();
	}
	
	/**
	 * Add an {@link IBuildStep}.
	 * 
	 * @param buildStep the {@link IBuildStep} to add
	 */
	public void addBuildStep(IBuildStep buildStep) {
		buildStepList.add(buildStep);
	}
	
	/**
	 * Execute the Tester on a {@link BuilderSubmission}.
	 * 
	 * @param submission the {@link BuilderSubmission} to build/test
     * @param config     configuration properties: i.e., properties from cloudcoder.properties file
	 */
	public void execute(BuilderSubmission submission, Properties config) {
		logger.debug("Executing {} build step(s)", buildStepList.size());
		
		for (IBuildStep buildStep : buildStepList) {
			logger.debug("Executing build step: {}", buildStep.getClass().getSimpleName());
			buildStep.execute(submission, config);
			
			// If a SubmissionResult was created, then we finish immediately,
			// even if there are more steps remaining.  This handles, e.g.,
			// a case where the compilation failed and trying to execute
			// the program would be pointless and incorrect.
			if (submission.isComplete()) {
				break;
			}
		}
		
		if (!submission.isComplete()) {
			throw new InternalBuilderException("Executed all build steps but submission is not complete");
		}
	}
}
