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

package org.cloudcoder.app.server.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Servlet to retrieve information about problem submissions.
 * 
 * @author David Hovemeyer
 */
public class Problems extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Problems.class);

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
//		resp.setContentType("text/plain");
//		resp.getWriter().println("Hey there");
//		resp.setStatus(HttpServletResponse.SC_OK);
		
		ProblemURLInfo problemURLInfo = ProblemURLInfo.fromRequest(req);
		if (problemURLInfo == null) {
			// Should not happen
			logger.error("Could not extract problem info from request pathInfo {}", req.getPathInfo());
			AdminServletUtil.badRequest(resp);
			return;
		}
		
		User user = (User) req.getAttribute(RequestAttributeKeys.USER_KEY);
		Course course = (Course) req.getAttribute(RequestAttributeKeys.COURSE_KEY);
		
		if (problemURLInfo.getProblemId() < 0) {
			summarizeProblems(user, course, resp);
		}
	}

	private void summarizeProblems(User user, Course course, HttpServletResponse resp) throws IOException {
		// Just summarize problems available in this course
		
		resp.setContentType("text/xml");
		
		ProblemList problemList = Database.getInstance().getProblemsInCourse(user, course);
		XStream xstream = new XStream(new StaxDriver());
		xstream.processAnnotations(ProblemList.class);
		xstream.toXML(problemList, resp.getWriter());
	}
}
