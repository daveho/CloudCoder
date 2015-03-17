package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserAchievementAndAchievement;

public class GetUserAchievements extends AbstractDatabaseRunnableNoAuthException<UserAchievementAndAchievement[]> {

	private User user;
	private Course course;

	public GetUserAchievements(User user, Course course) {
		this.user = user;
		this.course = course;
	}

	@Override
	public String getDescription() {
		return " get user achievements";
	}

	@Override
	public UserAchievementAndAchievement[] run(Connection conn) throws SQLException {
		// TODO: implement this with an actual database query!
		// Should be a select that joins on the cc_user_achievements and cc_achievements tables
		
		//return result.toArray(new UserAchievementAndAchievement[result.size()]);
		return Queries.doGetAchievementsForUser(user, course, conn, this);
	}

}
