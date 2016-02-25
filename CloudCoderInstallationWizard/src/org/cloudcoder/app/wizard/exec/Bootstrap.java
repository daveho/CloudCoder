package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class Bootstrap {
	private ICloudInfo info;

	public Bootstrap(ICloudInfo info) {
		this.info = info;
	}
	
	public void bootstrapWebappServer() throws ExecException {
		try {
			SSHClient ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier()); // FIXME: would be nice to have actual host key fingerprint
			ssh.connect(info.getWebappPublicIp());
			KeyProvider keys = ssh.loadKeys(info.getPrivateKeyFile().getAbsolutePath());
			ssh.authPublickey(info.getWebappServerUserName(), keys);
			
			System.out.println("We're connected?");
			
			ssh.close();
		} catch (IOException e) {
			throw new ExecException("Error bootstrapping CloudCoder on webapp instance", e);
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
