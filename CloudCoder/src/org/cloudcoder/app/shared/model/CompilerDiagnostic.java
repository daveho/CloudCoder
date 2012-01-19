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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jaimespacco
 *
 */
public class CompilerDiagnostic implements Serializable
{
    public static final long serialVersionUID=1L;
    
    private long startLine;
    private long startColumn;
    private long endLine;
    private long endColumn;
    private String message;
    
    public void adjustDiagnosticLineNumbers(int prologue, int epilogue) {
        // Student-written code may be embedded in a test harness
        // Using the number of lines of prologue/epilogue code
        // re-write all of the diagnostics to refer only to the student code
        startLine-=prologue;
        endLine-=epilogue;
    }
    
    public String toString() {
        return message+": startLine: "+startLine+
                ", endLine: "+endLine+
                ", startColumn: "+startColumn+
                ", endColumn: "+endColumn;
    }
    
    public CompilerDiagnostic() {
        this.startLine=-1;
        this.endLine=-1;
        this.startColumn=-1;
        this.endColumn=-1;
        this.message="";
    }

    public CompilerDiagnostic(long startLine, long endLine, long startColumn, long endColumn, String message) {
        this.startLine=startLine;
        this.endLine=endLine;
        this.startColumn=startColumn;
        this.endColumn=endColumn;
        this.message=message;
    }
    
    
    
    /**
     * @return the startLine
     */
    public long getStartLine() {
        return startLine;
    }
    /**
     * @return the startColumn
     */
    public long getStartColumn() {
        return startColumn;
    }
    /**
     * @return the endLine
     */
    public long getEndLine() {
        return endLine;
    }
    /**
     * @return the endColumn
     */
    public long getEndColumn() {
        return endColumn;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    // Groups:
	private static final Pattern GCC_ERROR_MSG_PATTERN =
			Pattern.compile("^[^\\:]+:(\\d+)(:(\\d+))?: error: (.*)$");
	private static final int LINE_NUMBER_GROUP = 1;
//	private static final int COLUMN_NUMBER_GROUP = 3; // could be empty
	private static final int ERROR_MESSAGE_GROUP = 4;

	/**
	 * Convert a string (a possible gcc error message)
	 * into a CompilerDiagnostic.
	 * 
	 * @param s string containing a possible gcc error message 
	 * @return a CompilerDiagnostic, or null if the string was not a gcc error message
	 */
	public static CompilerDiagnostic diagnosticFromGcc(String s) {
		System.out.println("Try: " + s);
		Matcher m = GCC_ERROR_MSG_PATTERN.matcher(s);
		if (m.matches()) {
			int lineNum = Integer.parseInt(m.group(LINE_NUMBER_GROUP));
			String message = m.group(ERROR_MESSAGE_GROUP);
			return new CompilerDiagnostic(lineNum, lineNum, -1, -1, message);
		} else {
			System.out.println("  no");
			return null;
		}
	}
}
