// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.builder2.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.cloudcoder.builder2.batch.BatchMain;
import org.cloudcoder.daemon.DaemonController;
import org.cloudcoder.daemon.DefaultUpgradeCallback;
import org.cloudcoder.daemon.IDaemon;
import org.cloudcoder.daemon.IOUtil;
import org.cloudcoder.daemon.JarRewriter;
import org.cloudcoder.daemon.Upgrade;
import org.cloudcoder.daemon.Util;

/**
 * {@link DaemonController} implementation for the Builder.
 * Also contains the main method used when the Builder is deployed
 * as an executable jar file.
 * 
 * @author David Hovemeyer
 */
public class Builder2DaemonController extends DaemonController {

	/**
	 * Implementation of {@link Options} for the builder2 daemon.
	 * 
	 * @author David Hovemeyer
	 */
	private final class Builder2Options extends Options {
		private String builderJvmArgs;
		
		public void setBuilderJvmArgs(String builderJvmArgs) {
			this.builderJvmArgs = builderJvmArgs;
		}
		
		@Override
		// Create the stdout log in the "log" directory.
		public String getStdoutLogFileName() {
			// If a stdout log filename was specified on the command line,
			// honor it.  Otherwise use the default of "log/stdout.log".
			String stdoutLogFilename = super.getStdoutLogFileName();
			if (stdoutLogFilename == null) {
				// Use the default stdout log filename.
				stdoutLogFilename = "log/stdout.log";
			}
			return stdoutLogFilename;
		}
		
