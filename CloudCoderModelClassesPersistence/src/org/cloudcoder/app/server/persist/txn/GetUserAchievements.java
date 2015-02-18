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
		ArrayList<UserAchievementAndAchievement> result = new ArrayList<UserAchievementAndAchievement>();

		// TODO: implement this with an actual database query!
		// Should be a select that joins on the cc_user_achievements and cc_achievements tables
		
		/*
		PreparedStatement stmt = prepareStatement(" TODO - SQL stuff ");
		
		stmt.setInt(something, user.getId());
		stmt.setInt(somethingElse, course.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		
		while (resultSet.next()) {
			UserAchievement u = new UserAchievement();
			int index = 1;
			index = DBUtil.loadModelObjectFields(u, u.getSchema(), resultSet, index);
			Achievement a = new Achievement();
			index = DBUtil.loadModelObjectFields(a, a.getSchema(), resultSet, index);
			
			UserAchievementAndAchievement uaa = new UserAchievementAndAchievement();
			uaa.setUserAchievement(u);
			uaa.setAchievement(a);
			result.add(uaa);
		}
		 */
		
		return result.toArray(new UserAchievementAndAchievement[result.size()]);
	}

}
