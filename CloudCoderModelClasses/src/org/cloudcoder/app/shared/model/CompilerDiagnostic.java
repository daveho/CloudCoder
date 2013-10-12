// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * A compiler diagnostic (compiler error, warning, etc.)  Indicates
 * some sort of invalid construct in the student's source code.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
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
    
    @Override
    public int hashCode() {
    	return (int) ((startLine*2207) + (endLine*937) + (startColumn*443) + (endColumn*113) + message.hashCode());
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (obj == null || !(obj instanceof CompilerDiagnostic)) {
    		return false;
    	}
    	CompilerDiagnostic other = (CompilerDiagnostic) obj;
    	return this.startLine == other.startLine
    			&& this.endLine == other.endLine
    			&& this.startColumn == other.startColumn
    			&& this.endColumn == other.endColumn
    			&& ModelObjectUtil.equals(this.message, other.message);
    }
    
    @Override
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
    
    public CompilerDiagnostic(CompilerDiagnostic d) {
        this(d.startLine, d.endLine, d.startColumn, d.endColumn, d.message);
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
    public void setStartLine(long startLine) {
        this.startLine = startLine;
    }
    public void setStartColumn(long startColumn) {
        this.startColumn = startColumn;
    }
    public void setEndLine(long endLine) {
        this.endLine = endLine;
    }
    public void setEndColumn(long endColumn) {
        this.endColumn = endColumn;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
