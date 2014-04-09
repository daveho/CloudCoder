// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import org.cloudcoder.app.server.model.HealthDataSingleton;
import org.cloudcoder.app.shared.model.HealthData;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.json.simple.JSONValue;

/**
 * Servlet to export health data about the CloudCoder webapp.
 * 
 * @author David Hovemeyer
 */
public class Health extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HealthData healthData = HealthDataSingleton.getInstance().getHealthData();
		
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		Object jsonValue = JSONConversion.convertModelObjectToJSON(healthData, healthData.getSchema());
		JSONValue.writeJSONString(jsonValue, resp.getWriter());
	}
}
