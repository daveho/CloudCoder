package org.cloudcoder.app.loadtester;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.Change;

/**
 * A sequence of {@link Change}s: a series of edits to program
 * text working on a {@link Problem}.  An edit sequence can
 * be replayed using {@link PlayEditSequence}.
 * 
 * @author David Hovemeyer
 */
public class EditSequence implements Cloneable {
	private List<Change> changeList;
	
	/**
	 * Constructor.
	 */
	public EditSequence() {
		
	}
	
	@Override
	protected EditSequence clone() {
		try {
			return (EditSequence) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen", e);
		}
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
			oos.writeObject(changeList);
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
	@SuppressWarnings("unchecked")
	public void loadFromInputStream(InputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		
		try {
			ois = new ObjectInputStream(in);
			changeList = (List<Change>) ois.readObject();
		} finally {
			IOUtils.closeQuietly(ois);
		}
	}
}
