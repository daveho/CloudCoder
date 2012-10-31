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

package org.cloudcoder.app.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Search criteria for finding {@link RepoProblem}s in the
 * exercise repository.
 * 
 * @author David Hovemeyer
 */
public class RepoProblemSearchCriteria {
	private Language language;
	private List<String> tagList;
	
	/**
	 * Constructor.
	 */
	public RepoProblemSearchCriteria() {
		tagList= new ArrayList<String>();
	}

	/**
	 * Set the language to search for.
	 * 
	 * @param language the language to search for
	 */
	public void setLanguage(Language language) {
		this.language = language;
	}
	
	/**
	 * @return the language to search for
	 */
	public Language getLanguage() {
		return language;
	}
	
	/**
	 * Add a tag to search for.
	 * 
	 * @param tag a tag to search for
	 */
	public void addTag(String tag) {
		tagList.add(tag);
	}
	
	/**
	 * Get list of tags to search for.
	 * 
	 * @return list of tags to search for
	 */
	public List<String> getTagList() {
		return tagList;
	}

	/**
	 * Determine whether search criteria are "empty",
	 * meaning no search criteria are specified.
	 * 
	 * @return true if search criteria are empty, false otherwise
	 */
	public boolean isEmpty() {
		return language == null && tagList.isEmpty();
	}
}
