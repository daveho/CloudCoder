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

import java.util.Iterator;
import java.util.LinkedList;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.IFunction;
import org.cloudcoder.app.shared.model.Problem;

/**
 * Object to manage the state of the code that the user is working
 * on.  Maintains the {@link ChangeList}, and manages the
 * current {@link CodeState}.  Takes care of running callbacks which should
 * be fired when a particular {@link CodeState} is reached.
 * 
 * @author David Hovemeyer
 */
public class CodeStateManager {
	/**
	 * Code state predicate matching states where there is no
	 * asynchronous operation pending.
	 */
	public static IFunction<CodeState, Boolean> NO_PENDING_OPERATION = new IFunction<CodeState, Boolean>() {
		@Override
		public Boolean invoke(CodeState arg) {
			return !arg.isPendingOperation();
		}
	};
	
	private static class StateChangeCallback {
		IFunction<CodeState, Boolean> predicate;
		Runnable f;
		
		StateChangeCallback(IFunction<CodeState, Boolean> predicate, Runnable f) {
			this.predicate = predicate;
			this.f = f;
		}
	}
	
	private Problem problem;
	private ChangeList changeList;
	private CodeState state;
	private LinkedList<StateChangeCallback> callbackList;
	
	public CodeStateManager() {
		this.changeList = new ChangeList();
		this.state = CodeState.NO_PROBLEM;
		this.callbackList = new LinkedList<StateChangeCallback>();
	}
	
	private void requireState(CodeState requiredState) {
		if (this.state != requiredState) {
			throw new UnsupportedOperationException("Invalid operation: "
					+ " state=" + this.state + ", required state=" + requiredState);
		}
	}
	
	private void changeState(CodeState nextState) {
		this.state = nextState;
		
		// Fire state change callbacks as appropriate
		Iterator<StateChangeCallback> i = callbackList.iterator();
		while (i.hasNext()) {
			StateChangeCallback callback = i.next();
			if (callback.predicate.invoke(state)) {
				callback.f.run();
				i.remove();
			}
		}
	}
	
	/**
	 * Register a callback to run when a particular {@link CodeState} is
	 * reached.
	 * 
	 * @param state the {@link CodeState} at which the callback should run
	 * @param f     the callback function
	 */
	public void runInState(final CodeState state, Runnable f) {
		runWhen(new IFunction<CodeState, Boolean>() {
			@Override
			public Boolean invoke(CodeState arg) {
				return state == arg;
			}
		}, f);
	}

	/**
	 * Register a callback to run when an arbitrary predicate function
	 * returns true for the the current {@link CodeState}.
	 *  
	 * @param predicate the predicate function
	 * @param f         callback to execute
	 */
	public void runWhen(IFunction<CodeState, Boolean> predicate, Runnable f) {
		if (predicate.invoke(state)) {
			// Current state matches predicate, so run now
			f.run();
		} else {
			callbackList.add(new StateChangeCallback(predicate, f));
		}
	}
	
	/**
	 * Set the {@link Problem}.
	 * This will cause a state transition from {@link CodeState#NO_PROBLEM}
	 * to {@link CodeState#NO_PROGRAM_TEXT}.
	 * 
	 * @param problem the problem to set
	 */
	public void setProblem(Problem problem) {
		requireState(CodeState.NO_PROBLEM);
		this.problem = problem;
		changeState(CodeState.NO_PROGRAM_TEXT);
	}
	
	/**
	 * Indicate that the program text has been received and set in the editor.
	 * This will cause a state transition from {@link CodeState#NO_PROGRAM_TEXT}
	 * to {@link CodeState#EDITABLE_CLEAN}.
	 */
	public void setProgramText() {
		requireState(CodeState.NO_PROGRAM_TEXT);
		changeState(CodeState.EDITABLE_CLEAN);
	}
	
	/**
	 * @return the problem
	 */
	public Problem getProblem() {
		return problem;
	}
	
	/**
	 * @return the state
	 */
	public CodeState getState() {
		return state;
	}

	/**
	 * Add a {@link Change} representing a code edit.
	 * This is only allowed if the current state allows editing.
	 * 
	 * @param change the {@link Change} to add
	 */
	public void addChange(Change change) {
		if (!state.isEditingAllowed()) {
			throw new IllegalStateException("Editing is not allowed in the " + state + " state");
		}
		changeList.addChange(change);
		state = CodeState.EDITABLE_DIRTY;
	}
	
	/**
	 * Initiate saving of any unsaved {@link Change}s (code edits).
	 * This method may only be called from the {@link CodeState#EDITABLE_DIRTY}
	 * state.  The callback object should call the
	 * 
	 * @param callback callback object to receive the unsaved changes
	 */
	public void saveChanges(ISaveChanges callback) {
		requireState(CodeState.EDITABLE_DIRTY);
		changeState(CodeState.SAVE_CHANGES_PENDING);
		Change[] unsaved = changeList.beginTransmit();
		callback.saveChanges(unsaved);
	}
	
	/**
	 * This method should be called by the callback passed to the
	 * {@link #saveChanges(ISaveChanges)} method to indicate
	 * whether or not the changes were saved successfully.
	 * It is expected that some amount of time will elapse between
	 * when saving changes is initiated and when this method is called. 
	 * 
	 * @param successful true if changes were saved successfully, false otherwise
	 */
	public void finishSavingChanges(boolean successful) {
		requireState(CodeState.SAVE_CHANGES_PENDING);
		changeState(successful ? CodeState.EDITABLE_CLEAN : CodeState.EDITABLE_DIRTY);
	}

	/**
	 * Change to the {@link CodeState#PREVENT_EDITS} state.
	 * This could reflect a catastrophic error (such as the server refusing
	 * to allow the user to work on the problem), or could mean a quiz
	 * has ended, etc.
	 */
	public void preventEdits() {
		changeState(CodeState.PREVENT_EDITS);
	}
}
