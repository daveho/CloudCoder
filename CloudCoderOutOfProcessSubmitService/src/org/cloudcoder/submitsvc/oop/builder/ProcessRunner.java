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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a subprocess, capturing its stdout and stderr as text.
 * Optionally, send text to the stdin of the process.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class ProcessRunner {
    private static final Logger logger=LoggerFactory.getLogger(ProcessRunner.class);
    
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
	
	public List<String> getStdout() {
		return stdoutCollector.getCollectedOutput();
	}
	
	public List<String> getStderr() {
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
    
    private String merge(String[] arr) {
        StringBuilder builder=new StringBuilder();
        for (String s : arr) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString();
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
}
