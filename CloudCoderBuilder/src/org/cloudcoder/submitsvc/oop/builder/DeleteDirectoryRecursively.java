/*
 * Web C programming environment
 * Copyright (c) 2010-2011, David H. Hovemeyer <david.hovemeyer@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudcoder.submitsvc.oop.builder;

import java.io.File;

/**
 * Delete a directory tree recursively.
 * 
 * @author David Hovemeyer
 */
public class DeleteDirectoryRecursively {
	private File baseDir;

	/**
	 * Constructor.
	 * 
	 * @param baseDir directory to delete
	 */
	public DeleteDirectoryRecursively(File baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * Delete the directory passed to the constructor.
	 * 
	 * @return true if directory was deleted successfully, false otherwise
	 */
	public boolean delete() {
		return deleteRecursively(baseDir);
	}

	private boolean deleteRecursively(File f) {
		if (f.isDirectory()) {
			// delete all children
			File[] items = f.listFiles();
			for (File item : items) {
				deleteRecursively(item);
			}
		}
		return f.delete();
	}
}
