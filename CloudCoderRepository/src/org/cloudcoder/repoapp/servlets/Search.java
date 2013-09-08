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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.webservice.util.ServletUtil;
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

	// Javascript hash mapping problem type ordinals to programming language names
	private static String PROBLEM_TYPE_TO_PROGRAMMING_LANGUAGE_MAP;
	static {
		StringBuilder buf = new StringBuilder();
		buf.append("{");
		int count = 0;
		for (ProblemType problemType : ProblemType.values()) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append("'");
			buf.append(problemType.ordinal());
			buf.append("':'");
			buf.append(problemType.getLanguage().getName());
			buf.append("'");
			count++;
		}
		buf.append("}");
		PROBLEM_TYPE_TO_PROGRAMMING_LANGUAGE_MAP = buf.toString();
	}
	
//	// Javascript array mapping problem type ordinals to problem type names
//	private static String PROBLEM_TYPE_ORDINAL_TO_PROBLEM_TYPE_MAP;
//	static {
//		StringBuilder buf = new StringBuilder();
//		
//		buf.append("[");
//		for (ProblemType problemType : ProblemType.values()) {
//			if (buf.length() > 1) {
//				buf.append(",");
//			}
//			buf.append("'");
//			buf.append(problemType.toString());
//			buf.append("'");
//		}
//		buf.append("]");
//		PROBLEM_TYPE_ORDINAL_TO_PROBLEM_TYPE_MAP = buf.toString();
//	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Javascript array mapping problem types to programming language names.
		// This is needed so the view can report the language for each
		// matched problem.
		req.setAttribute("problemTypeToLanguage", PROBLEM_TYPE_TO_PROGRAMMING_LANGUAGE_MAP);
		
		req.getRequestDispatcher("/_view/search.jsp").forward(req, resp);
	}
	
	// Note: POST requests are assumed to come from the search page via AJAX.
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		RepoProblemSearchCriteria searchCriteria = new RepoProblemSearchCriteria();
		
		// See if tags were specified
		String selectedTags = req.getParameter("selectedTags");
		if (selectedTags != null && !selectedTags.trim().equals("")) {
			String tags = selectedTags;
			
			// Split by whitespace
			for (String rawTag : tags.split("\\s")) {
				// Convert to lowercase and remove all non-alphanumeric characters
				String normalizedTag = ServletUtil.normalizeTag(rawTag);
				searchCriteria.addTag(normalizedTag);
				//System.out.println("Tag: " + normalizedTag);
			}
		}
		
		List<RepoProblemSearchResult> resultList = Database.getInstance().searchRepositoryExercises(searchCriteria);
		System.out.println("Found " + resultList.size() + " matching exercises");
		JSONArray result = new JSONArray();
		for (RepoProblemSearchResult searchResult : resultList) {
			result.add(JSONConversion.convertRepoProblemSearchResultToJSON(searchResult));
		}
		
		resp.setContentType("application/json");
		JSONValue.writeJSONString(result, resp.getWriter());
	}
}
