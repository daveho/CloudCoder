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

import org.cloudcoder.app.shared.model.RepoProblem;

/**
 * A result from searching the exercise repository.
 * 
 * @author David Hovemeyer
 */
public class RepoProblemSearchResult {
	public static final String MATCHED_TAG_LIST_ELEMENT_NAME = "matched_tag_list";
	
	private RepoProblem repoProblem;
	private List<String> matchedTagList;
	
	public RepoProblemSearchResult() {
		matchedTagList = new ArrayList<String>();
	}
	
	public void setRepoProblem(RepoProblem repoProblem) {
		this.repoProblem = repoProblem;
	}
	
	public RepoProblem getRepoProblem() {
		return repoProblem;
	}

	public void addMatchedTag(String matchedTag) {
		matchedTagList.add(matchedTag);
	}
	
	public List<String> getMatchedTagList() {
		return matchedTagList;
	}
}
