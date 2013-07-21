// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.loadtester;

import java.util.concurrent.atomic.AtomicInteger;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.SubmissionResult;

/**
 * Runnable for running a single {@link LoadTester} thread.
 * 
 * @author David Hovemeyer
 */
public class LoadTesterTask implements Runnable {
	private static final ThreadLocal<LoadTesterTask> threadLocalTask = new ThreadLocal<LoadTesterTask>();
	
	private static final AtomicInteger sequence = new AtomicInteger(0);
	
	private int sequenceNumber;
	private HostConfig hostConfig;
	private String userName;
	private String password;
	private EditSequence editSequence;
	private int repeatCount;
	private Client client;
	private ICallback<Change[]> onSend;
	private ICallback<SubmissionResult> onSubmissionResult;
	
	/**
	 * Constructor.
	 */
	public LoadTesterTask() {
		this.sequenceNumber = sequence.incrementAndGet();
	}
	
	/**
	 * @return the task running in the current thread
	 */
	public static LoadTesterTask getCurrent() {
		return threadLocalTask.get();
	}
	
	/**
	 * @return the sequence number of this task
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Set the {@link HostConfig} (how to connect to the webapp).
	 * 
	 * @param hostConfig the {@link HostConfig}
	 */
	public void setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
	}
	
	/**
	 * Set username of the test user account to use for this thread.
	 * 
	 * @param userName the username
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Set the password of the test user account to use for this thread.
	 * 
	 * @param password the password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Set the {@link EditSequence} to play.
	 * 
	 * @param editSequence the {@link EditSequence}
	 */
	public void setEditSequence(EditSequence editSequence) {
		this.editSequence = editSequence;
	}
	
	/**
	 * Set the repeat count: how many times to repeat playing the {@link EditSequence}.
	 * 
	 * @param repeatCount the repeat count
	 */
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}
	
	/**
	 * Set the on send callback, invoked when {@link Change}s are sent.
	 * 
	 * @param onSend the on send callback
	 */
	public void setOnSend(ICallback<Change[]> onSend) {
		this.onSend = onSend;
	}
	
	/**
	 * Set the on submission result callback, invoked when a {@link SubmissionResult}
	 * is received.
	 * 
	 * @param onSubmissionResult the on submission result callback
	 */
	public void setOnSubmissionResult(ICallback<SubmissionResult> onSubmissionResult) {
		this.onSubmissionResult = onSubmissionResult;
	}
	
	@Override
	public void run() {
		try {
			threadLocalTask.set(this);
			doRun();
		} catch (Exception e) {
			StringBuilder buf = new StringBuilder();
			buf.append("LoadTesterTask");
			if (client != null && client.getUser() != null) {
				buf.append(" (user=");
				buf.append(client.getUser().getUsername());
				buf.append(")");
			}
			buf.append(" caught exception: " + e.toString());
			System.err.println(buf.toString());
			e.printStackTrace(System.err);
		}
	}

	private void doRun() throws Exception {
		this.client = new Client(hostConfig);
		
		if (!client.login(userName, password)) {
			throw new RuntimeException("Could not log into " + userName + " account");
		}
		
		PlayEditSequence player = new PlayEditSequence();
		player.setClient(client);
		player.setEditSequence(editSequence);
		player.setSubmitOnFullTextChange(true);
		
		player.setOnSend(onSend);
		player.setOnSubmissionResult(onSubmissionResult);
		
		player.setup();
		
		for (int i = 0; i < repeatCount; i++) {
			player.play();
		}
	}

}
