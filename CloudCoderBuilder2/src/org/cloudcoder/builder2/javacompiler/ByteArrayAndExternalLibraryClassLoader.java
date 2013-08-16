// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.javacompiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cloudcoder.daemon.IOUtil;

/**
 * Class loader similar to {@link ByteArrayClassLoader}, but in addition
 * to loading classes from byte arrays in memory, can also load them
 * from an external library jar file.  This handles the case of
 * executing a compiled Java submission which was compiled against
 * an external library.  There is probably a more elegant way to
 * do this using delegation, but the Java compilation and execution
 * code is already complicated enough, so I just went for a simple
 * and direct approach.
 * 
 * @author David Hovemeyer
 */
public class ByteArrayAndExternalLibraryClassLoader extends ClassLoader {
	private Map<String, byte[]> classes;
	private String externalLibraryFileName;
	private JarFile jarFile;
	
	/**
	 * Constructor.
	 * 
	 * @param classes                  map of class names to classes compiled in memory
	 * @param externalLibraryFileName  filename of an external jarfile whose classes should also be available
	 * @throws IOException
	 */
	public ByteArrayAndExternalLibraryClassLoader(Map<String, byte[]> classes, String externalLibraryFileName) throws IOException {
		this.classes = new HashMap<String, byte[]>();
		this.classes.putAll(classes);
		this.externalLibraryFileName = externalLibraryFileName;
		this.jarFile = new JarFile(externalLibraryFileName);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return super.loadClass(name);
		} catch (ClassNotFoundException e) {
			if (classes.containsKey(name)) {
				byte[] bytes = classes.get(name);
				return defineClass(name, bytes, 0, bytes.length);
			} else {
				byte[] bytes = readJarEntry(name);
				if (bytes == null) {
					throw new ClassNotFoundException("Cannot load " + name + " from external library");
				}
				classes.put(name, bytes);
				return defineClass(name, bytes, 0, bytes.length);
			}
		}
	}

	private byte[] readJarEntry(String name) {
		name = name.replace('.', '/') + ".class";
		JarEntry jarEntry = jarFile.getJarEntry(name);
		if (jarEntry == null) {
			return null;
		}
		InputStream jin = null;
		try {
			jin = jarFile.getInputStream(jarEntry);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtil.copy(jin, out);
			return out.toByteArray();
		} catch (Exception e) {
			return null;
		} finally {
			IOUtil.closeQuietly(jin);
		}
	}
}
