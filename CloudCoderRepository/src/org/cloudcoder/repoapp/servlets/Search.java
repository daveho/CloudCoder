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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Servlet to allow the user to search the exercise repository
 * for exercises matching specified criteria. 
 * 
 * @author David Hovemeyer
 */
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Javascript array mapping problem type ordinals to programming languages
	private static String PROBLEM_TYPE_ORDINAL_TO_PROGRAMMING_LANGUAGE_MAP;
	static {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (ProblemType problemType : ProblemType.values()) {
			buf.append("'");
			buf.append(problemType.getLanguage().getName());
			buf.append("'");
			buf.append(",");
		}
		buf.append("'']");
		PROBLEM_TYPE_ORDINAL_TO_PROGRAMMING_LANGUAGE_MAP = buf.toString();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("problemTypes", ProblemType.values());
		
		// Thank you, Sun, for using the method "ordinal()" rather than
		// "getOrdinal()" to get the ordinal value of an enumeration constant.
		// Saving three characters of typing far outweighs the utter violation
		// of standard bean property accessor naming.
		Map<ProblemType, Integer> problemTypeOrdinals = new HashMap<ProblemType, Integer>();
		for (ProblemType problemType : ProblemType.values()) {
			problemTypeOrdinals.put(problemType, problemType.ordinal());
		}
		req.setAttribute("problemTypeOrdinals", problemTypeOrdinals);

		// Javascript array mapping problem type ordinals to programming languages
		req.setAttribute("problemTypeOrdinalToLanguage", PROBLEM_TYPE_ORDINAL_TO_PROGRAMMING_LANGUAGE_MAP);
		
		req.getRequestDispatcher("_view/search.jsp").forward(req, resp);
	}
	
	// Note: POST requests are assumed to come from the search page via AJAX.
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		ProblemType problemType = null;
		if (req.getParameter("problemType") != null) {
			Integer problemTypeOrdinal = Integer.parseInt(req.getParameter("problemType"));
			ProblemType[] values = ProblemType.values();
			if (problemTypeOrdinal >= 0 && problemTypeOrdinal < values.length) {
				problemType = values[problemTypeOrdinal];
				System.out.println("Search for " + problemType + " exercises");
			}
		}
		
		List<RepoProblem> resultList = Database.getInstance().searchRepositoryExercises(problemType);
		System.out.println("Found " + resultList.size() + " matching exercises");
		JSONArray result = new JSONArray();
		for (RepoProblem repoProblem : resultList) {
			result.add(JSONConversion.convertModelObjectToJSON(repoProblem, IProblemData.SCHEMA));
		}
		
		resp.setContentType("application/json");
		JSONValue.writeJSONString(result, resp.getWriter());
	}
}
