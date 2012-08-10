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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

}
