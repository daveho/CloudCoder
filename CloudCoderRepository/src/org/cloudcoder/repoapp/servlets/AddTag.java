package org.cloudcoder.repoapp.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.User;

public class AddTag extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
		
		Database.getInstance().addRepoProblemTag(repoProblemTag);
		
		// Success
		resp.setContentType("application/json");
		resp.getWriter().write("{\"messages\": \"Tag added successfully\"}");
	}
}
