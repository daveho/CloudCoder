package org.cloudcoder.repoapp.servlets;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public abstract class EmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private volatile Session session;

	public EmailServlet() {
		super();
	}
	
	public Session getSession() {
		return session;
	}

	@Override
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		if (this.session == null) {
			String smtpHost = context.getInitParameter("cloudcoder.repoapp.smtp.host");
			String smtpUsername = context.getInitParameter("cloudcoder.repoapp.smtp.user");
			String smtpPassword = context.getInitParameter("cloudcoder.repoapp.smtp.passwd");
			String smtpPort = context.getInitParameter("cloudcoder.repoapp.smtp.port");
			
			final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(smtpUsername, smtpPassword);
			Authenticator authenticator = new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return passwordAuthentication;
				}
			};
	
			Properties properties = new Properties();
			properties.putAll(System.getProperties());
			properties.setProperty("mail.smtp.submitter", passwordAuthentication.getUserName());
			properties.setProperty("mail.smtp.auth", "true");
			properties.setProperty("mail.password", smtpPassword);
			properties.setProperty("mail.smtp.host", smtpHost);
			properties.setProperty("mail.smtp.port", smtpPort);
	
			this.session = Session.getInstance(properties, authenticator);
		}
	}
}