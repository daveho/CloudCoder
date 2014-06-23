// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.javasandbox;

import java.util.ArrayList;
import java.util.HashSet;
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
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.ProgramSource;

/**
 * Utility methods for working with sandboxed Java (and other JVM languages) code.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
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

	/**
	 * College "dynamic" compiler diagnostics from completed {@link IsolatedTaskWithCompilerDiagnostic}s.
	 * 
	 * @param tasks the list of {@link IsolatedTaskWithCompilerDiagnostic}s
	 * @return list of {@link CompilerDiagnostic}s
	 */
	public static List<CompilerDiagnostic> collectDynamicCompilerDiagnostics(
			List<? extends IsolatedTaskWithCompilerDiagnostic<TestResult>> tasks) {
		List<CompilerDiagnostic> dynamicCompilerDiagnosticList = new ArrayList<CompilerDiagnostic>();
		HashSet<CompilerDiagnostic> seen = new HashSet<CompilerDiagnostic>();
		for (IsolatedTaskWithCompilerDiagnostic<TestResult> task : tasks) {
			CompilerDiagnostic diag = task.getCompilerDiagnostic();
			if (diag != null && !seen.contains(diag)) {
				seen.add(diag);
				dynamicCompilerDiagnosticList.add(diag);
			}
		}
		return dynamicCompilerDiagnosticList;
	}

	/**
	 * Create a "dynamic" {@link CompilationResult} where some test cases may have
	 * produced {@link CompilerDiagnostic}s.
	 * 
	 * @param programSource                  the scaffolded {@link ProgramSource}
	 * @param dynamicCompilerDiagnosticList  the list of dynamic {@link CompilerDiagnostic}s
	 * @return the {@link CompilationResult}
	 */
	public static CompilationResult createDynamicCompilationResult(ProgramSource programSource, List<CompilerDiagnostic> dynamicCompilerDiagnosticList) {
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.SUCCESS);
		int numDynamicCompilerDiagnostics = dynamicCompilerDiagnosticList.size();
		compilationResult.setCompilerDiagnosticList(dynamicCompilerDiagnosticList.toArray(new CompilerDiagnostic[numDynamicCompilerDiagnostics]));
		compilationResult.adjustDiagnosticLineNumbers(programSource.getPrologueLength(), programSource.getEpilogueLength());
		return compilationResult;
	}
}
