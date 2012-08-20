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

package org.cloudcoder.webserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

import org.cloudcoder.jetty.NestedJarClassLoader;

/**
 * Main entry point for the self-contained single-jar-file
 * deployment for the CloudCoder web application.
 * 
 * @author David Hovemeyer
 */
public class CloudCoderWebServer {
	public static void main(String[] args) throws Exception {
		if (args.length == 1 && args[0].equals("createdb")) {
			// If the command is "createdb", then execute the CreateWebappDatabase
			// program from the CloudCoderModelClasesPersistence library.
			runMain("org.cloudcoder.app.server.persist.CreateWebappDatabase");
		} else if (args.length == 1 && args[0].equals("createcourse")) {
			// If command is "createcourse", run the CreateCourse program.
			runMain("org.cloudcoder.app.server.persist.CreateCourse");
		} else if (args.length == 1 && args[0].equals("registerstudents")) {
		    runMain("org.cloudcoder.app.server.persist.RegisterStudents");
		} else {
			// Otherwise, use the CloudCoderDaemonController to handle the command.
			// CloudCoderDaemonController handles requests to start/control/shutdown
			// the webapp (and its web server).
			CloudCoderDaemonController controller = new CloudCoderDaemonController();
			controller.exec(args);
		}
	}
	
	private static void runMain(String mainClassName) throws IOException,
			ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		ProtectionDomain p = CloudCoderWebServer.class.getProtectionDomain();
		String codeBase = p.getCodeSource().getLocation().toExternalForm();
		
		// Get path of executable jar file
		if (!codeBase.startsWith("file:") || !codeBase.endsWith(".jar")) {
			throw new IllegalStateException("Codebase " + codeBase + " not a jarfile");
		}
		String jarPath = codeBase.substring("file:".length());
		//System.out.println(jarPath);
		
		// Create a JarFile object to read from the executable jarfile.
		JarFile jarFile = new JarFile(jarPath);
		
		try {
			// A NestedJarClassLoader will allow CreateWebappDatabase to run out of
			// the cloudcoderModelClassesPersist.jar nested in the executable jarfile,
			// along with its dependencies, some of which are also nested jarfiles. 
			NestedJarClassLoader classLoader = new NestedJarClassLoader(jarFile, CloudCoderWebServer.class.getClassLoader());
			
			// Load and run CreateWebappDatabase's main via reflection.
			Class<?> createWebappDatabase = classLoader.loadClass(mainClassName);
			Method main = createWebappDatabase.getMethod("main", new Class<?>[]{String[].class});
			main.invoke(null, new Object[]{new String[0]});
		} finally {
			jarFile.close();
		}
	}
}
