package org.cloudcoder.app.loadtester;

import java.util.ArrayList;
import java.util.List;

/**
 * A mix of {@link EditSequence}s that can be played for load testing.
 * 
 * @author David Hovemeyer
 */
public class Mix {
	private List<EditSequence> editSequenceList;
	
	/**
	 * Constructor.
	 */
	public Mix() {
		this.editSequenceList = new ArrayList<EditSequence>();
	}
	
	/**
	 * Add an {@link EditSequence} to the mix.
	 * 
	 * @param editSequence the {@link EditSequence} to add
	 * @return this object, for chaining of calls to {@link #add(EditSequence)}
	 */
	public Mix add(EditSequence editSequence) {
		editSequenceList.add(editSequence);
		return this;
	}
	
	/**
	 * @return number of {@link EditSequence}s in this mix
	 */
	public int size() {
		return editSequenceList.size();
	}

	/**
	 * Get an {@link EditSequence} from the mix (0 for first).
	 * 
	 * @param index the index
	 * @return the {@link EditSequence}
	 */
	public EditSequence get(int index) {
		return editSequenceList.get(index);
	}
}
