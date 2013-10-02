// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.extlib;

import java.util.Properties;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.ExternalLibrary;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;

/**
 * Build step to fetch an {@link ExternalLibrary} if necessary and
 * make it available in the {@link BuilderSubmission} so it can be used
 * by other build steps.
 * 
 * @author David Hovemeyer
 */
public class FetchExternalLibraryBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		Problem problem = submission.requireArtifact(this.getClass(), Problem.class);
		
		// Easy case: if the Problem doesn't require an external library, then there's
		// nothing to do.
		if (problem.getExternalLibraryUrl() == null || problem.getExternalLibraryUrl().equals("")) {
			return;
		}

		// Get the ExternalLibrary object from the ExternalLibraryCache.
		// This will attempt to download the library if necessary.
		ExternalLibraryCache cache = ExternalLibraryCache.getInstance(config);
		ExternalLibrary extlib;
		try {
			extlib = cache.get(problem.getExternalLibraryUrl(), problem.getExternalLibraryMD5());
		} catch (InterruptedException e) {
			throw new InternalBuilderException(FetchExternalLibraryBuildStep.class, "Interrupted fetching external library", e);
		}
		
		// If the ExternalLibrary is marked as not available, then there was
		// a problem downloading it.
		if (!extlib.isAvailable()) {
			throw new InternalBuilderException(FetchExternalLibraryBuildStep.class,
					"Required external library " + problem.getExternalLibraryUrl() + " is not available");
		}

		// The ExternalLibrary is marked as available, so we should be good.
		submission.addArtifact(extlib);
	}

}
