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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.PersistenceException;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.UserRegistrationRequest;
import org.cloudcoder.app.shared.model.UserRegistrationRequestStatus;

/**
 * Servlet for confirming a registration request.
 * 
 * @author David Hovemeyer
 */
public class Confirm extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String secret = req.getPathInfo();
		if (secret.startsWith("/")) {
			secret = secret.substring(1);
		}
		
		UserRegistrationRequest request = Database.getInstance().findUserRegistrationRequest(secret);

		OperationResult result;
		if (request == null) {
			result = new OperationResult(false, "Sorry, this registration URL doesn't seem to be valid.");
		} else if (request.getStatus() != UserRegistrationRequestStatus.PENDING) {
			result = new OperationResult(false, "This registration has already been confirmed.");
		} else {
			try {
				// Attempt to confirm the registration by creating the user account
				// and changing the request status to CONFIRMED.
				result = Database.getInstance().completeRegistration(request);
			} catch (PersistenceException e) {
				Throwable cause = e.getCause();
				result = new OperationResult(false, cause != null ? cause.getMessage() : e.getMessage());
			}
		}
		req.setAttribute("confSuccess", result.isSuccess());
		req.setAttribute("confMessage", result.getMessage());
		
		req.getRequestDispatcher("/_view/confirm.jsp").forward(req, resp);
	}

}
