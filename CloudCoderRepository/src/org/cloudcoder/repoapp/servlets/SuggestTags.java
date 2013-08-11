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
import org.cloudcoder.webservice.util.ServletUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Servlet to return possible tags that the user could search for,
 * given a prefix string.  The suggested tags are returned as
 * a JSON array.  (The input and output are designed to work
 * with the jquery-ui autocomplete widget.)
 * 
 * @author David Hovemeyer
 */
public class SuggestTags extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String term = req.getParameter("term");
		if (term == null) {
			ServletUtil.badRequest(resp, "A search term is required");
			return;
		}
		
		List<String> tagNames = Database.getInstance().suggestTagNames(term);
		
		JSONArray result = new JSONArray();
		result.addAll(tagNames);
		
		resp.setContentType("application/json");
		JSONValue.writeJSONString(result, resp.getWriter());
	}
}
