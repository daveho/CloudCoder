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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.UserAchievementAndAchievement;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * RPC service for achievements.
 * 
 * @author David Hovemeyer
 */
@RemoteServiceRelativePath("achievement")
public interface AchievementService extends RemoteService {
	/**
	 * Find all of the achievements the currently logged-in user
	 * has earned in the given {@link Course}.
	 *  
	 * @param course the {@link Course}
	 * @return array of {@link UserAchievementAndAchievement}s for the {@link User}
	 *         in the specified {@link Course}
	 */
	public UserAchievementAndAchievement[] getUserAchievements(Course course) throws CloudCoderAuthenticationException; 
}
