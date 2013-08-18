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

package org.cloudcoder.webservice.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

/**
 * Utility methods for servlets.
 * 
 * @author David Hovemeyer
 */
public class ServletUtil {
	/**
	 * Remove given type of model object from the session.
	 * 
	 * @param session the session
	 * @param type    the type of model object to remove
	 */
	public static void removeModelObject(HttpSession session, Class<?> type) {
		session.removeAttribute(type.getSimpleName());
	}

	/**
	 * Add a model object to the session.
	 * 
	 * @param session the session
	 * @param obj     the model object to add to the session
	 */
	public static void addModelObject(HttpSession session, Object obj) {
		session.setAttribute(obj.getClass().getSimpleName(), obj);
	}

	/**
	 * Add a model object to the request.
	 * 
	 * @param req   the request
	 * @param obj   the model object to add to the session
	 */
	public static void addModelObject(HttpServletRequest req, Object obj) {
		req.setAttribute(obj.getClass().getSimpleName(), obj);
	}
	
	/**
	 * Get a model object from the session.
	 * 
	 * @param session the session
	 * @param type    the type of model object to get
	 * @return the model object, or null if there is no such model object in the session
	 */
	public static<E> E getModelObject(HttpSession session, Class<E> type) {
		return type.cast(session.getAttribute(type.getSimpleName()));
	}
	
	/**
	 * Get HTTP basic authentication credentials from an HttpServletRequest.
	 * 
	 * @param req the HttpServletRequest
	 * @return the credentials
	 * @throws AuthenticationException if there are no valid credentials in the request
	 */
	public static Credentials getBasicAuthenticationCredentials(HttpServletRequest req) throws AuthenticationException {
		String authHeader = req.getHeader("Authorization");
		if (authHeader == null) {
			throw new AuthenticationException("HTTP basic authentication is required");
		}
		
		if (!authHeader.startsWith("Basic ")) {
			throw new AuthenticationException("Invalid Authorization header");
		}
		authHeader = authHeader.substring("Basic ".length());
		
		// Decode
		byte[] authHeaderDecodedBytes;
		try {
			authHeaderDecodedBytes = DatatypeConverter.parseBase64Binary(authHeader);
		} catch (IllegalArgumentException e) {
			throw new AuthenticationException("Invalid authorization string (not valid base64)");
		}
		
		String authHeaderDecoded = new String(authHeaderDecodedBytes, Charset.forName("UTF-8"));
		//System.out.println("Decoded header: " + authHeaderDecoded);
		int colon = authHeaderDecoded.indexOf(':');
		if (colon < 0) {
			throw new AuthenticationException("Invalid authorization string (not in username:password format)");
		}
		
		String username = authHeaderDecoded.substring(0, colon);
		String password = authHeaderDecoded.substring(colon + 1);
		
		return new Credentials(username, password);
	}

	/**
	 * Send a redirect to another servlet or path in the webapp.
	 * 
	 * @param servletContext the ServletContext
	 * @param resp           the HttpServletResponse
	 * @param path           the path to redirect to (e.g., "/index")
	 * @throws IOException 
	 */
	public static void sendRedirect(ServletContext servletContext, HttpServletResponse resp, String path) throws IOException {
		resp.sendRedirect(servletContext.getContextPath() + path);
	}

	/**
	 * Send a BAD REQUEST (400) response.
	 * 
	 * @param resp  the HttpServletResponse
	 * @param msg   a human-readable message to send as the body of the response
	 * @throws IOException
	 */
	public static void badRequest(HttpServletResponse resp, String msg) throws IOException {
		sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, msg);
	}

	/**
	 * Send a NOT FOUND (404) response.
	 * 
	 * @param resp  the HttpServletResponse
	 * @param msg   a human-readable message to send as the body of the response
	 * @throws IOException
	 */
	public static void notFound(HttpServletResponse resp, String msg) throws IOException {
		sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, msg);
	}

	/**
	 * Send an UNAUTHORIZED (401) response.
	 * 
	 * @param resp  the HttpServletResponse
	 * @param msg   a human-readable message to send as the body of the response
	 * @param realm the authentication realm (e.g. "Secure area")
	 * @throws IOException
	 */
	public static void authorizationRequired(HttpServletResponse resp, String msg, String realm) throws IOException {
		resp.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, msg);
	}

	/**
	 * Send a response.
	 * 
	 * @param resp        the HttpServletResponse
	 * @param statusCode  the status code of the response
	 * @param msg         a human-readable message
	 * @throws IOException
	 */
	public static void sendResponse(HttpServletResponse resp, int statusCode, String msg) throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("text/plain");
		resp.getWriter().println(msg);
	}

	/**
	 * Get a required parameter.
	 * 
	 * @param req     the HttpServletRequest
	 * @param name    the parameter name
	 * @return the parameter value
	 * @throws ServletException if the parameter is not set
	 */
	public static String getRequiredParam(HttpServletRequest req, String name) throws ServletException {
		String value = req.getParameter(name);
		if (value == null) {
			throw new ServletException("Missing: required parameter: " + name);
		}
		return value;
	}
	
	private static final Pattern INVALID_TAG_CHARS = Pattern.compile("[^a-z0-9]");

	/**
	 * Normalize a raw tag by removing all non-alphanumeric characters.
	 * 
	 * @param rawTag a raw tag
	 * @return the normalized tag
	 */
	public static String normalizeTag(String rawTag) {
		Matcher m = INVALID_TAG_CHARS.matcher(rawTag);
		return m.replaceAll("");
	}

	/**
	 * Return just the path part of a URL, without the context path.
	 * The path can then be used to send a redirect.
	 * 
	 * @param req         the HttpServletRequest
	 * @param url         the URL
	 * @param defaultPath the path to return if the URL can't be parsed
	 * @return the path part of the URL
	 */
	public static String getUrlPath(HttpServletRequest req, String urlStr, String defaultPath) {
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			return defaultPath;
		}
		
		String path = url.getPath();
		if (path.startsWith(req.getContextPath())) {
			// The path had better start with the context path.
			path = path.substring(req.getContextPath().length());
			if (!path.startsWith("/")) {
				path = "/" + path; // paranoia
			}
		}
		
		return path;
	}

	/**
	 * Send an internal server error response.
	 * 
	 * @param resp     the HttpServletResponse
	 * @param message  the message
	 * @throws IOException
	 */
	public static void internalServerError(HttpServletResponse resp, String message) throws IOException {
		sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
	}

}
