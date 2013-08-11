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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.webservice.util.ServletUtil;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to add a tag to a {@link RepoProblem} via an AJAX POST request.
 * An {@link OperationResult} serialized as a JSON object is returned
 * to indicate success or failure.
 * 
 * @author David Hovemeyer
 */
public class AddTag extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(AddTag.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		User user = ServletUtil.getModelObject(req.getSession(), User.class);
		if (user == null) {
			ServletUtil.badRequest(resp, "User must be logged in to add a tag to an exercise");
			return;
		}
		
		String tag = req.getParameter("tag");
		String repoProblemIdStr = req.getParameter("repoProblemId");
		if (tag == null || repoProblemIdStr == null) {
			ServletUtil.badRequest(resp, "Invalid parameters");
			return;
		}
		logger.info("Tagging repo problem {} with tag \"{}\"", repoProblemIdStr, tag);
		
		Integer repoProblemId = Integer.parseInt(repoProblemIdStr);
		tag = ServletUtil.normalizeTag(tag);
		if (tag.equals("")) {
			ServletUtil.badRequest(resp, "Invalid tag");
			return;
		}

		RepoProblemTag repoProblemTag = new RepoProblemTag();
		repoProblemTag.setName(tag);
		repoProblemTag.setRepoProblemId(repoProblemId);
		repoProblemTag.setUserId(user.getId());
		
		// Attempt to add the tag
		boolean success = Database.getInstance().addRepoProblemTag(repoProblemTag);
		
		// Report result as a serialized OperationResult
		resp.setContentType("application/json");
		OperationResult result = new OperationResult(success,
				success ? "Tag added successfully" : "You have already added that tag");
		Object obj = JSONConversion.convertOperationResultToJSON(result);
		resp.getWriter().write(JSONValue.toJSONString(obj));
	}
}
