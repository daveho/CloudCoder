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

/**
 * @author jaimespacco
 *
 */
public class CompilationResult implements Serializable
{
    private static final long serialVersionUID=1L;
    
    private CompilationOutcome outcome;
    private CompilerDiagnostic[] diagnosticList;
    
    public CompilationResult() {
    	diagnosticList = new CompilerDiagnostic[0];
    }
    
    public String toString() {
        StringBuilder buf=new StringBuilder();
        if (diagnosticList!=null) {
            for (CompilerDiagnostic d : diagnosticList) {
                buf.append(d.toString());
            }
        }
        return buf.toString();
    }
    
    /**
     * @param outcome
     */
    public CompilationResult(CompilationOutcome outcome) {
        this.outcome=outcome;
        this.diagnosticList = new CompilerDiagnostic[0];
    }
    
    public void setCompilerDiagnosticList(CompilerDiagnostic[] diagnostics) {
        this.diagnosticList=diagnostics;
    }
    
    public CompilationOutcome getOutcome() {
        return this.outcome;
    }

    /**
     * @param outcome the outcome to set
     */
    public void setOutcome(CompilationOutcome outcome) {
        this.outcome = outcome;
    }

    /**
     * @return
     */
    public CompilerDiagnostic[] getCompilerDiagnosticList() {
        return diagnosticList;
    }

    /**
     * @param prologueLength
     * @param epilogueLength
     */
    public void adjustDiagnosticLineNumbers(int prologueLength, int epilogueLength) {
        for (CompilerDiagnostic d : diagnosticList) {
            d.adjustDiagnosticLineNumbers(prologueLength, epilogueLength);
        }
    }
}
