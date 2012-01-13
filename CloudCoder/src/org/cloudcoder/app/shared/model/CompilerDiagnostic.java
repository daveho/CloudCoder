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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

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

    /**
     * @param s
     * @return
     */
    public static CompilerDiagnostic diagnosticFromGcc(String s) {
        // gcc compiler errors are in this format:
        // checker.c:5: error: expected ';' before '}' token
        // Going to split using :
        String[] arr=s.split(":");
        int lineNum=Integer.parseInt(arr[1]);
        String message=arr[3];
        return new CompilerDiagnostic(lineNum, lineNum, -1, -1, message);
    }
}
