// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.dataanalysis;

import java.util.Properties;

import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;

/**
 * Implemented by classes which analyze snapshots and/or work sessions.
 * @author David Hovemeyer
 */
public interface IAnalyzeSnapshots {
	/**
	 * Set configuration properties.
	 * 
	 * @param config the configuration properties to set
	 */
	public void setConfig(Properties config);
	
	/**
	 * Set the {@link SnapshotSelectionCriteria}.
	 * 
	 * @param criteria the {@link SnapshotSelectionCriteria} to set
	 */
	public void setCriteria(SnapshotSelectionCriteria criteria);
}