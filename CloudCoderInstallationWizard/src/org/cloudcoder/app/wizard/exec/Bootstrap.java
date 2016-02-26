package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
	
	public static final String DOWNLOAD_SITE = "https://s3.amazonaws.com/cloudcoder-binaries/";
	public static final String BOOTSTRAP_SCRIPT = DOWNLOAD_SITE + "bootstrap.pl";
	
	private static class Drain implements Runnable {
		private InputStream is;
		private StringWriter sink;
		private Thread thread;
		
		public Drain(InputStream is) {
			this.is = is;
			this.sink = new StringWriter();
		}
		
		public String getOutput() {
			return sink.toString();
		}
		
		@Override
		public void run() {
			InputStreamReader rdr = new InputStreamReader(is, Charset.forName("UTF-8"));
			try {
				IOUtils.copy(rdr, sink);
			} catch (IOException e) {
				logger.error("Error draining stream ", e);
			} finally {
				IOUtils.closeQuietly(rdr);
				IOUtils.closeQuietly(sink);
			}
		}
		
		public void start() {
			thread = new Thread(this);
			thread.start();
		}
		
		public void join() {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("Interrupted while draining stream", e);
			}
		}
	}
	
	private ICloudInfo info;

	public Bootstrap(ICloudInfo info) {
		this.info = info;
	}
	
	public void bootstrapWebappServer() throws ExecException {
		try {
			SSHClient ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier()); // FIXME: would be nice to have actual host key fingerprint
			try {
				ssh.connect(info.getWebappPublicIp());
				KeyProvider keys = ssh.loadKeys(info.getPrivateKeyFile().getAbsolutePath());
				ssh.authPublickey(info.getWebappServerUserName(), keys);
				Session session = ssh.startSession();
				try {
					System.out.println("Downloading bootstrap script");
					Command cmd = session.exec("wget " + BOOTSTRAP_SCRIPT);
					//System.out.println(IOUtils.readFully(cmd.getInputStream()));
					Drain out = new Drain(cmd.getInputStream());
					Drain err = new Drain(cmd.getErrorStream());
					out.start();
					err.start();
					out.join();
					err.join();
					cmd.join(10, TimeUnit.SECONDS);
					System.out.println("Command exit code is " + cmd.getExitStatus());
					System.out.println("Output:");
					System.out.println(out.getOutput());
					System.out.println("Error:");
					System.out.println(err.getOutput());
				} finally {
					session.close();
				}
			} finally {
				ssh.close();
			}
			
		} catch (Exception e) {
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
