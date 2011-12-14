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

import javax.tools.JavaFileObject;

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

    public CompilerDiagnostic(int startLine, int endLine, int startCol, int endCol, String message) {
        this.startLine=startLine;
        this.endLine=endLine;
        this.startColumn=startCol;
        this.endColumn=endCol;
        this.message=message;
    }
    
    public CompilerDiagnostic(javax.tools.Diagnostic<? extends JavaFileObject> d) {
        this.startLine=d.getLineNumber();
        this.endLine=d.getLineNumber();
        this.startColumn=d.getColumnNumber();
        this.endColumn=d.getColumnNumber();
        this.message=d.getMessage(null);
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
}
