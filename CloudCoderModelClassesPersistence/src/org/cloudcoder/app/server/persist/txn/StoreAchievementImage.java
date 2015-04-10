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
 * Transaction to store an {@link AchievementImage} in the database.
 * 
 * @author David Hovemeyer
 */
public class StoreAchievementImage extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private AchievementImage achievementImage;

	public StoreAchievementImage(AchievementImage achievementImage) {
		this.achievementImage = achievementImage;
	}

	@Override
	public String getDescription() {
		return " store achievement image";
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		DBUtil.storeModelObject(conn, achievementImage);
		return true;
	}
}
