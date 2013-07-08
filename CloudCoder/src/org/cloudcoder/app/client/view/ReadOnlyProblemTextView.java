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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.client.ui.ResizeComposite;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Read-only view of current {@link ProblemText} in the session.
 * If there is a {@link Problem} in the session, it is used
 * to indicate what the source language of the problem text is. 
 * 
 * @author David Hovemeyer
 */
public class ReadOnlyProblemTextView extends ResizeComposite implements SessionObserver, Subscriber {
	private Session session;
	private AceEditor editor;

	public ReadOnlyProblemTextView() {
		this.editor = new AceEditor();
		initWidget(editor);
	}
	
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
		// Subscribe to added object events
		this.session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		// Activate editor
		editor.startEditor();
		editor.setFontSize("14px");
		editor.setReadOnly(true);
		editor.setTheme(AceEditorTheme.VIBRANT_INK);
		setEditorMode();
		
		// Set initial problem text (if any)
		setProblemText();
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof Problem) {
			setEditorMode();
		} else if (key == Session.Event.ADDED_OBJECT && hint instanceof ProblemText) {
			setProblemText();
		}
	}

	private void setEditorMode() {
		Problem problem = this.session.get(Problem.class);
		AceEditorMode mode = (problem != null) ? ViewUtil.getModeForLanguage(problem.getProblemType().getLanguage()) : null;
		if (mode == null) {
			mode = AceEditorMode.TEXT;
		}
		editor.setMode(mode);
	}

	private void setProblemText() {
		ProblemText problemText = session.get(ProblemText.class);
		if (problemText != null) {
			editor.setText(problemText.getText());
		}
	}
}
