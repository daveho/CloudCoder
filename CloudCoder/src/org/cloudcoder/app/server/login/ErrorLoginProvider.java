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

import org.cloudcoder.app.server.persist.PersistenceException;
import org.cloudcoder.app.shared.model.User;

/**
 * Implementation of {@link ILoginProvider} that always throws
 * an exception.  This provider is used only if the cloudcoder
 * configuration does not specify a valid provider type.
 * 
 * @author David Hovemeyer
 */
public class ErrorLoginProvider extends AbstractLoginProvider {

	@Override
	public User login(String username, String password, HttpServletRequest request) {
		throw new PersistenceException("No login provider is configured");
	}

}
