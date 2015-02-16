// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.shared.model.AchievementImage;

/**
 * Servlet to display an {@link AchievementImage}.
 * 
 * @author David Hovemeyer
 */
public class DisplayAchievementImage extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String info = req.getPathInfo();
		if (info == null) {
			sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing achievement id");
			return;
		}
		if (info.startsWith("/")) {
			info = info.substring(1);
		}
		
		int achievementImageId;
		try {
			achievementImageId = Integer.parseInt(info);
		} catch (NumberFormatException e) {
			sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid achievement image id: " + info);
			return;
		}
		
		IDatabase db = Database.getInstance();
		AchievementImage img = db.findAchievementImage(achievementImageId);
		if (img == null) {
			sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "No such achievement image");
			return;
		}
		
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("image/png");
		resp.setContentLength(img.getImageArr().length);
		resp.getOutputStream().write(img.getImageArr());
	}

	private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
		resp.setStatus(status);
		resp.setContentType("text/plain");
		resp.getWriter().println(message);
	}
}
