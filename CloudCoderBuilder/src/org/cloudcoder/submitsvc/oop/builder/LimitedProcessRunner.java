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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProcessRunner implementation that sets process resource limits
 * appropriate for an untrusted test process.
 * 
 * @author David Hovemeyer
 */
public class LimitedProcessRunner extends ProcessRunner {
	
	/**
	 * Process limit types.
	 * 
	 * @author David Hovemeyer
	 */
	public enum LimitType {
		/**
		 * Maximum size of file child process is allowed to write.
		 */
		FILE_SIZE_KB("-f"),
		
		/**
		 * Maximum stack size.
		 */
		STACK_SIZE_KB("-s"),
		
		/**
		 * Maximum CPU time allowed.
		 */
		CPU_TIME_SEC("-t"),
		
		/**
		 * Maximum number of processes.
		 */
		PROCESSES("-u"),
		
		/**
		 * Maximum virtual memory.
		 */
		VM_SIZE_KB("-v"),
		;
		
		private String opt;
		
		private LimitType(String opt) {
			this.opt = opt;
		}
		
		/**
		 * @return the ulimit option name
		 */
		public String getOpt() {
			return opt;
		}
	}
	
	private Map<LimitType, Integer> limitMap;

	/**
	 * Constructor.
	 */
	public LimitedProcessRunner() {
		limitMap = new HashMap<LimitType, Integer>();
		
		// Set default limits
		
		//
		// Amazingly, -v16384 (allocating 16MB of virtual memory) is
		// not sufficient to allow a g++-compiled executable to run on Ubuntu 12.04.
		// 32MB ought to be plenty.  (Of course, that's what I thought about 16MB.)
		//
		
		limitMap.put(LimitType.FILE_SIZE_KB, 0);
		limitMap.put(LimitType.STACK_SIZE_KB, 256);
		limitMap.put(LimitType.CPU_TIME_SEC, 5);
		limitMap.put(LimitType.PROCESSES, 0);
		limitMap.put(LimitType.VM_SIZE_KB, 32768);
	}
	
	/**
	 * Get current limit for given {@link LimitType}.
	 * 
	 * @param limitType the {@link LimitType}
	 * @return the value of the limit, or 0 if no limit is set
	 */
	public int getLimit(LimitType limitType) {
		if (!limitMap.containsKey(limitType)) {
			return 0;
		}
		return limitMap.get(limitType);
	}

	/**
	 * Set a limit.
	 * 
	 * @param limitType the {@link LimitType} to set
	 * @param value     the limit value
	 */
	public void setLimit(LimitType limitType, int value) {
		limitMap.put(limitType, value);
	}
	
	/**
	 * Clear all currently-set limits.
	 */
	public void clearLimits() {
		limitMap.clear();
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
		for (Map.Entry<LimitType, Integer> entry : limitMap.entrySet()) {
			buf.append(entry.getKey().getOpt());
			buf.append(String.valueOf(entry.getValue()));
			buf.append(" ");
		}
		buf.setLength(buf.length() - 1);
		
		String limits = buf.toString();
		//System.out.println("Limits: " + limits);
		allEnvVars.add(limits);
		
		return allEnvVars.toArray(new String[allEnvVars.size()]);
	}
}
