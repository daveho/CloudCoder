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

package org.cloudcoder.app.client.model;

/**
 * States that the user's code can be in.
 * Used by {@link CodeStateManager}.
 * 
 * @author David Hovemeyer
 */
public enum CodeState {
	/** The {@link Problem} has not been set yet. */
	NO_PROBLEM(false, true),
	
	/** The {@link Problem} has been set, but there is no program text yet. */
	NO_PROGRAM_TEXT(false, true),
	
	/** Code is editable, and there are no unsaved changes. */
	EDITABLE_CLEAN(true),
	
	/** Code is editable, but there are unsaved changes. */
	EDITABLE_DIRTY(true),
	
	/** Saving of unsaved changes has been initiated, but is not complete. */
	SAVE_CHANGES_PENDING(false, true),
	
	/** A submission is in progress, but is not complete. */
	SUBMIT_PENDING(false, true),
	
	/** Edits are no longer allowed: for example, because a quiz has ended. */
	PREVENT_EDITS(false, false),
	;
	
	private final boolean editingAllowed;
	private final boolean pendingOperation;
	
	private CodeState(boolean editingAllowed, boolean pendingOperation) {
		this.editingAllowed = editingAllowed;
		this.pendingOperation = pendingOperation;
	}

	private CodeState(boolean editingAllowed) {
		this(editingAllowed, false);
	}

	/**
	 * @return true if editing should be allowed in this state
	 */
	public boolean isEditingAllowed() {
		return editingAllowed;
	}
	
	/**
	 * @return true if this state is one where an asynchronous operation is pending
	 */
	public boolean isPendingOperation() {
		return pendingOperation;
	}
}
