package org.cloudcoder.builder2.rubymethod;

import java.io.PrintStream;
import java.util.List;

import org.cloudcoder.builder2.javasandbox.AbstractKillableTaskManager;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.ThreadedPrintStreamMonitor;
import org.cloudcoder.builder2.javasandbox.TimeoutHandler;
import org.jruby.embed.ScriptingContainer;

public class RubyKillableTaskManager<T> extends AbstractKillableTaskManager<T>
{
    private ScriptingContainer container;
    private final PrintStream stdout;
    private final PrintStream stderr;
    
    public RubyKillableTaskManager(List<? extends IsolatedTask<T>> tasks, 
        long maxRunTime, 
        TimeoutHandler<T> timeoutHandler,
        ScriptingContainer container)
    {
        super(tasks, maxRunTime, timeoutHandler);
        this.container=container;
        stdout=container.getOutput();
        stderr=container.getError();
    }
    
    @Override
    public void redirectStandardOutputStreams() {
        stdOutMonitor=new ThreadedPrintStreamMonitor(stdout);
        stdErrMonitor=new ThreadedPrintStreamMonitor(stderr);
        container.setOutput(stdOutMonitor);
        container.setError(stdErrMonitor);
    }
    @Override
    public void unredirectStandardOutputStreams() {
        container.setOutput(stdout);
        container.setError(stderr);
    }
}
