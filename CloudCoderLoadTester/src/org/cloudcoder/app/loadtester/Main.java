package org.cloudcoder.app.loadtester;

import java.io.IOException;
import java.io.InputStream;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.QuizEndedException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException, IOException, CloudCoderAuthenticationException, InterruptedException, QuizEndedException {
		Client client = new Client("http", "localhost", 8081, "cloudcoder/cloudcoder");
		client.login("user2", "muffin");
		
		EditSequence editSequence = new EditSequence();
		InputStream in = Main.class.getClassLoader().getResourceAsStream("org/cloudcoder/app/loadtester/res/b89ba215e53343923a07d005cb03116ae07a31fb.dat");
		editSequence.loadFromInputStream(in);

		PlayEditSequence player = new PlayEditSequence();
		player.setClient(client);
		player.setEditSequence(editSequence);
		player.setExerciseName("which and how many?");
		
		player.setup();
		player.play(new ICallback<Change[]>() {
			@Override
			public void call(Change[] value) {
				System.out.println("Sending " + value.length + " changes");
			}
		});
		System.out.println("Done!");
	}
}
