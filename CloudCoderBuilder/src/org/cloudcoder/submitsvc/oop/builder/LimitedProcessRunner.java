// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.submitsvc.oop.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProcessRunner implementation that sets process resource limits
 * appropriate for an untrusted test process.
 * 
 * @author David Hovemeyer
 */
public class LimitedProcessRunner extends ProcessRunner {
	public LimitedProcessRunner() {
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.ProcessRunner#getEnvp(java.lang.String[])
	 */
	@Override
	protected String[] getEnvp(String... extraVars) {
		List<String> allEnvVars = new ArrayList<String>();
		allEnvVars.addAll(Arrays.asList(super.getEnvp(extraVars)));
		
		// In addition to the environment variables created by the superclass getEnvp(),
		// define the CC_PROCESS_RESOURCE_LIMITS environment variable used
		// by limitedRunProcess.sh to define resource limits for the created
		// process.
		
		StringBuilder buf = new StringBuilder();
		buf.append("CC_PROCESS_RESOURCE_LIMITS=");
		buf.append("-f0 -s256 -t5 -u0 -v16384"); // FIXME: make this configurable
		
		allEnvVars.add(buf.toString());
		
		return allEnvVars.toArray(new String[allEnvVars.size()]);
	}
}
