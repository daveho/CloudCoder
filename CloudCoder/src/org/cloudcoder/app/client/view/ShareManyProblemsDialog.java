// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.List;

import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.dto.ShareExercisesResult;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author jaimespacco
 *
 */
public class ShareManyProblemsDialog extends DialogBox {
    //private ProblemAndTestCaseList exercise;
    private Problem[] problems;
    private ICallback<ShareExercisesResult> resultCallback;
    private ShareProblemDialogPanel panel;
    
    public ShareManyProblemsDialog() {
        setTitle("Share problem(s)");
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
    public void setExercise(Problem[] exercises) {
        this.problems = exercises;
    }

    /**
     * Set the result callback that will receive the {@link OperationResult}
     * from attempting to share the exercise to the repository.
     * 
     * @param resultCallback the result callback
     */
    public void setResultCallback(ICallback<ShareExercisesResult> resultCallback) {
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
        
        RPC.getCoursesAndProblemsService.submitExercises(problems, repoUsername, repoPassword, new AsyncCallback<ShareExercisesResult>() {
            @Override
            public void onSuccess(ShareExercisesResult result_) {
                resultCallback.call(result_);
                hide();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // XXX Hopefully nothing has been shared at this point
                // since we don't have a ShareExercisesResult and can't know
                // which exercises have been shared
                ShareExercisesResult result=new ShareExercisesResult(1);
                result.failAll(caught.getMessage());
                resultCallback.call(result);
                hide();
            }
        });
    }

    /**
     * @param problemsList
     */
    public void setExercise(List<Problem> problemList) {
        this.problems=new Problem[problemList.size()];
        this.problems=problemList.toArray(this.problems);
    }
}
