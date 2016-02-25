package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class Bootstrap {
	public static final String DOWNLOAD_SITE = "https://s3.amazonaws.com/cloudcoder-binaries/";
	public static final String BOOTSTRAP_SCRIPT = DOWNLOAD_SITE + "/bootstrap.pl";
	
	private static class DrainStdout implements ICallbackWithIOException<Command> {
		@Override
		public void call(Command value) throws IOException {
			// FIXME: we ought to drain stderr as well
			IOUtils.copy(value.getInputStream(), System.out);
		}
	}
	
	private ICloudInfo info;

	public Bootstrap(ICloudInfo info) {
		this.info = info;
	}
	
	public void bootstrapWebappServer() throws ExecException {
		try {
			runCommand("wget " + BOOTSTRAP_SCRIPT, new DrainStdout());
		} catch (Exception e) {
			throw new ExecException("Error bootstrapping CloudCoder on webapp instance", e);
		}
	}
	
	private void runCommand(final String command, final ICallbackWithIOException<Command> processCommand) throws IOException {
		doSsh(new ICallbackWithIOException<Session>() {
			@Override
			public void call(Session value) throws IOException {
				Command cmd = value.exec(command);
				processCommand.call(cmd);
			}
		});
	}

	private void doSsh(ICallbackWithIOException<Session> callback) throws IOException {
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier()); // FIXME: would be nice to have actual host key fingerprint
		try {
			ssh.connect(info.getWebappPublicIp());
			KeyProvider keys = ssh.loadKeys(info.getPrivateKeyFile().getAbsolutePath());
			ssh.authPublickey(info.getWebappServerUserName(), keys);
			Session session = ssh.startSession();
			try {
//				System.out.println("We're connected?");
				callback.call(session);
			} finally {
				session.close();
			}
		} finally {
			ssh.close();
		}
	}
	
	// This is just for testing
	private static class TestCloudInfo extends AbstractCloudInfo implements ICloudInfo {
		private String username;
		private String hostAddress;
		private String keyPairFilename;

		public TestCloudInfo(String username, String hostAddress, String keyPairFilename) {
			this.username = username;
			this.hostAddress = hostAddress;
			this.keyPairFilename = keyPairFilename;
		}

		@Override
		public String getWebappPublicIp() {
			return hostAddress;
		}

		@Override
		public boolean isPrivateKeyGenerated() {
			throw new UnsupportedOperationException();
		}

		@Override
		public File getPrivateKeyFile() {
			return new File(keyPairFilename);
		}

		@Override
		public String getWebappPrivateIp() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getWebappServerUserName() {
			return username;
		}
		
	}

	// This is just for testing
	public static void main(String[] args) throws ExecException {
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Host username: ");
		String username = keyboard.nextLine();
		System.out.print("Host address: ");
		String hostAddress = keyboard.nextLine();
		System.out.print("Keypair file: ");
		String keyPairFilename = keyboard.nextLine();

		TestCloudInfo info = new TestCloudInfo(username, hostAddress, keyPairFilename);
		
		Bootstrap bootstrap = new Bootstrap(info);
		
		bootstrap.bootstrapWebappServer();
	}
}
