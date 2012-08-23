// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Configure a CloudCoder executable jarfile, either by reading configuration
 * settings interactively, or loading an existing cloudcoder.properties file.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class ConfigureCloudCoder
{
	private interface Condition {
		boolean evaluate();
	}
	
	private Condition IF_IMAP = new Condition() {
		@Override
		public boolean evaluate() {
			return config.getProperty("cloudcoder.login.service", "").equals("imap");
		}
	};
	
	private Condition IF_CONFIG_REPO = new Condition() {
		@Override
		public boolean evaluate() {
			return configureRepository;
		}
	};
	
	private static class Setting {
		public String prompt;
		public String name;
		public String defval;
		public boolean isSection;
		public Condition condition;
		
		public Setting(String prompt, String name, String defval, boolean isSection) {
			this(prompt, name, defval, isSection, null);
		}

		public Setting(String prompt, String name, String defval, boolean isSection, Condition condition) {
			this.prompt = prompt;
			this.name = name;
			this.defval = defval;
			this.isSection = isSection;
			this.condition = condition;
		}
	}

	private static Setting askprop(String prompt, String name) {
		return new Setting(prompt, name, null, false);
	}
	
	private static Setting askprop(String prompt, String name, String defval) {
		return new Setting(prompt, name, defval, false);
	}
	
	private static Setting askprop(String prompt, String name, String defval, Condition condition) {
		return new Setting(prompt, name, defval, false, condition);
	}
	
	private static Setting section(String name) {
		return new Setting(null, name, null, true);
	}
	
	private static Setting section(String name, Condition condition) {
		return new Setting(null, name, null, true, condition);
	}
	
	private List<Setting> getSettings() {
		return Arrays.asList(
			askprop("Where is your GWT SDK installed (the directory with webAppCreator in it)?", "gwt.sdk"),
			
			section("Database configuration properties"),
			askprop("What MySQL username will the webapp use to connect to the database?", "cloudcoder.db.user"),
			askprop("What MySQL username will the webapp use to connect to the database?",
					"cloudcoder.db.user", null),
			askprop("What MySQL password will the webapp use to connect to the database?",
				"cloudcoder.db.passwd", null),
			askprop("What MySQL database will contain the CloudCoder tables?",
				"cloudcoder.db.databaseName", "cloudcoderdb"),
			askprop("What host will CloudCoder connect to to access the MySQL database?",
				"cloudcoder.db.host", "localhost"),
			askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" +
				"Just hit enter if MySQL is running on the standard port.",
				"cloudcoder.db.portStr", null),
				
			section("Login service properties"),
			askprop("Which login service do you want to use (imap or database)?",
					"cloudcoder.login.service", "database"),
			askprop("What is the hostname or IP address of your IMAP server?",
					"cloudcoder.login.host", null, IF_IMAP),
			
			section("Builder properties"),
			askprop("What host will the CloudCoder webapp be running on?\n" +
					"(This information is needed by the Builder so it knows how to connect\n" +
					"to the webapp.)",
					"cloudcoder.submitsvc.oop.host", "localhost"),
			askprop("How many threads should the Builder use? (suggestion: 1 per core)",
				"cloudcoder.submitsvc.oop.numThreads", "2"),
			askprop("What port will the CloudCoder webapp use to listen for connections from\n" +
				"Builders?",
				"cloudcoder.submitsvc.oop.port", "47374"),
				
			section("TLS/SSL (secure communication between webapp and builder(s)"),
			askprop("What is the hostname of your institution?",
					  "cloudcoder.submitsvc.ssl.cn", "None"),
			askprop("What is the name of the keystore that will store your public/private keypair?\n" +
				"(A new keystore will be created if it doesn't already exist)",
				"cloudcoder.submitsvc.ssl.keystore", "keystore.jks"),
			askprop("What is the keystore/key password?",
				"cloudcoder.submitsvc.ssl.keystore.password", "changeit"),
				
			section("Web server properties (webapp)"),
			askprop("What port will the CloudCoder web server listen on?",
					"cloudcoder.webserver.port", "8081"),
			askprop("What context path should the webapp use?",
				"cloudcoder.webserver.contextpath", "/cloudcoder"),
			askprop("Should the CloudCoder web server listen only on localhost?\n" +
				"(Set this to 'true' if using a reverse proxy, which is recommended)",
				"cloudcoder.webserver.localhostonly", "true"),
			
			section("Database configuration (repository webapp)", IF_CONFIG_REPO),
			askprop("What MySQL username will the repository webapp use to connect to the database?",
					"cloudcoder.repoapp.db.user", null, IF_CONFIG_REPO),
			askprop("What MySQL password will the repository webapp use to connect to the database?",
				"cloudcoder.repoapp.db.passwd", null, IF_CONFIG_REPO),
			askprop("What MySQL database will contain the repository tables?",
				"cloudcoder.repoapp.db.databaseName", "cloudcoderrepodb", IF_CONFIG_REPO),
			askprop("What host will the repository webapp connect to to access the MySQL database?",
				"cloudcoder.repoapp.db.host", "localhost", IF_CONFIG_REPO),
			askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" +
				"Just hit enter if MySQL is running on the standard port.",
				"cloudcoder.repoapp.db.portStr", null, IF_CONFIG_REPO),
				
			section("Webserver configuration (repository webapp)", IF_CONFIG_REPO),
			askprop("What port will the exercise repository web server listen on?",
					"cloudcoder.repoapp.webserver.port", "8082", IF_CONFIG_REPO),
			askprop("What context path should the exercise repository webapp use?",
				"cloudcoder.repoapp.webserver.contextpath", "/repo", IF_CONFIG_REPO),
			askprop("Should the exercise repository web server listen only on localhost?\n" +
				"(Set this to 'true' if using a reverse proxy, which is recommended)",
				"cloudcoder.repoapp.webserver.localhostonly", "true", IF_CONFIG_REPO)
			
		);
	}
	
	private Properties config;
	private boolean configureRepository;
	
	public ConfigureCloudCoder() {
		this.config = new Properties();
		this.configureRepository = false;
	}
    
    private void execute() throws Exception
    {
        // re-write configuration properties
        Scanner keyboard=new Scanner(System.in);
        String readFromFile=ConfigurationUtil.ask(keyboard, "Do you want to read new configuration properties from a file?","no");
        
        if (readFromFile.equalsIgnoreCase("yes")) {
            String filename=ConfigurationUtil.ask(keyboard, "What is the name of the file containing the new configuration properties?", "cloudcoder.properties");
            //Properties properties = new Properties();
            config.load(new FileInputStream(filename));
        } else {
            // read configuration properties from command line
        	
        	// See if properties already exist
        	Properties origConfig = null;
        	try {
        		origConfig = DBUtil.getConfigProperties();
        	} catch (Exception e) {
        		// ignore
        	}
        	if (origConfig != null) {
        		String reuse = ConfigurationUtil.ask(keyboard, "It looks like you have already configured CloudCoder.\n" + 
        				"Use the previous configuration settings as defaults?", "yes");
        		if (!reuse.trim().toLowerCase().equals("yes")) {
        			// Don't use
        			origConfig = null;
        		}
        	}
        	
        	for (Setting setting : getSettings()) {
        		if (setting.condition != null && !setting.condition.evaluate()) {
        			continue;
        		}
        		
        		if (setting.isSection) {
        			System.out.println();
        			System.out.println("########################################################################");
        			System.out.println(" >>> " + setting.name + " <<<");
        			System.out.println("########################################################################");
        			System.out.println();
        			continue;
        		}
        		
        		String defval = origConfig.containsKey(setting.name) ? origConfig.getProperty(setting.name) : setting.defval;
        		
        		String value;
        		if (defval != null) {
        			value = ConfigurationUtil.ask(keyboard, setting.prompt, defval);
        		} else {
        			value = ConfigurationUtil.ask(keyboard, setting.prompt);
        		}
        		
        		config.setProperty(setting.name, value);
        	}
        }
        
        // Create the new configured jarfile
        String jarfileName=ConfigurationUtil.ask(keyboard, "What is the name of the jarfile containing all of the code for CloudCoder?", "cloudcoderApp.jar");
        copyJarfileWithNewProperties(jarfileName, "cloudcoder.properties");
        System.out.println("Wrote new configuration properties to cloudcoder.properties contained in jarfile "+jarfileName);
    }
    
    /**
     * copy input to output stream - available in several StreamUtils or Streams classes 
     */    
    private void copy(InputStream input, OutputStream output)
    throws IOException
    {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER))!= -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }
    private final byte[] BUFFER = new byte[4096 * 1024];

    private void copyJarfileWithNewProperties(String jarfileName, 
        String propertiesFileName)
    throws Exception
    {
        // read in jarfileName, and replace propertiesFileName with newProps
        ZipFile jarfile = new ZipFile(jarfileName);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        ZipOutputStream newJarfileData = new ZipOutputStream(bytes);

        // first, copy contents from existing war
        Enumeration<? extends ZipEntry> entries = jarfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            //System.out.println("copy: " + e.getName());
            
            if (e.getName().equals(propertiesFileName)) {
                // If we find the file we're interested in, copy it!
                ZipEntry newEntry = new ZipEntry(propertiesFileName);
                newJarfileData.putNextEntry(newEntry);
                config.store(newJarfileData, "");
            } else {
                newJarfileData.putNextEntry(e);
                if (!e.isDirectory()) {
                    copy(jarfile.getInputStream(e), newJarfileData);
                }
            }
            newJarfileData.closeEntry();
        }

        // close
        newJarfileData.close();
        bytes.flush();
        bytes.close();
        jarfile.close();
        
        // copy over the file with new version we had just changed
        FileOutputStream out=new FileOutputStream(jarfileName);
        ByteArrayInputStream in=new ByteArrayInputStream(bytes.toByteArray());
        copy(in, out);
        
        out.close();
    }
	
    public static void main(String[] args) throws Exception {
    	ConfigureCloudCoder configureCloudCoder = new ConfigureCloudCoder();
    	if (args.length == 1 && args[0].equals("--repo")) {
    		configureCloudCoder.configureRepository = true;
    	}
    	configureCloudCoder.execute();
    }
}
