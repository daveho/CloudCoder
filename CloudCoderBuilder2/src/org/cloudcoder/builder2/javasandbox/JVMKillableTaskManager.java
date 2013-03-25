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

package org.cloudcoder.builder2.javasandbox;

import java.io.PrintStream;
import java.util.List;



/**
 * I'm going to use the stop() method in thread.  I feel a little dirty.
 * 
 * I'm 99% sure that this is a safe use of stop().  Each
 * thread that might be stopped will put its result into
 * a separate object; if that thread is killed early, the
 * result simply won't show up and instead we'll put
 * in a user-supplied default result.  So long as the
 * instances of Task don't hold any locks or put any resources
 * into an inconsistent state, the threads spawned to execute
 * those tasks should be able to be stop()ed without any
 * negative repercussions.
 * 
 * 
 * 
 * @author jspacco
 *
 */
public class JVMKillableTaskManager<T> extends AbstractKillableTaskManager<T>
{
    private final PrintStream originalStdOut=System.out;
    private final PrintStream originalStdErr=System.err;
    
    public JVMKillableTaskManager(List<IsolatedTask<T>> tasks, 
        long maxRunTime, 
        TimeoutHandler<T> timeoutHandler)
    {
        super(tasks, maxRunTime, timeoutHandler);
    }
    
    public void redirectStandardOutputStreams() {
        stdOutMonitor=new ThreadedPrintStreamMonitor(System.out);
        stdErrMonitor=new ThreadedPrintStreamMonitor();
        System.setOut(stdOutMonitor);
        System.setErr(stdErrMonitor);
    }
    
    @Override
    public void unredirectStandardOutputStreams() {
        System.setOut(originalStdOut);
        System.setErr(originalStdErr);
    }
}
