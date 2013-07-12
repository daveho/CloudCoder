package org.cloudcoder.app.loadtester;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.Change;

public class EditSequence {
	private List<Change> changeList;
	
	public EditSequence() {
		
	}
	
	public void setChangeList(List<Change> changeList) {
		this.changeList = changeList;
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
