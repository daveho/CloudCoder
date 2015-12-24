// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.validator;

import com.google.gwt.user.client.ui.TextBox;

/**
 * @author jspacco
 *
 */
public class MatchingTextBoxValidator implements IFieldValidator<TextBox>
{
    private TextBox thisBox;
    private TextBox otherBox;
    private String msg;

    /**
     * 
     */
    public MatchingTextBoxValidator(String msg, TextBox otherBox) {
        this.otherBox=otherBox;
        this.msg=msg;
    }

    @Override
    public void setWidget(TextBox widget) {
        this.thisBox=widget;
    }

    
    @Override
    public boolean validate(IValidationCallback callback) {
        if (!thisBox.getText().equals(otherBox.getText())) {
            callback.onFailure(msg);
            return false;
        }
        callback.onSuccess();
        return true;
    }
}
