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

import java.util.Arrays;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.SubmissionResult;

/**
 * Runnable for running a single {@link LoadTester} thread.
 * 
 * @author David Hovemeyer
 */
public class LoadTesterTask implements Runnable {
	private HostConfig hostConfig;
	private String userName;
	private String password;
	private EditSequence editSequence;
	private int repeatCount;
	private Client client;

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
	
	@Override
	public void run() {
		try {
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
		
		player.setOnSend(new ICallback<Change[]>() {
			@Override
			public void call(Change[] value) {
				if (value.length == 1 && value[0].getType() == ChangeType.FULL_TEXT) {
					System.out.print("\u2191");
				} else {
					char[] a = new char[value.length];
					Arrays.fill(a, '.');
					System.out.print(new String(a));
				}
				System.out.flush();
			}
		});
		player.setOnSubmissionResult(new ICallback<SubmissionResult>() {
			public void call(SubmissionResult value) {
				char c;
				if (value.getCompilationResult().getOutcome() != CompilationOutcome.SUCCESS
						|| value.getNumTestsPassed() < value.getNumTestsAttempted()) {
					c = '\u2639';
				} else {
					c = '\u263A';
				}
				System.out.print(String.valueOf(c));
				System.out.flush();
			}
		});
		
		player.setup();
		
		for (int i = 0; i < repeatCount; i++) {
			player.play();
		}
	}

}
