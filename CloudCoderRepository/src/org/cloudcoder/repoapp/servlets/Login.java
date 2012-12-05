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
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login servlet.
 * 
 * @author David Hovemeyer
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Login.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Remove User object from session (if there is one).
		ServletUtil.removeModelObject(req.getSession(), User.class);
		
		// Add redirectPath request attribute: specifies what page should
		// be loaded on a successful login.
		// Redirect to the original (referring) page, or "/index"
		// if there was no referrer.
		String referrer = req.getHeader("Referer"); // yes, it really is spelled "Referer"
		String redirectPath = ServletUtil.getUrlPath(req, referrer, "/index");
		req.setAttribute("redirectPath", redirectPath);
		logger.info("Login: redirectPath={}", redirectPath);
		
		// Render the view (with the login form)
		req.getRequestDispatcher("/_view/login.jsp").forward(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		
		if (username != null && password != null) {
			// Attempt to log in
			User user = Database.getInstance().authenticateUser(username, password);
			if (user != null) {
				ServletUtil.addModelObject(req.getSession(), user);
				
				String redirectPath = req.getParameter("redirectPath");
				if (redirectPath == null) {
					// paranoia
					redirectPath = "/index";
				}
				logger.info("Redirecting to {}", redirectPath);
				
				ServletUtil.sendRedirect(getServletContext(), resp, redirectPath);
				return;
			}
			
			// Authentication failure
			req.setAttribute("username", username);
			req.setAttribute("redirectPath", req.getParameter("redirectPath"));
			req.setAttribute("error", "Username/password not found");
		}

		req.getRequestDispatcher("/_view/login.jsp").forward(req, resp);
	}
}
