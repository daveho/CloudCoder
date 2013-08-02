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

import java.util.ArrayList;
import java.util.List;

/**
 * A mix of {@link EditSequence}s that can be played for load testing.
 * 
 * @author David Hovemeyer
 */
public class Mix implements Cloneable {
	private List<EditSequence> editSequenceList;
	
	/**
	 * Constructor.
	 */
	public Mix() {
		this.editSequenceList = new ArrayList<EditSequence>();
	}
	
	/**
	 * @return the list of {@link EditSequence}s
	 */
	public List<EditSequence> getEditSequenceList() {
		return editSequenceList;
	}
	
	@Override
	public Mix clone() {
		try {
			Mix dup = (Mix) super.clone();
			
			// Deep copy all of the EditSequences in the Mix
			dup.editSequenceList = new ArrayList<EditSequence>();
			for (EditSequence seq : this.editSequenceList) {
				dup.editSequenceList.add(seq.clone());
			}
			
			return dup;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen");
		}
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
