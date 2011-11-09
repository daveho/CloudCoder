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

import com.google.gwt.user.client.rpc.IsSerializable;

public class TestResult implements Serializable, IsSerializable
{
    public static final long serialVersionUID=1L;
    
    //TODO: Replace with enum?  
    //TODO: Add 6 separate methods for each possible outcome
    public static final String PASSED="passed";
    public static final String FAILED_ASSERTION="failed";
    public static final String FAILED_WITH_EXCEPTION="runtime_exception";
    public static final String FAILED_BY_SECURITY_MANAGER="security_exception";
    public static final String FAILED_FROM_TIMEOUT="timeout";
    public static final String INTERNAL_ERROR="interal_error";
    
    private String outcome;
    private String message;
    private String stdout;
    private String stderr;
    
    public TestResult(String outcome, String message) {
        this.outcome=outcome;
        this.message=message;
    }
    
    public TestResult(String outcome, 
            String message, 
            String stdout, 
            String stderr)
    {
        this(outcome,message);
        this.stdout=stdout;
        this.stderr=stderr;
    }
    
    public TestResult() {}

    public String toString() {
        return message;
    }

    public static TestResult[] error(String msg) {
        return new TestResult[] {new TestResult(INTERNAL_ERROR, msg)};
    }
    /**
     * @return the success
     */
    public String getOutcome() {
        return outcome;
    }
    /**
     * @param success the success to set
     */
    public void setOutcome(String outcome) {
        this.outcome= outcome;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return the stdout
     */
    public String getStdout() {
        return stdout;
    }
    /**
     * @param stdout the stdout to set
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
    /**
     * @return the stderr
     */
    public String getStderr() {
        return stderr;
    }
    /**
     * @param stderr the stderr to set
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
}
