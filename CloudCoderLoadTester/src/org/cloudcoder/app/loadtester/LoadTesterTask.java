package org.cloudcoder.app.loadtester;

import java.util.Arrays;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.SubmissionResult;

public class LoadTesterTask implements Runnable {
	private HostConfig hostConfig;
	private String userName;
	private String password;
	private EditSequence editSequence;
	private int repeatCount;

	public void setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setEditSequence(EditSequence editSequence) {
		this.editSequence = editSequence;
	}
	
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}
	
	@Override
	public void run() {
		try {
			doRun();
		} catch (Exception e) {
			System.err.println("LoadTesterTask caught exception: " + e.toString());
			e.printStackTrace(System.err);
		}
	}

	private void doRun() throws Exception {
		Client client = new Client(hostConfig);
		
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
