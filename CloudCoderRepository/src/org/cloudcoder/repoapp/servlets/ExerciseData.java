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

package org.cloudcoder.repoapp.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoTestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;
import org.cloudcoder.webservice.util.AuthenticationException;
import org.cloudcoder.webservice.util.Credentials;
import org.cloudcoder.webservice.util.ServletUtil;

/**
 * Servlet to import/export exercises (problem and its test cases) as JSON.
 * Supports the import and export features in the main webapp.
 * GET requests with an SHA-1 hash as the pathinfo export an exercise.
 * POST requests with basic authentication import an exercise. 
 * 
 * @author David Hovemeyer
 */
public class ExerciseData extends LoadExerciseServlet {
	private static final String AUTH_REALM = "Exercise Repository";
	private static final long serialVersionUID = 1L;

	@Override
	protected void doExercise(HttpServletRequest req, HttpServletResponse resp, RepoProblemAndTestCaseList exercise) throws IOException {
		// Write the exercise as JSON
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		JSONConversion.writeProblemAndTestCaseData(exercise, resp.getWriter());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && !pathInfo.equals("/")) {
			ServletUtil.badRequest(resp, "Invalid URI for POST");
			return;
		}

		// Authenticate the user
		Credentials credentials;
		try {
			credentials = ServletUtil.getBasicAuthenticationCredentials(req);
		} catch (AuthenticationException e) {
			ServletUtil.authorizationRequired(resp, e.getMessage(), AUTH_REALM);
			return;
		}
		
		User user = Database.getInstance().authenticateUser(credentials.getUsername(), credentials.getPassword());
		if (user == null) {
			ServletUtil.authorizationRequired(resp, "Unknown username/password", AUTH_REALM);
			return;
		}
		
		// Read an exercise from the message body
		RepoProblemAndTestCaseList exercise = new RepoProblemAndTestCaseList();
		JSONConversion.readProblemAndTestCaseData(
				exercise,
				ReflectionFactory.forClass(RepoProblem.class),
				ReflectionFactory.forClass(RepoTestCase.class),
				req.getReader());

		// Store in database
		Database.getInstance().storeRepoProblemAndTestCaseList(exercise, user);
	}
}
