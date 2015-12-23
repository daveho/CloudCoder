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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for bulk student registration.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class RegisterStudents extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(RegisterStudents.class);

	/**
	 * Handle POST requests with uploaded bulk student registration data.
	 * If the registrations are successful,
	 * the body of the response will be a message indicating
	 * how many students were registered.
	 * If the registrations are unsuccessful, or if there is an error
	 * processing the data, the body of the response will be
	 * a message beginning with "Error: ".
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();

			//Create a temporary file in a platform-independent manner
			File tempFile = File.createTempFile("registerstudentsupload", ".tmp");
			tempFile.delete();
			tempFile.mkdirs();
			tempFile.deleteOnExit();

			DiskFileItemFactory factory=new DiskFileItemFactory(10*1024*1024, tempFile);
			ServletFileUpload upload = new ServletFileUpload(factory);

			int courseId=-1;
			InputStream in=null;
			int num=0;

			@SuppressWarnings("unchecked")
			List<FileItem> items= (List<FileItem>) upload.parseRequest(request);

			logger.info("num file items: "+items.size());
			for (FileItem item : items) {
				if (item.isFormField() && item.getFieldName().equals("courseId")) {
					courseId=Integer.parseInt(item.getString());
				} else {
					// Assume that this is the actual data
					in=item.getInputStream();
				}
			}
			if (courseId==-1 || in==null) {
				logger.error("courseId "+courseId+", file input stream is null? "+(in==null));
				//throw new ServletException("courseId "+courseId);
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Data received by server is invalid");
				return;
			}
			num=ConfigurationUtil.registerStudentsForCourseId(in,
					courseId, 
					conn);
			sendResponse(response, HttpServletResponse.SC_OK, "Registered "+num+" students");
		} catch (SQLException e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
		} catch (FileUploadException e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading data: " + e.getMessage());
		} finally {
			DBUtil.closeQuietly(conn);
		}
	}

	private void sendResponse(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("text/plain");
		PrintStream out = new PrintStream(response.getOutputStream());
		if (status != HttpServletResponse.SC_OK) {
			// If the client is doing an AJAX file upload, the message
			// body is the only means we have of signaling an error.
			out.print("Error: ");
		}
		out.println(message);
		out.flush();
		out.close();
	}
}
