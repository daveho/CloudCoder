package org.cloudcoder.app.loadtester;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	public void setChangeList(List<Change> changeList) {
		this.changeList = changeList;
	}
	
	public List<Change> getChangeList() {
		return changeList;
	}
	
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
	
	@SuppressWarnings("unchecked")
	public void loadFromFile(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		
		try {
			ois = new ObjectInputStream(new FileInputStream(fileName));
			changeList = (List<Change>) ois.readObject();
		} finally {
			IOUtils.closeQuietly(ois);
		}
	}
}
