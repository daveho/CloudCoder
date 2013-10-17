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

package org.cloudcoder.builder2.process;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

/**
 * Send text to a process as its standard input (stdin).
 * 
 * @author David Hovemeyer
 */
public class InputSender {
	private OutputStream outputStream;
	private String stdin;
	private Thread writerThread;
	private OutputStreamWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param outputStream  the OutputStream through which to send stdin to the process
	 * @param stdin         the text to send to the process
	 */
	public InputSender(OutputStream outputStream, String stdin) {
		this.outputStream = outputStream;
		this.stdin = stdin;
	}

	/**
	 * Start a thread to send text to the stdin of the process.
	 */
	public void start() {
		Charset utf8 = Charset.forName("UTF-8");
		this.writer = new OutputStreamWriter(outputStream, utf8);
		
		this.writerThread = new Thread(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				try {
					writer.write(stdin);
					writer.flush();
					//System.out.println("Successfully wrote and flushed " + stdin);
				} catch (IOException e) {
					// ignore
					//System.err.println("Error sending stdin");
					//e.printStackTrace();
				} finally {
					IOUtils.closeQuietly(writer);
				}
			}
		});
		
		this.writerThread.start();
	}

	/**
	 * Wait for the InputSender's thread to complete.
	 * 
	 * @throws InterruptedException 
	 */
	public void join() throws InterruptedException {
		writerThread.join();
	}

	/**
	 * Force the InputSender to shutdown abruptly.
	 */
	public void interrupt() {
		// Try to force writer to close (if it hasn't closed already).
		// This should ensure that the writer thread will exit.
		IOUtils.closeQuietly(writer);
		
		// And, just in case the thread is blocked on some interruptable
		// action (sleep, wait, etc.), interrupt it.
		writerThread.interrupt();
	}
}
