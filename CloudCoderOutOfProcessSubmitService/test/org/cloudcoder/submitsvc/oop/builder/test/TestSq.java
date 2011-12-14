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

package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.TestOutcome;
import org.junit.Test;

/**
 * @author jaimespacco
 *
 */
public class TestSq extends GenericTest
{
    @Test
    public void test1() {
        runOneTestCase("test1", "1", "1", TestOutcome.FAILED_ASSERTION);
    }
    
    @Test
    public void test2() {
        runOneTestCase("test2", "2", "4", TestOutcome.FAILED_WITH_EXCEPTION);
    }
    
    @Test
    public void test3() {
        runOneTestCase("test3", "3", "9", TestOutcome.FAILED_FROM_TIMEOUT);
    }
    
    @Test
    public void test4() {
        runOneTestCase("test4", "4", "16", TestOutcome.FAILED_BY_SECURITY_MANAGER);
    }
    
    @Test
    public void test5() {
        runOneTestCase("test5", "5", "25", TestOutcome.PASSED);
    }
    
    @Test
    public void test6() {
        runOneTestCase("test6", "6", "36", TestOutcome.FAILED_BY_SECURITY_MANAGER);
    }
    
    @Test
    public void runAllTests() {
        addTestCaseAndOutcome("test1", "1", "1", TestOutcome.FAILED_ASSERTION);
        addTestCaseAndOutcome("test2", "2", "4", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCaseAndOutcome("test3", "3", "9", TestOutcome.FAILED_FROM_TIMEOUT);
        addTestCaseAndOutcome("test4", "4", "16", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        addTestCaseAndOutcome("test5", "5", "25", TestOutcome.PASSED);
        addTestCaseAndOutcome("test6", "6", "36", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        super.runAllTests();
    }
}
