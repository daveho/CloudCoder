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

/**
 * A Submission bundles up all of the information needed
 * to compile and test a submission of a (possible) solution to a particular
 * problem.
 * 
 * @author jaimespacco
 */
public class Submission
{
    private Problem problem;
    private List<TestCase> testCaseList;
    private String programText;
    
    public Submission(Problem problem, List<TestCase> testCaseList, String programText) {
        this.problem=problem;
        this.testCaseList=testCaseList;
        this.programText=programText;
    }

    /**
     * @return the problem
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * @return the testCaseList
     */
    public List<TestCase> getTestCaseList() {
        return testCaseList;
    }

    /**
     * @return the programText
     */
    public String getProgramText() {
        return programText;
    }

    /**
     * @param testCase
     */
    public void addTestCase(TestCase testCase) {
        testCaseList.add(testCase);
    }

    /**
     * @param programText2
     */
    public void setProgramText(String programText) {
        this.programText=programText;
    }
}