		@Override
		public String getJvmOptions() {
			// Use the concatenation of jvm args specified in the builder's configuration properties
			// and those specified on the daemon controller command line.
			StringBuilder buf = new StringBuilder();
			if (super.getJvmOptions() != null) {
				buf.append(super.getJvmOptions());
			}
			if (builderJvmArgs != null && !builderJvmArgs.trim().equals("")) {
				buf.append(" ");
				buf.append(builderJvmArgs.trim());
			}
			return buf.toString();
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.daemon.DaemonController#getDefaultInstanceName()
	 */
	@Override
	public String getDefaultInstanceName() {
		return "instance";
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.daemon.DaemonController#getDaemonClass()
	 */
	@Override
	public Class<? extends IDaemon> getDaemonClass() {
		return Builder2Daemon.class;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.daemon.DaemonController#createOptions()
	 */
	@Override
	protected Options createOptions() {
		
		Builder2Options options = new Builder2Options();
		
		// Attempt to load the embedded configuration properties:
		// if we can find them, see if there are any JVM arguments we should be using
		try {
			Properties config = Util.loadPropertiesFromResource(this.getClass().getClassLoader(), "cloudcoder.properties");
			String jvmArgs = config.getProperty("cloudcoder.builder2.jvmargs");
			if (jvmArgs != null) {
				// Set builder JVM options
				options.setBuilderJvmArgs(jvmArgs);
			}
		} catch (IllegalStateException e) {
			System.err.println("Warning: couldn't find cloudcoder.properties");
		}
		return options;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length >= 1 && args[0].equals("configure")) {
			// Allow the builder jarfile to be updated
			// (e.g., to add/replace cloudcoder.properties or keystore.jks)
			JarRewriter jarRewriter = null;
			for (int i = 1; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("--editJar=")) {
					arg = arg.substring("--editJar=".length());
					jarRewriter = new JarRewriter(arg);
				} else if (arg.startsWith("--replace=")) {
					arg = arg.substring("--replace=".length());
					int eq = arg.indexOf('=');
					String entry = arg.substring(0, eq);
					String fileName = arg.substring(eq + 1);
					jarRewriter.replaceEntry(entry, new JarRewriter.FileEntryData(fileName));
				} else if (arg.startsWith("--fromWebappJar=")) {
					// Copy configuration properties and keystore from a configured webapp jarfile.
					arg = arg.substring("--fromWebappJar=".length());
					System.out.println("Copying cloudcoder.properties and keystore.jks from " + arg + "...");
					extractJarEntry(arg, "cloudcoder.properties", "cloudcoder.properties");
					extractJarEntry(arg, "war/WEB-INF/classes/keystore.jks", "keystore.jks");
					jarRewriter.replaceEntry("cloudcoder.properties", new JarRewriter.FileEntryData("cloudcoder.properties"));
					jarRewriter.replaceEntry("keystore.jks", new JarRewriter.FileEntryData("keystore.jks"));
				}
			}
			jarRewriter.rewrite();
		} else if (args.length >= 1 && args[0].equals("batch")) {
			// Batch testing
			List<String> argList = new ArrayList<String>(Arrays.asList(args));
			argList.remove(0);
			BatchMain.main(argList.toArray(new String[argList.size()]));
		} else if (args.length >= 1 && args[0].equals("upgrade")) {
			doUpgrade();
		} else if (args.length >= 1 && args[0].equals("listconfig")) {
		    doListConfig();
		} else {
			Builder2DaemonController controller = new Builder2DaemonController();
			controller.exec(args);
		}
	}

	/**
	 * Extract an entry from a jarfile to a local file.
	 * 
	 * @param jarFile   the jarfile
	 * @param entryName the entry
	 * @param outFile   the local file
	 * @throws IOException 
	 */
	private static void extractJarEntry(String jarFile, String entryName, String outFile) throws IOException {
		JarFile jf = new JarFile(jarFile);
		ZipEntry zipEntry = jf.getEntry(entryName);
		if (zipEntry == null) {
			throw new IOException("Entry not found: " + entryName);
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = jf.getInputStream(zipEntry);
			out = new FileOutputStream(outFile);
			IOUtil.copy(in, out);
		} finally {
			IOUtil.closeQuietly(in);
			IOUtil.closeQuietly(out);
		}
	}

	private static void doUpgrade() throws IOException {
		Upgrade upgrader = new Upgrade("cloudcoderBuilder", "https://s3.amazonaws.com/cloudcoder-binaries", Builder2DaemonController.class);
		upgrader.addConfigFile("cloudcoder.properties");
		upgrader.addConfigFile("keystore.jks");
		upgrader.upgradeJarFile(new DefaultUpgradeCallback());
	}
	
	private static void doListConfig() throws IOException {
	    String filename=CLOUDCODER_PROPERTIES;
        Properties ccProps=Util.loadPropertiesFromResource(Builder2Daemon.class.getClassLoader(), filename);
        // go through properties in a certain order
        
        System.out.println("\n\ncloudcoderApp.jar configuration properties ('the Webapp')");
        printCloudCoderProperties(ccProps);
	}
	private static final String CLOUDCODER_PROPERTIES = "cloudcoder.properties";
    private static List<String> keys=Arrays.asList("gwt.sdk",
            "cloudcoder.db.user",
            "cloudcoder.db.passwd",
            "cloudcoder.db.databaseName",
            "cloudcoder.db.host",
            "cloudcoder.db.portStr",
            "cloudcoder.login.service",
            "cloudcoder.submitsvc.oop.host",
            "cloudcoder.submitsvc.oop.numThreads",
            "cloudcoder.submitsvc.oop.port",
            "cloudcoder.submitsvc.ssl.cn",
            "cloudcoder.submitsvc.ssl.keystore",
            "cloudcoder.submitsvc.ssl.keystore.password",
            "cloudcoder.builder2.jvmargs",
            "cloudcoder.webserver.port",
            "cloudcoder.webserver.contextpath",
            "cloudcoder.webserver.localhostonly");
    
    public static void printCloudCoderProperties(Properties ccProps) {
        for (String key : keys) {
            if (ccProps.containsKey(key)) {
                System.out.println(key+"="+ccProps.getProperty(key));
            }
        }
        for (Entry e : ccProps.entrySet()) {
            //OK, fine, this requires a few linear scans
            if (!keys.contains(e.getKey())) {
                System.out.println(e.getKey()+"="+e.getValue());
            }
        }
    }
}
