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

import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

public class CTester implements ITester
{

    @Override
    public List<TestResult> testSubmission(Problem problem,
            List<TestCase> testCaseList, String programText)
    {
        throw new IllegalStateException("C not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.submitsvc.oop.builder.ITester#testOneSubmission(org.cloudcoder.app.shared.model.Problem, org.cloudcoder.app.shared.model.TestCase, java.lang.String)
     */
    @Override
    public TestResult testOneSubmission(Problem problem, TestCase testCase,
            String programText) {
        // TODO Auto-generated method stub
        return null;
    }

}
