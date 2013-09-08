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

package org.cloudcoder.builder2.util;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with {@link CompilationResult}s.
 * 
 * @author David Hovemeyer
 */
public class CompilationResultUtil {
	private static final Logger logger = LoggerFactory.getLogger(CompilationResultUtil.class);
	
	/**
	 * Sanitize a {@link CompilationResult} by ensuring that it contains
	 * all required fields.
	 * 
	 * @param compilationResult the {@link CompilationResult} to sanitize
	 */
	public static void sanitizeCompilationResult(CompilationResult compilationResult) {
		if (compilationResult.getOutcome() == null) {
			compilationResult.setOutcome(CompilationOutcome.BUILDER_ERROR);
			logger.warn("CompilationResult is missing a CompilationOutcome");
		}
		
		if (compilationResult.getCompilerDiagnosticList() == null) {
			compilationResult.setCompilerDiagnosticList(new CompilerDiagnostic[0]);
			logger.warn("CompilationResult is missing list of CompilerDiagnostics");
		}
		
		if (ArrayUtil.hasNullElements(compilationResult.getCompilerDiagnosticList())) {
			CompilerDiagnostic[] sanitizedCompilerDiagnostics = ArrayUtil.stripNullElements(compilationResult.getCompilerDiagnosticList());
			compilationResult.setCompilerDiagnosticList(sanitizedCompilerDiagnostics);
		}
	}
}
