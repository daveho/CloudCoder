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
import java.util.LinkedList;
import java.util.List;

/**
 * @author jaimespacco
 *
 */
public class CompilationResult implements Serializable
{
    private static final long serialVersionUID=1L;
    
    private CompilationOutcome outcome;
    private List<CompilerDiagnostic> diagnostics=new LinkedList<CompilerDiagnostic>();
    private Exception exception;
    
    /**
     * @param failure
     */
    public CompilationResult(CompilationOutcome outcome) {
        this.outcome=outcome;
    }
    
    public void setDiagnostics(List<CompilerDiagnostic> diagnostics) {
        this.diagnostics=diagnostics;
    }
    
    public CompilationOutcome getOutcome() {
        return this.outcome;
    }

    /**
     * @param compilerDiagnostic
     */
    public void addCompilerDiagnostic(CompilerDiagnostic compilerDiagnostic) {
        this.diagnostics.add(compilerDiagnostic);
    }

    /**
     * @param e
     */
    public void setException(Exception e) {
        this.exception=e;
    }

    /**
     * @return
     */
    public List<CompilerDiagnostic> getCompilerDiagnosticList() {
        return diagnostics;
    }

}
