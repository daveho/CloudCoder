// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.submitsvc.oop.builder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudcoder.app.shared.model.CompilerDiagnostic;

/**
 * Utilities for creating CompilerDiagnostics.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class CompilerDiagnosticUtil {
    
    // Groups:
	private static final Pattern GCC_ERROR_MSG_PATTERN =
			Pattern.compile("^[^\\:]+:(\\d+)(:(\\d+))?: (error|warning): (.*)$");
	private static final int LINE_NUMBER_GROUP = 1;
//	private static final int COLUMN_NUMBER_GROUP = 3; // could be empty
	private static final int ERROR_OR_WARNING_GROUP = 4;
	private static final int ERROR_MESSAGE_GROUP = 5;

	/**
	 * Convert a string (a possible gcc error message)
	 * into a CompilerDiagnostic.
	 * 
	 * @param s string containing a possible gcc error message 
	 * @return a CompilerDiagnostic, or null if the string was not a gcc error message
	 */
	public static CompilerDiagnostic diagnosticFromGcc(String s) {
		Matcher m = GCC_ERROR_MSG_PATTERN.matcher(s);
		if (m.matches()) {
			int lineNum = Integer.parseInt(m.group(LINE_NUMBER_GROUP));
			String errorOrWarning = m.group(ERROR_OR_WARNING_GROUP);
			String message = m.group(ERROR_MESSAGE_GROUP);
			return new CompilerDiagnostic(lineNum, lineNum, -1, -1, errorOrWarning + ": " + message);
		} else {
			return null;
		}
	}
}
