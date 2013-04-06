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

package org.cloudcoder.builder2.javamethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IsolatedTask} for testing {@link ProblemType#JAVA_METHOD}
 * submissions.
 * 
 * @author Jaime Spacco
 */
public class IsolatedTaskRunner implements IsolatedTask<TestResult>
{
	private static final Logger logger = LoggerFactory.getLogger(IsolatedTaskRunner.class);
	
    private Class<?> theClass;
	private Problem problem;
    private TestCase testCase;
    
    public IsolatedTaskRunner(Class<?> theClass, Problem problem, TestCase testCase) {
        this.theClass=theClass;
        this.problem=problem;
        this.testCase=testCase;
    }

    
    
    @Override
    public TestResult execute() {
        try {
            Method m = theClass.getMethod(testCase.getTestCaseName());
            Object[] results=(Object[])m.invoke(null);
            Boolean passedTest=(Boolean)results[0];
            String output=(String)results[1];
            logger.trace("Hooked onto the outcome! "+output);
            
            if (passedTest) {
                return TestResultUtil.createResultForPassedTest(problem, testCase);
            } else {
            	    return TestResultUtil.createResultForFailedTest(problem, testCase, output);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SecurityException) {
                logger.error("Security exception", e);
                return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Security exception while testing submission");
            } 
            logger.warn("InvocationTargetException", e);
            logger.trace("cause: "+e.getCause());
            logger.trace("target exception: "+e.getTargetException());
            return TestResultUtil.createResultForFailedWithExceptionTest(problem, testCase, e);
        } catch (NoSuchMethodException e) {
            return new TestResult(TestOutcome.INTERNAL_ERROR, "Method not found while testing submission");
        } catch (IllegalAccessException e) {
            return new TestResult(TestOutcome.INTERNAL_ERROR, "Illegal access while testing submission");
        }
        //TODO: Catch Throwable and report INTERNAL_ERROR for anything else
    }
    
}
