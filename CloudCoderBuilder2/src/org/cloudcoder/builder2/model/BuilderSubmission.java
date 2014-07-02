// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * A submission to the builder.
 * It will be processed by a series of {@link IBuildStep}s.
 * Initially, {@link Problem}, {@link TestCase} array, and {@link ProgramSource}
 * artifacts are added to describe the submission.
 * Each build step can add arbitrary artifact objects
 * (such as executables and test results).
 * The submission is complete when a build step adds
 * a {@link SubmissionResult} artifact.
 * 
 * @author David Hovemeyer
 */
public class BuilderSubmission {
	private Map<Class<?>, Object> artifactMap;
	private CleanupActionStack cleanupActionStack;
	private List<ISubmissionResultHook> submissionResultHooks;

	/**
	 * Constructor.
	 */
	public BuilderSubmission() {
		artifactMap = new HashMap<Class<?>, Object>();
		cleanupActionStack = new CleanupActionStack();
		submissionResultHooks = new ArrayList<ISubmissionResultHook>();
	}
	
	/**
	 * Add an artifact.
	 * If an artifact of the given artifact's type already exists,
	 * it is replaced.
	 * 
	 * @param artifact the artifact to add or replace
	 */
	public void addArtifact(Object artifact) {
		artifactMap.put(artifact.getClass(), artifact);
	}

	/**
	 * Get artifact of given type.
	 * 
	 * @param type the artifact type
	 * @return the artifact of the given type, or null if there is no such artifact
	 */
	@SuppressWarnings("unchecked")
	public<E> E getArtifact(Class<E> type) {
		Object artifact = artifactMap.get(type);
		if (artifact != null) {
			return (E) artifact;
		}
		return null;
	}
	
	/**
	 * Require artifact of given type.
	 * 
	 * @param the class calling this method
	 * @param type the artifact type
	 * @return the artifact of the given type
	 * @throws InternalBuilderException if there is no artifact of the required type
	 */
	public<E> E requireArtifact(Class<?> callerCls, Class<E> type) {
		E obj = getArtifact(type);
		if (obj == null) {
			throw new InternalBuilderException(callerCls, "No " + type.getSimpleName());
		}
		return obj;
	}
	
	/**
	 * Check whether the submission has an artifact of the given type.
	 * 
	 * @param type the artifact type
	 * @return true if the submission has an artifact of the given type, false otherwise
	 */
	public boolean hasArtifact(Class<?> type) {
		return getArtifact(type) != null;
	}
	
	/**
	 * Check whether or not this submission is complete
	 * (has a {@link SubmissionResult}).
	 *  
	 * @return true if this submission is complete, false otherwise
	 */
	public boolean isComplete() {
		return hasArtifact(SubmissionResult.class);
	}

	/**
	 * Add an {@link ICleanupAction} to be executed when building/testing
	 * of this submission is complete.
	 * 
	 * @param action the {@link ICleanupAction}
	 */
	public void addCleanupAction(ICleanupAction action) {
		cleanupActionStack.push(action);
	}
	
	/**
	 * Execute all cleanup actions (starting with the most recently added
	 * and working back.)
	 */
	public void executeAllCleanupActions() {
		cleanupActionStack.executeAll();
	}
	
	/**
	 * Add a {@link ISubmissionResultHook} to be executed on the finished
	 * {@link SubmissionResult}.
	 * 
	 * @param hook the {@link ISubmissionResultHook}
	 */
	public void addSubmissionResultHook(ISubmissionResultHook hook) {
		submissionResultHooks.add(hook);
	}
	
	/**
	 * Execute all {@link ISubmissionResultHook}s on the {@link SubmissionResult}.
	 * This should not be called unless {@link #isComplete()} returns true.
	 */
	public void executeAllSubmissionResultHooks() {
		SubmissionResult result = requireArtifact(this.getClass(), SubmissionResult.class);
		for (ISubmissionResultHook hook : submissionResultHooks) {
			hook.invoke(result);
		}
	}
}
