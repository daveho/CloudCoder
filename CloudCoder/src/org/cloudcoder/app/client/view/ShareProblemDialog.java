// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * Dialog for sharing an exercise to the exercise repository.
 * 
 * @author David Hovemeyer
 */
public class ShareProblemDialog extends DialogBox {
	private ProblemAndTestCaseList exercise;
	private ICallback<OperationResult> resultCallback;
	private ShareProblemDialogPanel panel;
	
	public ShareProblemDialog() {
		setTitle("Share problem");
		setGlassEnabled(true);

		this.panel = new ShareProblemDialogPanel();
		add(panel);
		
		panel.setCancelButtonCallback(new Runnable() {
			@Override
			public void run() {
				hide();
			}
		});
		
		panel.setShareExerciseButtonCallback(new Runnable() {
			@Override
			public void run() {
				onClickShare();
			}
		});
	}
	
	/**
	 * @param exercise the exercise to set
	 */
	public void setExercise(ProblemAndTestCaseList exercise) {
		this.exercise = exercise;
		Problem problem = exercise.getProblem();
		panel.setExerciseName(problem.getTestname() + " - " + problem.getBriefDescription());
		panel.setExerciseLicense(problem.getLicense().getName());
	}

	/**
	 * Set the result callback that will receive the {@link OperationResult}
	 * from attempting to share the exercise to the repository.
	 * 
	 * @param resultCallback the result callback
	 */
	public void setResultCallback(ICallback<OperationResult> resultCallback) {
		this.resultCallback = resultCallback;
	}

	protected void onClickShare() {
		String repoUsername = panel.getUsername();
		String repoPassword = panel.getPassword();
		
		// Make sure that the repository username and password were entered correctly.
		if (repoUsername.equals("") || repoPassword.equals("")) {
			panel.setErrorMessage("Please enter your repository username and password");
			return;
		}
		
		RPC.getCoursesAndProblemsService.submitExercise(exercise, repoUsername, repoPassword, new AsyncCallback<OperationResult>() {
			@Override
			public void onSuccess(OperationResult result_) {
				resultCallback.call(result_);
				hide();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				resultCallback.call(new OperationResult(false, caught.getMessage()));
				hide();
			}
		});
	}
}
