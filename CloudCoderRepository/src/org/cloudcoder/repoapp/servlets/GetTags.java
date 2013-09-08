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
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.webservice.util.ServletUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Servlet to retrieve the most popular tags for a {@link RepoProblem}
 * via an AJAX GET request.  The result is a serialized JSON array of
 * {@link RepoProblemTag} objects, where an extra <code>count</code>
 * field has been added to each object.  The count indicates the number
 * of users who tagged the problem.
 * 
 * @author David Hovemeyer
 */
public class GetTags extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String repoProblemIdStr = req.getParameter("repoProblemId");
		if (repoProblemIdStr == null) {
			ServletUtil.badRequest(resp, "Missing repo problem id");
			return;
		}
		
		Integer repoProblemId = Integer.parseInt(repoProblemIdStr);
		List<RepoProblemTag> tags = Database.getInstance().getProblemTags(repoProblemId);
		
		JSONArray result = new JSONArray();
		for (RepoProblemTag tag : tags) {
			LinkedHashMap<String, Object> obj = JSONConversion.convertModelObjectToJSON(tag, RepoProblemTag.SCHEMA);
			// Set the count field
			obj.put("count", tag.getCount());
			result.add(obj);
		}
		
		resp.setContentType("application/json");
		resp.getWriter().write(JSONValue.toJSONString(result));
	}
}
