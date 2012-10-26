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

import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;

/**
 * Servlet to display an exercise in human-readable form.
 * 
 * @author David Hovemeyer
 */
public class Exercise extends LoadExerciseServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doExercise(HttpServletRequest req, HttpServletResponse resp, RepoProblemAndTestCaseList exercise)
			throws ServletException, IOException {
		
		ServletUtil.addModelObject(req, exercise.getProblem());
		
		req.getRequestDispatcher("/_view/exercise.jsp").forward(req, resp);
	}
}
