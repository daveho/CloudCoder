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
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.cloudcoder.app.shared.model.json.JSONUtil.*;
import org.cloudcoder.webservice.util.AuthenticationException;
import org.cloudcoder.webservice.util.Credentials;
import org.cloudcoder.webservice.util.ServletUtil;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to accept submissions and deliver submission results
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
		ServletUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Hey there!");
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
			Map<?, ?> requestObj = expectObject(requestObj_);
			
			// The Data field should contain the code execution request
			Map<?, ?> data = expectObject(requiredField(requestObj, "Data"));
			
			// Extract field values
			String language = expect(String.class, requiredField(data, "Language"));
			Integer executionType = expectInteger(requiredField(data, "ExecutionType"));
			JSONArray codeArray = expect(JSONArray.class, requiredField(data, "Code"));
			Integer testcaseType = expectInteger(requiredField(data, "TestcaseType"));
			Boolean trace = expect(Boolean.class, requiredField(data, "Trace"));
			Boolean stdout = expect(Boolean.class, requiredField(data, "Stdout"));
			Boolean returnValue = expect(Boolean.class, requiredField(data, "ReturnValue"));
			JSONArray testcases = expect(JSONArray.class, requiredField(data, "Testcases"));
			
			// This is just for testing
			ServletUtil.sendResponse(resp, HttpServletResponse.SC_OK, "All right!");
			
		} catch (ParseException e) {
			ServletUtil.badRequest(resp, "Invalid JSON request object: " + e.getMessage());
			logger.warn("Exception parsing request", e);
		} catch (IllegalArgumentException e) {
			ServletUtil.badRequest(resp, e.getMessage());
			logger.warn("Exception interpreting request", e);
		} catch (AuthenticationException e) {
			ServletUtil.authorizationRequired(resp, e.getMessage(), "BuilderWebService");
		}
	}
}
