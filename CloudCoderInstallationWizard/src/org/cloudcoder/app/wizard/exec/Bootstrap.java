package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.DocumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	public interface SSHRunnable {
		public void run(SSHClient ssh) throws IOException;
	}
	
	// Real download site
	//public static final String DOWNLOAD_SITE = "https://s3.amazonaws.com/cloudcoder-binaries";

	// Temporary site for development
	public static final String DOWNLOAD_SITE = "http://faculty.ycp.edu/~dhovemey/cloudcoder";
	
	public static final String BOOTSTRAP_SCRIPT = DOWNLOAD_SITE + "/bootstrap.pl";
	
	private static class Drain implements Runnable {
		private InputStream is;
		private OutputStream os;
		
		public Drain(InputStream is, OutputStream os) {
			this.is = is;
			this.os = os;
		}
		
		@Override
		public void run() {
			try {
				IOUtils.copy(is, os);
			} catch (IOException e) {
				logger.error("Error draining stream ", e);
			} finally {
				IOUtils.closeQuietly(is);
				// Note: do NOT close the output stream
			}
		}
	}
	
	private ICloudInfo info;
	private Document document;

	public Bootstrap(ICloudInfo info, Document document) {
		this.info = info;
		this.document = document;
	}
	
	public void bootstrapWebappServer() throws ExecException {
		try {
			// Fetch the bootstrap script from the download site
			executeCommand("wget " + BOOTSTRAP_SCRIPT);
			
			// Generate a bootstrap config properties file
			File bootstrapPropertiesFile = new File(info.getDataDir(), "bootstrap.properties");
			try (PrintWriter w = new PrintWriter(new FileWriter(bootstrapPropertiesFile))) {
				w.println("# Bootstrap properties generated by CloudCoder installation wizard");
				writeConfigProperty(w, "ccUser", "ccAcct.username");
				writeConfigProperty(w, "ccPassword", "ccAcct.password");
				writeConfigProperty(w, "ccFirstName", "ccAcct.firstname");
				writeConfigProperty(w, "ccLastName", "ccAcct.lastname");
				writeConfigProperty(w, "ccEmail", "ccAcct.email");
				writeConfigProperty(w, "ccWebsite", "ccAcct.website");
				writeConfigProperty(w, "ccInstitutionName", "instDetails.institutionName");
				writeConfigProperty(w, "ccMysqlRootPasswd", "mysqlAcct.rootPasswd");
				writeConfigProperty(w, "ccMysqlCCPasswd", "mysqlAcct.ccPasswd");
				writeConfigProperty(w, "ccHostname", "dns.hostname");
			}
			
			// Copy bootstrap properties file to webapp instance
			copyFile(bootstrapPropertiesFile);
		} catch (Exception e) {
			throw new ExecException("Failed to bootstrap webapp server", e);
		}
	}

	private void writeConfigProperty(PrintWriter w, String propName, String varName) {
		w.printf("%s=%s\n", propName, document.getValue(varName).getString());
	}

	/**
	 * Execute remote command on the webapp server,
	 * diverting the remote process's stdout and stderr to
	 * System.out and System.err.
	 * 
	 * @param cmdStr  the command to execute
	 * @throws ExecException
	 */
	private void executeCommand(final String cmdStr) throws ExecException {
		try {
			doSsh(new SSHRunnable() {
				@Override
				public void run(SSHClient ssh) throws IOException {
					Session session = ssh.startSession();
					session.setEnvVar("LANG", "en_US.UTF-8");
					try {
						System.out.println("Executing command: " + cmdStr);
						Command cmd = session.exec(cmdStr);

						// Divert command output and error
						Thread t1 = new Thread(new Drain(cmd.getInputStream(), System.out));
						Thread t2 = new Thread(new Drain(cmd.getErrorStream(), System.err));
						t1.start();
						t2.start();
						try {
							t1.join();
							t2.join();
						} catch (InterruptedException e) {
							// Should not happen (?)
							throw new IOException("Drain thread interrupted?", e);
						}
						
						cmd.join(10, TimeUnit.SECONDS);
						System.out.println("Command exit code is " + cmd.getExitStatus());
					} finally {
						session.close();
					}
				}
			});
		} catch (Exception e) {
			throw new ExecException("Error executing bootstrap command", e);
		}
	}
	
	private void copyFile(final File localFile) throws ExecException {
		try {
			doSsh(new SSHRunnable() {
				@Override
				public void run(SSHClient ssh) throws IOException {
					System.out.println("Uploading " + localFile.getAbsolutePath() + " to webapp server");
					ssh.useCompression();
					SCPFileTransfer scp = ssh.newSCPFileTransfer();
					// TODO: use TransferListener to report progress
					scp.upload(localFile.getAbsolutePath(), ".");
				}
			});
		} catch (Exception e) {
			throw new ExecException("Error copying bootstrap file", e);
		}
	}

	private void doSsh(SSHRunnable r) throws IOException, UserAuthException, TransportException {
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier()); // FIXME: would be nice to have actual host key fingerprint
		try {
			ssh.connect(info.getWebappPublicIp());
			KeyProvider keys = ssh.loadKeys(info.getPrivateKeyFile().getAbsolutePath());
			ssh.authPublickey(info.getWebappServerUserName(), keys);
			r.run(ssh);
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
		Document document = DocumentFactory.create();
		// TODO: populate Document
		
		// Create ccinstall directory if it doesn't exist
		File dataDir = new File(System.getProperty("user.home"), "ccinstall");
		dataDir.mkdirs();
		if (!dataDir.isDirectory()) {
			System.err.println("Could not create " + dataDir.getAbsolutePath());
			System.exit(1);
		}
		info.setDataDir(dataDir);
		
		Bootstrap bootstrap = new Bootstrap(info, document);
		
		bootstrap.bootstrapWebappServer();
	}
}
