package org.cloudcoder.app.wizard.exec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
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
import org.apache.commons.io.output.TeeOutputStream;
import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.DocumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>> {
	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	public interface SSHRunnable<E> {
		public E run(SSHClient ssh) throws IOException;
	}
	
	// Real download site
	//public static final String DOWNLOAD_SITE = "https://s3.amazonaws.com/cloudcoder-binaries";

	// Temporary site for development
	public static final String DOWNLOAD_SITE = "http://faculty.ycp.edu/~dhovemey/cloudcoder";
	
	public static final String BOOTSTRAP_SCRIPT = DOWNLOAD_SITE + "/bootstrap.pl";
	
	private static class Drain implements Runnable {
		private InputStream is;
		private OutputStream os;
		private ByteArrayOutputStream capture;
		
		public Drain(InputStream is, OutputStream os) {
			this(is, os, false);
		}		
		
		public Drain(InputStream is, OutputStream os, boolean capture) {
			this.is = is;
			this.os = os;
			if (capture) {
				this.capture = new ByteArrayOutputStream();
			}
		}
		
		@Override
		public void run() {
			try {
				OutputStream os = (capture != null) ? new TeeOutputStream(this.os, capture) : this.os;
				
				IOUtils.copy(is, os);
				os.flush();
			} catch (IOException e) {
				logger.error("Error draining stream ", e);
			} finally {
				IOUtils.closeQuietly(is);
				// Note: do NOT close the output stream, but close the capture output stream if there is one
				IOUtils.closeQuietly(capture);
			}
		}
		
		public String getCapturedOutput() {
			return new String(capture.toByteArray(), Charset.forName("UTF-8"));
		}
	}

	private ServiceType cloudService;

	public Bootstrap(ServiceType cloudService) {
		this.cloudService = cloudService;
	}
	
	public void establishSshConnectivity() throws ExecException {
		// Run a simple "Hello, world" command on the webapp instance.
		// This establishes that it is possible to connect via ssh,
		// which can take some time, so some number of retries may
		// be needed.
		int retries = 0;
		ExecException ex = null;
		while (true) {
			try {
				executeCommand("echo 'Hello world'");
				System.out.println("Hello world command executed successfully - ssh connectivity established");
				return;
			} catch (ExecException e) {
				System.err.println("Error executing hello world command (to establish ssh connectivity)");
				e.printStackTrace();
				ex = e;
				retries++;

				if (retries >= 10) {
					System.err.println("Too many retries attempting to run hello world command");
					throw ex;
				}
				
				System.out.println("Waiting 10 seconds before next retry");
				Util.sleep(10000);
			}
		}
	}
	
	public void downloadBootstrapScript() throws ExecException {
		try {
			// Fetch the bootstrap script from the download site
			executeCommand("wget " + BOOTSTRAP_SCRIPT);
		} catch (Exception e) {
			throw new ExecException("Error downloading bootstrap script on webapp instance", e);
		}
	}
	
	public void uploadBootstrapProperties() throws ExecException {
		try {
			// Generate a bootstrap config properties file
			File bootstrapPropertiesFile = new File(InstallationConstants.DATA_DIR, "bootstrap.properties");
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
			throw new ExecException("Error uploading bootstrap properties to webapp instance", e);
		}
	}
	
	public void runBootstrapScript() throws ExecException {
		try {
			// Make the bootstrap script executable
			executeCommand("chmod a+x bootstrap.pl");
			
			// Execute the bootstrap script
			executeCommand("./bootstrap.pl --config=bootstrap.properties --enable=integrated-builder");
		} catch (Exception e) {
			throw new ExecException("Error executing build script", e);
		}
	}
	
	public void configureDuckDnsHostName() throws ExecException {
		// Note that errors here are non-fatal.
		
		try {
			if (!cloudService.getDocument().getValue("dynDns.useDuckDns").getBoolean()) {
				// Not using Duck DNS
				throw new NonFatalExecException("Not using Duck DNS");
			}
			
			String authToken = cloudService.getDocument().getValue("dynDns.duckDnsToken").getString();
			String dnsHostname = cloudService.getDocument().getValue("dns.hostname").getString();
			String ipAddress = cloudService.getInfo().getWebappPublicIp();
			
			String domain = dnsHostname.substring(0, dnsHostname.length() - ".duckdns.org".length());
			
			String updateUrl =
					"https://www.duckdns.org/update?domains=" + domain + "&token=" + authToken + "&ip=" + ipAddress;
			System.out.println("Updating Duck DNS using URL " + updateUrl);

			// OpenJDK doesn't trust the StartSSL root certificate,
			// which is needed to talk to duckdns.org via HTTPS,
			// so we can't use Java to update Duck DNS.  However,
			// we CAN use ssh to run a curl command on the webapp
			// instance.
			String output = executeCommandAndCaptureOutput("curl --silent --show-error '" + updateUrl + "'");
			if (!output.trim().equals("OK")) {
				throw new NonFatalExecException("Non-OK result from Duck DNS update: " + output);
			}
		} catch (Exception e) {
			throw new NonFatalExecException("Error updating Duck DNS dynamic IP address", e);
		}
	}
	
	public void verifyDnsHostname(boolean checkPublicIp) throws ExecException {
		// Get hostname and public ip address
		String hostname = cloudService.getDocument().getValue("dns.hostname").getString();
		String publicIp = cloudService.getInfo().getWebappPublicIp();
		
		// Convert public ip address to an InetAddress
		InetAddress expectedAddr = null;
		if (checkPublicIp) {
			try {
				expectedAddr = InetAddress.getByName(publicIp);
			} catch (UnknownHostException e1) {
				throw new NonFatalExecException(
						"Public ip " + publicIp + " could not be converted to InetAddress (should not happen)", e1);
			}
		}
		
		// It might take a few tries to resolve the hostname, since we just
		// added the DNS entry.
		int retries = 0;
		while (true) {
			try {
				InetAddress addr = InetAddress.getByName(hostname);
				if (!checkPublicIp) {
					System.out.printf("Hostname %s resolves as %s, not checking expected public ip\n", hostname, addr.toString());
					return;
				}
				if (addr.equals(expectedAddr)) {
					System.out.printf("Hostname %s resolves as %s, looks good\n", hostname, addr.toString());
					return;
				}
				System.err.printf(
						"Resolved ip address %s for hostname %s does not match expected ip address %s\n",
						addr.toString(), hostname, expectedAddr.toString());
			} catch (UnknownHostException e) {
				System.err.printf("Could not resolve hostname %s\n", hostname);
			}
			
			if (retries >= 20) {
				throw new NonFatalExecException("Too many retries verifying DNS entry, giving up");
			}
			
			System.err.println("Waiting 10 seconds to retry...");
			Util.sleep(10000);
			retries++;
		}
	}
	
	// Issue and install a Let's Encrypt SSL certificate!
	public void letsEncrypt() throws ExecException {
		int exitCode;
		try {
			System.out.println("Issuing/installing Let's Encrypt SSL certificate...");
			// The bootstrap script handles the work here
			exitCode = executeCommand("./bootstrap.pl --config=bootstrap.properties letsencrypt");
		} catch (Exception e) {
			throw new NonFatalExecException("Error issuing/installing Let's Encrypt SSL certificate", e);
		}
		
		if (exitCode != 0) {
			throw new NonFatalExecException(
					"Executing bootstrap script to issue/install Let's Encrypt SSL cert exited with code " +
					exitCode);
		}
		System.out.println("Let's Encrypt SSL certificate issued and installed!");
	}

	private void writeConfigProperty(PrintWriter w, String propName, String varName) {
		w.printf("%s=%s\n", propName, cloudService.getDocument().getValue(varName).getString());
	}
	
	private interface GetCommandResult<E> {
		public E get(Command cmd, Drain outputDrain);
	}

	/**
	 * Execute remote command on the webapp server,
	 * diverting the remote process's stdout and stderr to
	 * System.out and System.err.
	 * 
	 * @param cmdStr  the command to execute
	 * @return the command's exit code
	 * @throws ExecException
	 */
	private int executeCommand(final String cmdStr) throws ExecException {
		return doCommand(cmdStr, false, new GetCommandResult<Integer>() {
			@Override
			public Integer get(Command cmd, Drain outputDrain) {
				return cmd.getExitStatus();
			}
		});
	}
	
	/**
	 * Execute remote command on the webapp server,
	 * diverting the remote process's stdout and stderr to
	 * System.out and System.err, and also capturing
	 * the remote process's stdout as a string.
	 * 
	 * @param cmdStr  the command to execute
	 * @return the captured stdout of the command
	 * @throws ExecException
	 */
	private String executeCommandAndCaptureOutput(final String cmdStr) throws ExecException {
		return doCommand(cmdStr, true, new GetCommandResult<String>() {
			@Override
			public String get(Command cmd, Drain outputDrain) {
				return outputDrain.getCapturedOutput();
			}
		});
	}

	private<E> E doCommand(final String cmdStr, final boolean captureOutput, final GetCommandResult<E> getResult) throws ExecException {
		try {
			return doSsh(new SSHRunnable<E>() {
				@Override
				public E run(SSHClient ssh) throws IOException {
					System.out.println("Starting ssh session...");
					Session session = ssh.startSession();
					System.out.println("ssh session started");
					session.setEnvVar("LANG", "en_US.UTF-8");
					try {
						System.out.println("Executing command: " + cmdStr);
						Command cmd = session.exec(cmdStr);

						// Divert command output and error
						System.out.println("Starting output redirection threads");
						Drain outputDrain = new Drain(cmd.getInputStream(), System.out, captureOutput);
						Thread t1 = new Thread(outputDrain);
						Thread t2 = new Thread(new Drain(cmd.getErrorStream(), System.err));
						t1.start();
						t2.start();
						try {
							System.out.println("Waiting for output redirection threads to complete...");
							t1.join();
							t2.join();
							System.out.println("Output redirection threads are complete");
						} catch (InterruptedException e) {
							// Should not happen (?)
							throw new IOException("Drain thread interrupted?", e);
						}
						
						System.out.println("Waiting for command to complete...");
						cmd.join(10, TimeUnit.SECONDS);
						System.out.println("Command exit code is " + cmd.getExitStatus());
						return getResult.get(cmd, outputDrain);
					} finally {
						System.out.println("Closing ssh session");
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
			doSsh(new SSHRunnable<Boolean>() {
				@Override
				public Boolean run(SSHClient ssh) throws IOException {
					System.out.println("Uploading " + localFile.getAbsolutePath() + " to webapp server");
					ssh.useCompression();
					SCPFileTransfer scp = ssh.newSCPFileTransfer();
					// TODO: use TransferListener to report progress
					scp.upload(localFile.getAbsolutePath(), ".");
					return true;
				}
			});
		} catch (Exception e) {
			throw new ExecException("Error copying bootstrap file", e);
		}
	}

	private<E> E doSsh(SSHRunnable<E> r) throws IOException, UserAuthException, TransportException {
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier()); // FIXME: would be nice to have actual host key fingerprint
		try {
			InfoType info = cloudService.getInfo();
			System.out.println("Starting ssh connection...");
			
			String hostname;
			Document document = cloudService.getDocument();
			if (document.getValue("db.sshConnectViaHostname").getBoolean()) {
				// Identify host via hostname
				hostname = document.getValue("dns.hostname").getString();
			} else {
				// Identify host via public ip address
				hostname = info.getWebappPublicIp();
			}
			ssh.connect(hostname);
			
			System.out.println("Connected");
			KeyProvider keys = ssh.loadKeys(info.getPrivateKeyFile().getAbsolutePath());
			System.out.println("Doing ssh authentication using keypair");
			ssh.authPublickey(info.getWebappServerUserName(), keys);
			System.out.println("Authentication successful");
			return r.run(ssh);
		} finally {
			System.out.println("Closing ssh connection");
			ssh.close();
		}
	}
	
	// This is just for testing
	public static class TestCloudInfo extends AbstractCloudInfo implements ICloudInfo {
		private String username;
		private String hostAddress;
		private String keyPairFilename;
		private String privateIp;

		public TestCloudInfo(String username, String hostAddress, String keyPairFilename) {
			this(username, hostAddress, keyPairFilename, "10.0.0.222");
		}
		
		public TestCloudInfo(String username, String hostAddress, String keyPairFilename, String privateIp) {
			this.username = username;
			this.hostAddress = hostAddress;
			this.keyPairFilename = keyPairFilename;
			this.privateIp = privateIp;
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
		public void setPrivateKeyFile(File privateKeyFile) {
			throw new UnsupportedOperationException();
		}

		@Override
		public File getPrivateKeyFile() {
			return new File(keyPairFilename);
		}

		@Override
		public String getWebappPrivateIp() {
			return privateIp;
		}

		@Override
		public String getWebappServerUserName() {
			return username;
		}
	}
	
	private static class TestCloudService extends AbstractCloudService<TestCloudInfo, TestCloudService> {
		private Document document;
		private TestCloudInfo info;

		public TestCloudService(Document document, TestCloudInfo info) {
			this.document = document;
			this.info = info;
		}

		@Override
		public Document getDocument() {
			return document;
		}

		@Override
		public void addInstallSteps(
				InstallationProgress<TestCloudInfo, TestCloudService> progress) {
			throw new UnsupportedOperationException();
		}

		@Override
		public TestCloudInfo getInfo() {
			return info;
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
		TestCloudService cloudService = new TestCloudService(document, info);
		
		Bootstrap<TestCloudInfo, TestCloudService> bootstrap = new Bootstrap<TestCloudInfo, TestCloudService>(cloudService);
		
		bootstrap.downloadBootstrapScript();
		bootstrap.uploadBootstrapProperties();
		bootstrap.runBootstrapScript();
	}
}
