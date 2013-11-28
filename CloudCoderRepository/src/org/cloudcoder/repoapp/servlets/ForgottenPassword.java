package org.cloudcoder.repoapp.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForgottenPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
			// TODO: send password reset email
			
			req.setAttribute("message", "Password reset email sent.");
		}

		req.getRequestDispatcher("/_view/forgottenPassword.jsp").forward(req, resp);
	}
}
