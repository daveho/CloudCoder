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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.AchievementImage;

/**
 * Database transaction to find an {@link AchievementImage} given its
 * database id.
 * 
 * @author David Hovemeyer
 */
public class FindAchievementImage extends AbstractDatabaseRunnableNoAuthException<AchievementImage> {
	private int achievementImageId;

	public FindAchievementImage(int achievementImageId) {
		this.achievementImageId = achievementImageId;
	}

	@Override
	public String getDescription() {
		return "find achievement image";
	}

	@Override
	public AchievementImage run(Connection conn) throws SQLException {
		AchievementImage img = new AchievementImage();
		img.setId(achievementImageId);
		try {
			DBUtil.loadModelObject(conn, img);
			return img;
		} catch (SQLException e) {
			return null;
		}
	}
}
