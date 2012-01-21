/*
 * Web C programming environment
 * Copyright (c) 2010-2011, David H. Hovemeyer <david.hovemeyer@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudcoder.submitsvc.oop.builder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cloudcoder.submitsvc.oop.builder.CUtil.merge;

/**
 * Run a subprocess, capturing its stdout and stderr as text.
 * Optionally, send text to the stdin of the process.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class ProcessRunner implements ITestOutput {
	private static final Logger logger=LoggerFactory.getLogger(ProcessRunner.class);
    
	private static String RUN_PROCESS_SCRIPT;
	static {
		// FIXME: find a way to make this work if we're running from a jar file
		String runProcessPath = ProcessRunner.class.getPackage().getName().replace('.', '/') + "/res/" + "runProcess.pl";
		//System.out.println("path: " + runProcessPath);
		URL url = ProcessRunner.class.getClassLoader().getResource(runProcessPath);
		if (url != null) {
			String fileName = url.toExternalForm();
			//System.out.println("fileName: " + fileName);
			if (fileName.startsWith("file://")) {
				RUN_PROCESS_SCRIPT = fileName.substring("file://".length());
			} else if (fileName.startsWith("file:")) {
				RUN_PROCESS_SCRIPT = fileName.substring("file:".length());
			}
		}
		if (RUN_PROCESS_SCRIPT == null || !(new File(RUN_PROCESS_SCRIPT).exists())) {
			throw new IllegalStateException("can't find filename of runProcess.pl script");
		}
		System.out.println("Run process script: " + RUN_PROCESS_SCRIPT);
	}
    
	private String statusMessage = "";
	private int exitCode;
	private volatile Process process;
	private Thread exitValueMonitor;
	private String stdin;
	private OutputCollector stdoutCollector;
	private OutputCollector stderrCollector;
	private InputSender stdinSender;
	
	private Map<String,String> env=new HashMap<String,String>();
	
	/**
	 * Constructor.
	 */
	public ProcessRunner() {
	    for (Entry<String,String> entry : System.getenv().entrySet()) {
	        env.put(entry.getKey(), entry.getValue());
	    }
	}
	
	/**
	 * Set text to send to the process as its standard input.
	 * 
	 * @param stdin text to send to the process as its standard input
	 */
	public void setStdin(String stdin) {
		this.stdin = stdin;
	}
	
	private String[] getEnvp() {
	    String[] envp=new String[env.size()];
	    int i=0;
	    for (Entry<String,String> entry : env.entrySet()) {
	        envp[i]=entry.getKey()+"="+entry.getValue();
	        i+=1;
	    }
	    return envp;
	}
	
	public void addDirToPath(String dir) {
	    String path=env.get("PATH");
	    path+=File.separatorChar+dir;
	    env.put("PATH", path);
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public boolean runSynchronous(File workingDir, String[] command) {
	 // exec command
        logger.info("Running in {} the command: {} with env: {} ",
                new Object[] {workingDir.toString(), merge(command), merge(getEnvp())});
        try {
            process = Runtime.getRuntime().exec(command, getEnvp(), workingDir);

            // collect compiler output
            stdoutCollector = new OutputCollector(process.getInputStream());
            stderrCollector = new OutputCollector(process.getErrorStream());
            stdoutCollector.start();
            stderrCollector.start();
            
            // If stdin was provided, send it
            if (stdin != null) {
            	stdinSender = new InputSender(process.getOutputStream(), stdin);
            	stdinSender.start();
            }

            // wait for process and output collector threads to finish
            exitCode = process.waitFor();
            stdoutCollector.join();
            stderrCollector.join();
            if (stdinSender != null) {
            	stdinSender.join();
            }

            statusMessage = "Process finished";
            return true;
        } catch (IOException e) {
            statusMessage = "Could not execute process: " + e.getMessage();
        } catch (InterruptedException e) {
            statusMessage = "Process was interrupted (infinite loop killed?)";
        }
        return false;
	}
	
	public void runAsynchronous(final File workingDir, final String[] command) {
	    exitValueMonitor=new Thread() {
	        public void run() {
	            runSynchronous(workingDir, command);
	        }
	    };
	    exitValueMonitor.start();
	}
	
	public int getExitCode() {
		return exitCode;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IHasStdoutAndStderr#getStdout()
	 */
	@Override
	public String getStdout() {
		return CUtil.merge(getStdoutAsList());
	}

	/**
	 * @return the standard output written by the process as a List of strings
	 */
	public List<String> getStdoutAsList() {
		return stdoutCollector.getCollectedOutput();
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IHasStdoutAndStderr#getStderr()
	 */
	@Override
	public String getStderr() {
		return CUtil.merge(getStderrAsList());
	}

	/**
	 * @return the standard error written by the process as a List of strings
	 */
	public List<String> getStderrAsList() {
		return stderrCollector.getCollectedOutput();
	}

    public boolean isRunning() {
    	Process p = process;
    	
    	if (p == null) {
    		// Process hasn't started yet.
    		// Count that as "running".
    		return true;
    	}
    	
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e){
            return true;
        }
    }
    
    public void killProcess() {
        logger.info("Killing process");
        process.destroy();
        stdoutCollector.interrupt();
        stderrCollector.interrupt();
        if (stdinSender != null) {
        	stdinSender.interrupt();
        }
    }

	/**
	 * Determine whether or not the process ended with a fatal signal.
	 * 
	 * @return true if the process ended with a fatal signal, false if
	 *         it exited normally
	 */
	public boolean isCoreDump() {
        // error code 6 means CORE DUMP
		// FIXME: how robust is this?
		return getExitCode() == 6;
	}
}
