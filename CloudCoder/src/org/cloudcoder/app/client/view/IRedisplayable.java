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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.model.Session;

/**
 * Interface to be implemented by widgets that need
 * to be "kicked" to redisplay there contents.
 * This helps us work around issues with updates to
 * DataGrids happening while a tab is not visible
 * not displaying when the tab is made visible again.
 * 
 * @author David Hovemeyer
 */
public interface IRedisplayable {
	/**
	 * Force a redisplay from contents of the {@link Session}.
	 * This is to help work around a bug where a view
	 * (particularly, one based on a DataGrid) doesn't
	 * get updated properly if the course selection changes
	 * while the tab in which the view is displayed is
	 * not the current tab.
	 */
	void redisplay();
}
