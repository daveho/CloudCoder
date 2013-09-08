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

package org.cloudcoder.app.loadtester;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Problem;

/**
 * A sequence of {@link Change}s: a series of edits to program
 * text working on a {@link Problem}.  An edit sequence can
 * be replayed using {@link PlayEditSequence}.
 * 
 * @author David Hovemeyer
 */
public class EditSequence implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String exerciseName;
	private List<Change> changeList;
	
	/**
	 * Constructor.
	 */
	public EditSequence() {
		
	}
	
	@Override
	public EditSequence clone() {
		try {
			EditSequence dup = (EditSequence) super.clone(); // shallow copy
			dup.copyFrom(this); // deep copy!
			return dup;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}
	
	/**
	 * Copy date from given {@link EditSequence}.
	 * 
	 * @param other the edit sequence to copy into this one
	 */
	public void copyFrom(EditSequence other) {
		this.exerciseName = other.exerciseName;
		
		// Create a completely independent change list, with cloned Change/Event objects
		if (other.changeList == null) {
			this.changeList = null;
		} else {
			this.changeList = new ArrayList<Change>();
			// Very important: clone all of the Change objects, so that we have a true deep copy.
			// Otherwise, the clone will share the Change/Event objects, which is bad
			// if any of them are modified.
			for (Change c : other.changeList) {
				this.changeList.add(c.duplicate());
			}
			
		}
	}

	/**
	 * Set the exercise name.
	 * 
	 * @param exerciseName the exercise name
	 */
	public void setExerciseName(String exerciseName) {
		this.exerciseName = exerciseName;
	}
	
	/**
	 * @return the exercise name
	 */
	public String getExerciseName() {
		return exerciseName;
	}
	
	/**
	 * Set the list of {@link Change} objects.
	 * 
	 * @param changeList the list of {@link Change} objects
	 */
	public void setChangeList(List<Change> changeList) {
		this.changeList = changeList;
	}
	
	/**
	 * @return the list of {@link Change} objects
	 */
	public List<Change> getChangeList() {
		return changeList;
	}
	
	/**
	 * Save to given file.
	 * 
	 * @param fileName name of file
	 * @throws IOException
	 */
	public void saveToFile(String fileName) throws IOException {
		ObjectOutputStream oos = null;
		
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fileName));
			oos.writeObject(this);
			oos.flush();
		} finally {
			IOUtils.closeQuietly(oos);
		}
	}
	
	/**
	 * Load from given file.
	 * 
	 * @param fileName name of file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadFromFile(String fileName) throws IOException, ClassNotFoundException {
		loadFromInputStream(new FileInputStream(fileName));
	}
	
	/**
	 * Load from given {@link InputStream}, which is guaranteed
	 * to be closed.
	 * 
	 * @param in the {@link InputStream}
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void loadFromInputStream(InputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		
		try {
			ois = new ObjectInputStream(in);
			EditSequence data = (EditSequence) ois.readObject();
			this.copyFrom(data);
		} finally {
			IOUtils.closeQuietly(ois);
		}
	}
}
