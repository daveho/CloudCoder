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

import java.util.List;

public class SubmissionResult
{
    private List<TestResult> testResults;
    private CompilationResult compilationResult;
    //Can add in other stuff like static error warnings
    
    public SubmissionResult(CompilationResult compilationResult) {
        this.compilationResult=compilationResult;
    }
    
    /**
     * @param compileResult
     */
    public void setCompilationResult(CompilationResult compileResult) {
        this.compilationResult=compileResult;
    }

    /**
     * @return the compilationResult
     */
    public CompilationResult getCompilationResult() {
        return compilationResult;
    }

    /**
     * @param outcomes
     */
    public void setTestResults(List<TestResult> outcomes) {
        this.testResults=outcomes;
    }

    /**
     * @return
     */
    public List<TestResult> getTestResults() {
        return testResults;
    }

    /**
     * @return
     */
    public boolean isCompiled() {
        return compilationResult.getOutcome()==CompilationOutcome.SUCCESS;
    }
}
