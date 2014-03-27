// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, York College of Pennsylvania
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

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for implementations of the "dev actions panel",
 * which includes the Reset and Submit! buttons.
 * 
 * @author David Hovemeyer
 */
public interface IDevActionsPanel extends IsWidget {
	/**
	 * Set the handler to run when the Submit! button is clicked.
	 * 
	 * @param submitHandler handler to run when the Submit! button is clicked
	 */
	public void setSubmitHandler(Runnable submitHandler);

	/**
	 * Set the handler to run when the Reset button is clicked.
	 * 
	 * @param resetHandler handler to run when the Reset button is clicked
	 */
	public void setResetHandler(Runnable resetHandler);
}