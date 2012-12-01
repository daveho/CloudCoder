// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.Date;

import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.shared.model.Language;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;

/**
 * Utility methods for views.
 * 
 * @author David Hovemeyer
 */
public class ViewUtil {
	/**
	 * Format a Date as a string.
	 * 
	 * @param date the Date to format
	 * @return the formatted Date
	 */
	public static String formatDate(Date date) {
		DateTimeFormat f = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
		return f.format(date);
	}
	
	/**
	 * Determine the {@link AceEditorMode} to use for a given {@link Language}.
	 * 
	 * @param language the Language
	 * @return the AceEditorMode for the Language, or null if the Language is not known
	 */
	public static AceEditorMode getModeForLanguage(Language language) {
		switch (language) {
		case JAVA:
			return AceEditorMode.JAVA;
		case PYTHON:
			return AceEditorMode.PYTHON;
		case C:
		case CPLUSPLUS:
			return AceEditorMode.C_CPP;
		case RUBY:
			return AceEditorMode.RUBY;
		}
		
		// unknown Language
		return null;
	}
}
