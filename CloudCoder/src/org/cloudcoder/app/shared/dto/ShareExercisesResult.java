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

package org.cloudcoder.app.shared.dto;

import java.io.Serializable;

/**
 * @author jaimespacco
 *
 */
public class ShareExercisesResult implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private ShareExerciseStatus status;
    private String message;
    private boolean[] shareResults;
    private int current=0;
    
    public ShareExercisesResult() {
        shareResults=new boolean[0];
    }
    
    public ShareExercisesResult(int num) {
        shareResults=new boolean[num];
    }
    
    public ShareExerciseStatus getStatus() {
        return this.status;
    }
    
    public void success() {
        shareResults[current]=true;
        current++;
    }
    
    public void failAll(String msg) {
        this.message=msg;
        for (boolean b : shareResults) {
            if (b) {
                throw new IllegalStateException("Some exercises have already been shared; you are not allowed to fail all the results.  You probably wanted to call 'failRemaining()' instead.");
            }
        }
        status=ShareExerciseStatus.ALL_FAILED;
    }
    
    public void failRemaining(String msg) {
        this.message=msg;
        status=ShareExerciseStatus.SOME_FAILED;
    }
    
    public void allSucceeded(String msg) {
        for (boolean b : shareResults) {
            if (!b) {
                throw new IllegalStateException("Some exercises have failed to be shared; you are not allowed to declare success for all shares.  Did you mean to call 'failRemaining()' instead?");
            }
        }
        this.message=msg;
        status=ShareExerciseStatus.ALL_OK;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return
     */
    public int getNumSharedSuccessfully() {
        int count=0;
        for (boolean b : shareResults) {
            if (b) {
                count++;
            }
        }
        return count;
    }
}
