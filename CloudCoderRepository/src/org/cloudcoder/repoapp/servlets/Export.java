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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.xml.XMLConversion;

import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;

/**
 * Servlet to export a problem and its test cases as XML.
 * This servlet will be invoked by the webapp when importing a problem
 * from the repository.
 * 
 * @author David Hovemeyer
 */
public class Export extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			ServletUtil.badRequest(resp, "Missing hash");
			return;
		}
		
		// get rid of leading "/" from the hash
		String hash = pathInfo.substring(1);
		
		// Load the exercise
		RepoProblemAndTestCaseList exercise = Database.getInstance().getRepoProblemAndTestCaseList(hash);
		
		if (exercise == null) {
			ServletUtil.notFound(resp, "No exercise with hash " + hash);
			return;
		} else {
			// Write the exercise as XML
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/xml");
			
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			try {
				XMLStreamWriter writer = factory.createXMLStreamWriter(resp.getOutputStream());
				writer.writeStartDocument();
				XMLConversion.writeProblemAndTestCaseData(exercise, writer);
				writer.writeEndDocument();
			} catch (XMLStreamException e) {
				throw new ServletException("Couldn't write XML", e);
			}
		}
	}
}
