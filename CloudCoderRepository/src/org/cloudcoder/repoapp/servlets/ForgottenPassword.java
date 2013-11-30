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

package org.cloudcoder.repoapp.servlets;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.ConvertBytesToHex;
import org.cloudcoder.app.shared.model.PasswordResetRequest;
import org.cloudcoder.app.shared.model.SHA1;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet allowing the user to request a password reset.
 * 
 * @author David Hovemeyer
 */
public class ForgottenPassword extends EmailServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ForgottenPassword.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("/_view/forgottenPassword.jsp").forward(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String emailAddress = req.getParameter("emailAddress");
		if (emailAddress == null || emailAddress.trim().equals("")) {
			req.setAttribute("error", "Please enter your email address");
		} else {
			// Find user account
			User user = Database.getInstance().findUserForEmailAddress(emailAddress);
			
			// Note: we don't produce any diagnostic if the user isn't found
			// (because there is no email address)
			if (user != null) {
				// Generate a secret.
				// We use both a random string and information from the retrieved user
				// to generate the hash.  (Using just a random string would raise to
				// possibility of duplicate secrets if two requests are created at
				// the same time.)
				Random random = new Random();
				SHA1 computeHash = new SHA1();
				computeHash.digest(String.valueOf(random.nextLong()).getBytes("UTF-8"));
				computeHash.digest(user.getUsername().getBytes("UTF-8"));
				computeHash.digest(user.getEmail().getBytes("UTF-8"));
				String secret = new ConvertBytesToHex(computeHash.digest()).convert();
				
				// Add a PasswordResetRequest to the database
				PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
				passwordResetRequest.initFromUserAndSecret(user, secret);
				Database.getInstance().insertModelObject(passwordResetRequest);
				
				// TODO: send password reset email
				logger.info("Sending password reset email to {}", passwordResetRequest.getEmail());
			}
			
			req.setAttribute("message", "Password reset email sent.");
		}

		req.getRequestDispatcher("/_view/forgottenPassword.jsp").forward(req, resp);
	}
}
