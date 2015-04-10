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

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.server.rpc.SessionAttributeKeys;
import org.cloudcoder.app.shared.model.AchievementImage;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.User;

/**
 * Servlet to store uploaded achievement images in the
 * database.
 * 
 * @author David Hovemeyer
 */
public class UploadAchievementImage extends UploadAction {
	private static final long serialVersionUID = 1L;

	@Override
	public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
		// Make sure user is logged in
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute(SessionAttributeKeys.USER_KEY);
		if (user == null) {
			// User is not logged in
			throw new UploadActionException("Upload failed: user is not logged in");
		}
		
		IDatabase db = Database.getInstance();
		
		// Make sure that the user is an instructor in at least one
		// course registration.
		List<? extends Object[]> regs = db.getCoursesForUser(user);
		boolean isInstructor = false;
		for (Object[] triple : regs) {
			CourseRegistration reg = (CourseRegistration) triple[2];
			if (reg.getRegistrationType().isInstructor()) {
				isInstructor = true;
			}
		}
		if (!isInstructor) {
			throw new UploadActionException("Only instructors can upload image files");
		}
		
		// Make sure image(s) do not exceed the maximum size
		for (FileItem item : sessionFiles) {
			if (item.getSize() > AchievementImage.IMAGEARR.getSize()) {
				throw new UploadActionException("File is too large");
			}
		}
		
		try {
			// Add the image(s) to the database.
			for (FileItem item : sessionFiles) {
				AchievementImage achievementImage = createAchievementImage(item);
				db.storeAchievementImage(achievementImage);
			}
		} finally {
			// Clean up any temporary files
			removeSessionFileItems(request);
		}
		
		return "Upload successful";
	}

	private AchievementImage createAchievementImage(FileItem item) {
		try {
			InputStream in = item.getInputStream();
			try {
				AchievementImage achievementImage = new AchievementImage();
				achievementImage.setImageArr(IOUtils.toByteArray(in));
				return achievementImage;
			} finally {
				IOUtils.closeQuietly(in);
			}
		} catch (IOException e) {
			throw new UploadActionException("Error reading file", e);
		}
	}
}
