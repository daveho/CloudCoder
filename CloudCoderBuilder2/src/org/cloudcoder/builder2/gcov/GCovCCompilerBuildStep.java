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

package org.cloudcoder.builder2.gcov;

import java.util.Properties;

import org.cloudcoder.builder2.ccompiler.Compiler;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.util.PropertyUtil;

/**
 * Modify the {@link Compiler} to enable test coverage using
 * gcov.  This obviously requires that gcc/g++ is used for
 * compilation.  This step is a no-op if gcov is not enabled
 * in cloudcoder.properties.
 * 
 * @author David Hovemeyer
 */
public class GCovCCompilerBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		if (!PropertyUtil.isEnabled(config, "cloudcoder.builder2.cprog.gcov")) {
			return;
		}

		Compiler compiler = submission.requireArtifact(this.getClass(), Compiler.class);
		
		// See: https://gcc.gnu.org/onlinedocs/gcc/Invoking-Gcov.html
		compiler.addFlag("-fprofile-arcs");
		compiler.addFlag("-ftest-coverage");
	}

}
