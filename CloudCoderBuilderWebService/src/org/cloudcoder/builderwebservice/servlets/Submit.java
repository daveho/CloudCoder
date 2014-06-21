// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builderwebservice.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.json.JSONUtil;
import org.cloudcoder.webservice.util.AuthenticationException;
import org.cloudcoder.webservice.util.BadRequestException;
import org.cloudcoder.webservice.util.Credentials;
import org.cloudcoder.webservice.util.ServletUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to accept submissions (POST) and deliver submission results (GET)
 * back to the client.
 * 
 * @author David Hovemeyer
 */
public class Submit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Submit.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String key = req.getPathInfo();
			if (key == null) {
				throw new BadRequestException("Must specify the unique key of the submission");
			}
			
			if (key.startsWith("/")) {
				key = key.substring(1);
			}
			
			IFutureSubmissionResult result = ActiveSubmissionMap.getInstance().get(key);
			if (result == null) {
				ServletUtil.notFound(resp, "No such unique key: " + key);
				return;
			}
			
			SubmissionResult submissionResult;
			try {
				submissionResult = result.waitFor(IFutureSubmissionResult.STANDARD_POLL_WAIT_MS);
			} catch (InterruptedException e) {
				throw new ServletException("Unexpectedly interrupted waiting for submission result", e);
			}
			
			Map<String, Object> resultObj = new HashMap<String, Object>();
			if (submissionResult == null) {
				// Submission is still pending
				resultObj.put("Status", "Pending");
			} else {
				// Submission is complete: encode the response and purge it from the ActiveSubmissionMap
				ResultBuilder resultBuilder = new ResultBuilder(submissionResult);
				resultObj.put("Status", "Complete");
				resultObj.put("Data", resultBuilder.build());
				ActiveSubmissionMap.getInstance().purge(key);
			}
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			resp.getWriter().println(JSONObject.toJSONString(resultObj));
			
		} catch (BadRequestException e) {
			logger.warn("Invalid submission request", e);
			ServletUtil.badRequest(resp, e.getMessage());
		} catch (SubmissionException e) {
			logger.error("Error polling for submission result", e);
			ServletUtil.internalServerError(resp, e.getMessage());
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletContext context = getServletContext();
		
		// FIXME: this is pretty cheesy - think about implementing a more flexible mechanism for authentication
		String expectedUsername = context.getInitParameter("cloudcoder.builderwebservice.clientusername");
		String expectedPassword = context.getInitParameter("cloudcoder.builderwebservice.clientpassword");
		
		try {
			// Get the Credentials
			Credentials credentials = ServletUtil.getBasicAuthenticationCredentials(req);
			
			// Check the credentials
			if (!(credentials.getUsername().equals(expectedUsername) && credentials.getPassword().equals(expectedPassword))) {
				throw new AuthenticationException("Invalid username/password");
			}
			
			// Read the request JSON object
			JSONParser parser = new JSONParser();
			Object requestObj_ = parser.parse(req.getReader());

			// Build the request
			RequestBuilder requestBuilder = new RequestBuilder(requestObj_);
			Request request = requestBuilder.build();
			logger.info("Successfully built request: testname={}", request.getTestname());
			
			// Build a Problem from the request
			ProblemBuilder problemBuilder = new ProblemBuilder(request);
			Problem problem = problemBuilder.build();
			
			// Build list of TestCases from the request
			List<TestCase> testCaseList = new ArrayList<TestCase>();
			JSONArray tcList = request.getTestcases();
			int count = 1;
			for (Object tc : tcList) {
				TestCaseBuilder testCaseBuilder = new TestCaseBuilder(tc, problem.getProblemType(), count++);
				TestCase testCase = testCaseBuilder.build();
				testCaseList.add(testCase);
			}
			
			// Extract the program text
			JSONArray code = request.getCodeArray();
			if (code.size() != 1) {
				throw new BadRequestException("Only single source file submissions are supported");
			}
			String programText = JSONUtil.expect(String.class, code.get(0));
			
			// Build a BuilderSubmission
			ISubmitService submitSvc = DefaultSubmitService.getInstance();
			IFutureSubmissionResult promise = submitSvc.submitAsync(problem, testCaseList, programText);
			
			// Add the submission result to the ActiveSubmissionMap
			String key = ActiveSubmissionMap.getInstance().add(promise);
			
			// Return the response JSON object containing the unique key that will
			// be used to identify this submission's (eventual) result
			Map<String, Object> resultObj = new HashMap<String, Object>();
			resultObj.put("Status", "Pending");
			resultObj.put("Key", key);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			resp.getWriter().println(JSONObject.toJSONString(resultObj));
			
		} catch (ParseException e) {
			ServletUtil.badRequest(resp, "Invalid JSON request object: " + e.getMessage());
			logger.warn("Exception parsing request", e);
		} catch (BadRequestException e) {
			ServletUtil.badRequest(resp, e.getMessage());
			logger.warn("Exception interpreting request", e);
		} catch (AuthenticationException e) {
			ServletUtil.authorizationRequired(resp, e.getMessage(), "BuilderWebService");
		} catch (SubmissionException e) {
			logger.error("Error handling submission", e);
			ServletUtil.internalServerError(resp, e.getMessage());
		}
	}
}
