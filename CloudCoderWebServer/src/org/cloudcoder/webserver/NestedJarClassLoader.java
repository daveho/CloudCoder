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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * Load classes from jarfiles nested within the given jarfile.
 * This is <em>extremely</em> slow, since resources are located by a
 * sequential scan of the nested jarfiles.  We use it here
 * only because we're doing something (creating the webapp database)
 * that is not performance-critical.  Also, the URLs of resources
 * loaded from this classloader refer to temporary files (since jar:
 * URLs don't nest), and code that expects the URL of a returned resource
 * to bear some resemblance to its resource name will break.
 * 
 * @author David Hovemeyer
 */
public class NestedJarClassLoader extends ClassLoader {
	private JarFile jar;
	private Map<String, String> resourceToNestedJarMap;
	
	/**
	 * Constructor.
	 * 
	 * @param jar     a {@link JarFile} containing nested jar files from which
	 *                to load classes and resources
	 * @param parent  the parent classloader
	 * @throws IOException if an error occurs scanning the contents of the nested jarfiles
	 */
	public NestedJarClassLoader(JarFile jar, ClassLoader parent) throws IOException {
		super(parent);
		this.jar = jar;
		scanNestedJarFiles();
	}
	
	private void scanNestedJarFiles() throws IOException {
		resourceToNestedJarMap = new HashMap<String, String>();
		
		Enumeration<JarEntry> e = jar.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = e.nextElement();
			if (entry.getName().endsWith(".jar")) {
				// Scan the nested jar for entries.
				InputStream in = jar.getInputStream(entry);
				try {
					JarInputStream jin = new JarInputStream(in);
					while (true) {
						JarEntry nestedEntry = jin.getNextJarEntry();
						if (nestedEntry == null) {
							break;
						}
						//System.out.println(nestedEntry.getName() + " -> " + entry.getName());
						resourceToNestedJarMap.put(nestedEntry.getName(), entry.getName());
					}
				} finally {
					in.close();
				}
			}
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// Convert binary class name to resource name
		String resourceName = name.replace('.', '/') + ".class";
		//System.out.println("Looking for " + resourceName);
		
		// See if the resource is available in a nested jarfile
		String nestedJar = resourceToNestedJarMap.get(resourceName);
		if (nestedJar != null) {
			// Load class bytes from nested jar
			try {
				byte[] bytes = loadDataFromNestedJar(nestedJar, resourceName);
				return defineClass(name, bytes, 0, bytes.length);
			} catch (IOException e) {
				System.err.println("Error reading from nested jar entry " + resourceName);
				e.printStackTrace();
			}
		}
		
		// Resolve via parent classloader
		return super.findClass(name);
	}
	
	@Override
	protected URL findResource(String name) {
		// See if the resource is in a nested jarfile
		String nestedJar = resourceToNestedJarMap.get(name);
		
		if (nestedJar != null) {
			// Put it in a temp file, and return a URL pointing to the temp file.
			// We'll cross our fingers and hope that the code loading the resource
			// doesn't attempt to inspect the URI (which will be meaningless and
			// have no relation to the resource name).
			int lastDot = name.lastIndexOf('.');
			try {
				File tmp = File.createTempFile("ccws", lastDot >= 0 ? name.substring(lastDot) : ".tmp");
				tmp.deleteOnExit();
				
				// Get the resource data
				byte[] bytes = loadDataFromNestedJar(nestedJar, name);
				
				// Write it to the temp file
				FileOutputStream fos = new FileOutputStream(tmp);
				try {
					fos.write(bytes);
				} finally {
					fos.close();
				}
				
				return tmp.toURI().toURL();
			} catch (IOException e) {
				System.err.println("Error loading resource " + name + " from " + nestedJar);
				e.printStackTrace();
			}
		}

		// Resolve via parent classloader
		return super.findResource(name);
	}

	private byte[] loadDataFromNestedJar(String nestedJar, String resourceName) throws IOException {
		JarEntry entry = jar.getJarEntry(nestedJar);
		InputStream in = jar.getInputStream(entry);
		try {
			JarInputStream jin = new JarInputStream(in);
			while (true) {
				JarEntry nestedEntry = jin.getNextJarEntry();
				if (nestedEntry == null) {
					throw new IllegalStateException("Couldn't find resource " + resourceName + " in " + nestedJar);
				}
				if (nestedEntry.getName().equals(resourceName)) {
					int length = (int) nestedEntry.getSize();
					byte[] bytes = new byte[length];
					int n = 0;
					while (n < length) {
						int r = jin.read(bytes, n, bytes.length - n);
						if (r > 0) {
							n += r;
						}
					}
					return bytes;
				}
			}
		} finally {
			in.close();
		}
	}
}
