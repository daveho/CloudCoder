package org.cloudcoder.builder2.javasandbox;

import java.util.List;
import java.util.Map;

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

import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.pythonfunction.PythonKillableTaskManager;

/**
 * Utility methods for working with sandboxed Java code.
 * 
 * @author Jaime Spacco
 */
public abstract class SandboxUtil {
	/**
	 * Annotate all {@link TestResult}s produced by given {@link JVMKillableTaskManager}
	 * with stdout/stderr text.
	 * 
	 * @param pool the {@link JVMKillableTaskManager}
	 * @return list of {@link TestResult}s
	 */
	public static List<TestResult> getStdoutStderr(AbstractKillableTaskManager<TestResult> pool) {
		List<TestResult> outcomes=pool.getOutcomes();
		Map<Integer,String> stdout=pool.getBufferedStdout();
		Map<Integer,String> stderr=pool.getBufferedStderr();
		for (int i=0; i<outcomes.size(); i++) {
			TestResult t=outcomes.get(i);
			if (t!=null) {
				t.setStdout(stdout.get(i));
				t.setStderr(stderr.get(i));
			}
		}
		return outcomes;
	}
}
