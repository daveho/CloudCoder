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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author jaimespacco
 *
 * @param <T>
 */
public abstract class AbstractKillableTaskManager<T>
{
    protected static final Logger logger = LoggerFactory.getLogger(AbstractKillableTaskManager.class);
    protected static boolean securityManagerInstalled = false;
    /** list of "isolated tasks" to be executed */
    private List<IsolatedTask<T>> tasks;
    /** List of Outcomes; essentially placeholders objects where tasks will put their results */
    private List<Outcome<T>> results;
    private long maxRunTime;
    private int numPauses = 5;

    protected ThreadedPrintStreamMonitor stdOutMonitor;
    protected ThreadedPrintStreamMonitor stdErrMonitor;
    private Map<Integer,String> stdOutMap = new HashMap<Integer,String>();
    private Map<Integer,String> stdErrMap = new HashMap<Integer,String>();
    
    /** Handles timeouts by producing a T representing a timeout event */
    private TimeoutHandler<T> timeoutHandler;
    /** All threads will be in a thread group of worker threads */
    public static final ThreadGroup WORKER_THREAD_GROUP = new ThreadGroup("WorkerThreads");
    public int numThreads = 1;
    protected String threadNamePrefix;

    /**
     * Constructor.
     * Note that {@link #installSecurityManager()} must be called
     * before instances of {@link JVMKillableTaskManager} can be created.
     * 
     * @param tasks          tasks to run
     * @param maxRunTime     maximum time to let any task run
     * @param timeoutHandler callback to run if a timeout occurs
     */
    public AbstractKillableTaskManager(List<IsolatedTask<T>> tasks, 
            long maxRunTime, 
            TimeoutHandler<T> timeoutHandler)
    {
        if (!securityManagerInstalled) {
            throw new IllegalStateException(
                    "Must call KillableTaskManager.installSecurityManager() before creating instance");
        }
        
        this.tasks=tasks;
        this.maxRunTime=maxRunTime;
        this.timeoutHandler=timeoutHandler;

        this.results=new ArrayList<Outcome<T>>(tasks.size());
        for (int i=0; i<tasks.size(); i++) {
            results.add(new Outcome<T>());
        }
        
        this.threadNamePrefix = "Thread";
    }

    /**
     * Set the name prefix that will be used for the names of worker threads.
     * Useful for allowing {@link ThreadGroupSecurityManager} to implement
     * specialized rules for specific worker threads.
     * 
     * @param threadNamePrefix name prefix for names of worker threads
     */
    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public boolean isFinished(int x) {
        return results.get(x).finished;
    }

    public T getResult(int x) {
        return results.get(x).result;
    }

    public List<T> getOutcomes() {
        List<T> ret=new LinkedList<T>();
        for (int i=0; i<results.size(); i++) {
            ret.add(getResult(i));
        }
        return ret;
    }

    public void run() {
        // re-direct stdout/stderr to print stream monitors
        // that will buffer the outputs for each thread
        redirectStandardOutputStreams();

        Thread[] pool=new Thread[tasks.size()];
        for (int i=0; i<tasks.size(); i++) {
            IsolatedTask<T> task=tasks.get(i);
            pool[i]=new WorkerThread<T>(task, results.get(i));
            pool[i].setDaemon(true);
            pool[i].start();
        }

        // now pause a couple of times
        for (int i=1; i<=numPauses; i++) {
            if (!pauseAndPoll(maxRunTime/numPauses, pool)) {
                // we can stop pausing
                break;
            }
        }

        // Go through and kill any threads that haven't finished yet
        // Also put the buffered output from stdout/stderr into the map
        for (int i=0; i<pool.length; i++) {
            Thread t=pool[i];
            if (t.isAlive()) {
                //XXX Yes, I know that stop() is deprecated.  
                //But this is a necessary use of stop!
                t.stop();

                //TODO Log that a thread is being stopped

                // stop the monitors
                stdOutMonitor.flush(t);
                stdOutMonitor.close(t);

                stdErrMonitor.flush(t);
                stdErrMonitor.close(t);

                // handle a timeout
                results.get(i).result=timeoutHandler.handleTimeout();
            }
            stdOutMap.put(i, stdOutMonitor.getBufferedOutput(t));
            stdErrMap.put(i, stdErrMonitor.getBufferedOutput(t));
        }
        // return the original stdout/stderr
        // how to do this varies between Java, Python/Jython
        // and Ruby/JRuby
        unredirectStandardOutputStreams();
    }
    
