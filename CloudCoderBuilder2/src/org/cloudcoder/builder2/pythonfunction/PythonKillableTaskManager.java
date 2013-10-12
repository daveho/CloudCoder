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

package org.cloudcoder.builder2.pythonfunction;

import java.util.List;

import org.cloudcoder.builder2.javasandbox.AbstractKillableTaskManager;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.ThreadedPrintStreamMonitor;
import org.cloudcoder.builder2.javasandbox.TimeoutHandler;
import org.python.util.PythonInterpreter;
/**
 * @author jaimespacco
 *
 * @param <T>
 */
public class PythonKillableTaskManager<T> extends AbstractKillableTaskManager<T>
{
    private PythonInterpreter pythonInterpreter;

    public PythonKillableTaskManager(List<? extends IsolatedTask<T>> tasks, 
        long maxRunTime, 
        TimeoutHandler<T> timeoutHandler,
        PythonInterpreter pythonInterpreter)
    {
        super(tasks, maxRunTime, timeoutHandler);
        this.pythonInterpreter=pythonInterpreter;
    }
    
    @Override
    public void unredirectStandardOutputStreams() {
     // Nothing to do here...
        // We stop using the interpreter when we're done
        // with the test cases
    }

    @Override
    public void redirectStandardOutputStreams() {
        stdOutMonitor=new ThreadedPrintStreamMonitor();
        stdErrMonitor=new ThreadedPrintStreamMonitor();
        pythonInterpreter.setOut(stdOutMonitor);
        pythonInterpreter.setErr(stdErrMonitor);
    }
}
