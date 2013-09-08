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

package org.cloudcoder.builderwebservice;

import org.cloudcoder.daemon.DaemonController;
import org.cloudcoder.daemon.IDaemon;

/**
 * {@link DaemonController} implementation for {@link CloudCoderBuilderWebServiceDaemon}.
 * Contains the main entry point for the deployable jarfile.
 * 
 * @author David Hovemeyer
 */
public class CloudCoderBuilderWebServiceDaemonController extends DaemonController {
	@Override
	public String getDefaultInstanceName() {
		return "instance";
	}

	@Override
	public Class<? extends IDaemon> getDaemonClass() {
		return CloudCoderBuilderWebServiceDaemon.class;
	}
	
	@Override
	protected Options createOptions() {
		return new Options() {
			@Override
			public String getStdoutLogFileName() {
				return "logs/stdout.log";
			}
		};
	}

	public static void main(String[] args) {
		new CloudCoderBuilderWebServiceDaemonController().exec(args);
	}
}