    /**
     * Redirect standard output and standard error.
     * 
     * How to do this varies between Java, Python/Jython,
     * and Ruby/JRuby.
     * 
     */
    public abstract void unredirectStandardOutputStreams();
    /**
     * Put standard output and standard error back the
     * way that they were.
     * 
     * How to do this varies between Java, Python/Jython,
     * and Ruby/JRuby.
     */
    public abstract void redirectStandardOutputStreams();

    /**
     * Pause for a certain amount of time.  Return true if any
     * of the threads in the given threadpool are still alive,
     * and false if they are all finished.
     * @param time
     * @param pool
     * @return
     */
    private boolean pauseAndPoll(long time, Thread[] pool) {
        try {
            Thread.sleep(time);
            for (Thread t : pool) {
                if (t.isAlive()) {
                    return true;
                }
            }
        } catch (InterruptedException e) {
            // should never happen; to be safe, assume a thread may
            // still be running.
            return true;
        }
        // no threads are alive, so we can stop waiting
        return false;
    }

    /**
     * Install the security manager needed by {@link AbstractKillableTaskManager}.
     */
    public static void installSecurityManager() {
        if (!securityManagerInstalled) {
            // So far the new system of extracting a PyFunction and passing
            // that and the PythonInterpreter into the KillableThread seems to work.
            // The main concern is that this requires removing any executable code that is
            // not inside a method.
            System.setSecurityManager(
                    new ThreadGroupSecurityManager(AbstractKillableTaskManager.WORKER_THREAD_GROUP));
            securityManagerInstalled  = true;
        }
    }

    public Map<Integer, String> getBufferedStdout() {
        return stdOutMap;
    }

    public Map<Integer, String> getBufferedStderr() {
        return stdErrMap;
    }

    public AbstractKillableTaskManager() {
        super();
    }

    /**
     * Simple container for a result of type T and whether the task
     * producing T finished normally.
     * 
     * @author jspacco
     *
     * @param <T>
     */
    private static class Outcome<T> {
        //Outcome() {}
        boolean finished;
        T result;
    }
    /**
     * Worker thread takes a given Task, calls its execute() method
     * to produce a result of type E, and puts the result into the 
     * given outcome container.
     * 
     * This thread is set up so that, assuming that the Task doesn't
     * access any shared resources, it is safe to use the stop() method
     * in thread to halt this thread.
     * 
     * @author jspacco
     *
     * @param <E>
     */
    class WorkerThread<E> extends Thread
    {
        private IsolatedTask<E> task;
        private Outcome<E> out;

        /**
         * Create a thread that executes the given task and puts
         * the result of the task into the given container.
         * 
         * @param task The task to execute
         * @param out The container in which to put the result of the task
         */
        public WorkerThread(IsolatedTask<E> task, Outcome<E> out)
        {
            super(WORKER_THREAD_GROUP, threadNamePrefix+(numThreads++));
            this.task=task;
            this.out=out;
        }

        /**
         * Given a task and a container, execute the task and put
         * its result in the container.  The entire run method catches
         * Throwable, so that if another thread uses stop() to kill this
         * thread, nothing bad should happen.
         * 
         * 
         * @see java.lang.Thread#run()
         */
        public void run() {
            E o;
            try {
                o=task.execute();
                out.result=o;
                out.finished=true;
            } catch (NoClassDefFoundError e) {
                logger.error("Killing test case thread due to class loading error", e);
            } catch (Throwable e) {
                // Make sure that the thread dies very quietly
                // "Attaching an exception-catching silencer to my thread-killing gun"
                logger.error("Killing test case thread for unknown reason", e);
            } finally {
                //System.err.println(System.getSecurityManager());
                //System.out.println(System.getSecurityManager());
            }
        }
    }
}