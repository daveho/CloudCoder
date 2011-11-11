// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.app.client.model;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.util.Publisher;

/**
 * ChangeList stores a list of Change objects representing textual
 * changes in the editor.  It supports scheduling batches of changes
 * to be transmitted to the server.
 */
public class ChangeList extends Publisher {
	/**
	 * State enumeration - represents whether editor is clean,
	 * contains unsent changes, or is currently transmitting changes.
	 * The members of this enumeration are used as the event types
	 * published by the object.  (I.e., each state change
	 * is published.)
	 */
	public enum State {
        /** No unsent changes. */
		CLEAN, 
        /** There are unsent changes that have not been transmitted. */
		UNSENT,
        /** Some changes are currently in transmission. */
		TRANSMISSION,
	}
	
	private State state;                 // current state
	private boolean transmitSuccess;     // last transmit succeeded
	private List<Change> unsent;         // changes waiting to be sent
	private List<Change> inTransmission; // changes currently in-transit
	
	/**
	 * Constructor.
	 */
	public ChangeList() {
		this.state = State.CLEAN;
		this.transmitSuccess = true;
		this.unsent = new ArrayList<Change>();
		this.inTransmission = new ArrayList<Change>();
	}
	
	/**
	 * @return number of unsent changes
	 */
	public int getNumUnsentChanges() {
		return unsent.size();
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * @return true if most recent transmission was successful, false otherwise
	 */
	public boolean isTransmitSuccess() {
		return transmitSuccess;
	}
	
	/**
	 * Add a change (scheduling it to be sent to the server at some point in the future).
	 * 
	 * @param change a change
	 */
	public void addChange(Change change) {
		unsent.add(change);
		if (state == State.CLEAN) {
			state = State.UNSENT;
			notifySubscribers(getState(), null);
		}
	}
	
	/**
	 * Begin a transmission.
	 * 
	 * @return array of Change objects to be sent to server
	 */
	public Change[] beginTransmit() {
		assert state == State.UNSENT;
		assert !unsent.isEmpty();
		assert inTransmission.isEmpty() || !transmitSuccess;
		
		inTransmission.addAll(unsent);
		unsent.clear();
		
		state = State.TRANSMISSION;
		notifySubscribers(getState(), null);
		
		// return a single string containing the entire batch of changes
		return inTransmission.toArray(new Change[inTransmission.size()]);
	}
	
	/**
	 * Mark end of transmission.
	 * 
	 * @param success true if transmission was successful, false otherwise
	 */
	public void endTransmit(boolean success) {
		assert state == State.TRANSMISSION;

		if (success) {
			// can now discard the in-transmission changes
			inTransmission.clear();
			
			// set state (noting that unsent changes may have accumulated)
			state = unsent.isEmpty() ? State.CLEAN : State.UNSENT;
		} else {
			// Next attempt will need to send
			//   - the changes we just failed to transmit
			//   - any additional changes that have accumulated in the meantime
			inTransmission.addAll(unsent);
			List<Change> tmp = inTransmission;
			inTransmission = unsent;
			unsent = tmp;
			state = State.UNSENT;
		}
		transmitSuccess = success;
		//GWT.log("Setting transmitSuccess to " + transmitSuccess);
		notifySubscribers(getState(), null);
	}
}
