// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.login;

import javax.servlet.http.HttpServletRequest;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.User;

/**
 * Implementation of {@link ILoginProvider} that compares the username and
 * password against the user account data stored in the database.
 * 
 * @author David Hovemeyer
 */
public class DatabaseLoginProvider extends AbstractLoginProvider {

	@Override
	public User login(String username, String password, HttpServletRequest request) {
		User user = Database.getInstance().authenticateUser(username, password);
		return user;
	}

}
