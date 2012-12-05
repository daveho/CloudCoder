// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012 Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;

/**
 * Add secret success and failure exit codes to be used
 * by the scaffolding for testing a {@link ProblemType#C_FUNCTION} submission.
 * 
 * @author David Hovemeyer
 */
public class CreateSecretSuccessAndFailureCodesBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		submission.addArtifact(SecretSuccessAndFailureCodes.create());
	}

}
