// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.rpc;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.RunService;
import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * RPC servlet to handle "playground" submissions.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class RunServiceImpl extends RemoteServiceServlet implements RunService
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger=LoggerFactory.getLogger(RunServiceImpl.class);
    
    @Override
    public void run(Problem problem, String programText, TestCase[] testCases) throws CloudCoderAuthenticationException, SubmissionException
    {
        // Make sure that client is authenticated and has permission to edit the given problem
        User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), RunServiceImpl.class);

        HttpSession session = getThreadLocalRequest().getSession();

        //TODO Don't insert into the DB yet, until we have some kind of a virtual file system
        // (or something like it)
//        Change fullTextChange = new Change(
//                ChangeType.FULL_TEXT,
//                0, 0, 0, 0,
//                System.currentTimeMillis(),
//                user.getId(), problem.getProblemId(),
//                programText);
//        Database.getInstance().storeChanges(new Change[]{fullTextChange});
        
        ISubmitService submitService = DefaultSubmitService.getInstance();

        logger.info("Passing submission to submit service...");
        
        // Convert TestCase[] to List<TestCase> to match ISubmitService
        List<TestCase> listTestCases=new LinkedList<TestCase>();
        for (TestCase tc : testCases) {
            listTestCases.add(tc);
        }
        
        IFutureSubmissionResult future = submitService.submitAsync(problem, listTestCases, programText);

        // put the future into the session
        session.setAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY, future);
        
        // Put the full-text Change and IFutureSubmissionResult in the user's session.
        //addSessionObjects(session, fullTextChange, future);
    }

    @Override
    public SubmissionResult checkSubmission() throws CloudCoderAuthenticationException, SubmissionException
    {
     // Make sure user is authenticated
        User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest(), RunServiceImpl.class);

        HttpSession session = getThreadLocalRequest().getSession();
        
        // Retrieve session objects for submission
        IFutureSubmissionResult future =
                (IFutureSubmissionResult) session.getAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY);
        
        if (future == null) {
            throw new SubmissionException("No pending submission in session");
        }
        
        // See if the SubmissionResult is ready
        SubmissionResult result;
        try {
            result = future.waitFor(IFutureSubmissionResult.STANDARD_POLL_WAIT_MS);
        } catch (SubmissionException e) {
            // If poll() throws an exception, the submission completed
            // with an error, but it did complete, so clear the session objects.
            session.removeAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY);
            throw e;
        } catch (InterruptedException e) {
			logger.error("checkSubmission interrupted unexpectedly", e);
			return null;
		}
        if (result == null) {
            // submission result not ready yet
            return null;
        }
        
        // Re-number the test results
        // The Builder thinks it is returning database keys
        // which it doesn't know so it sets everything to -1
        int i=1;
        for (TestResult r : result.getTestResults()) {
            r.setId(i);
            i++;
        }
        
        // We are just trusting that the submission result is for the
        // correct problem...

        // TODO Put results into DB once we have a meta-file system set up
//        SubmissionReceipt receipt = createSubmissionReceipt(fullTextChange, result, user, problem);
//        Database.getInstance().insertSubmissionReceipt(receipt, result.getTestResults());
//        
//        int numResult=0;
//        if (result!=null && result.getTestResults()!=null) {
//            numResult=result.getTestResults().length;
//        }
//        logger.info("Compilation "+result.getCompilationResult()+", received " +numResult+" TestResults");
//        
//        // Clear session objects for submission
//        session.removeAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY);        
        return result;
    }

}
