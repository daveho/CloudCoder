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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.PageParams;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.UserSelection;

import com.google.gwt.core.shared.GWT;

/**
 * Create {@link PageParams} from the current {@link CloudCoderPage} and {@link Session}.
 * 
 * @author David Hovemeyer
 */
public class CreatePageParamsFromPageAndSession {
	/**
	 * Create {@link PageParams} from the given {@link CloudCoderPage} and
	 * its {@link Session}.
	 * 
	 * @param page the {@link CloudCoderPage}
	 * @return the {@link PageParams}
	 */
	public PageParams create(CloudCoderPage page) {
		PageParams pageParams = new PageParams();
		
		for (Class<?> pageObjectCls : page.getRequiredPageObjects()) {
			String paramName = PageObjectParamNameMap.getInstance().get(pageObjectCls);
			
			if (paramName != null) {
				GWT.log("create param " + paramName);
				String paramValue = null;
				if (pageObjectCls == CourseSelection.class) {
					CourseSelection courseSelection = page.getSession().get(CourseSelection.class);
					if (courseSelection != null) {
						paramValue = String.valueOf(courseSelection.getCourse().getId());
					} else {
						GWT.log("No CourseSelection in Session");
					}
				} else if (pageObjectCls == Problem.class) {
					Problem problem = page.getSession().get(Problem.class);
					if (problem != null) {
						paramValue = String.valueOf(problem.getProblemId());
					} else {
						GWT.log("No Problem in session");
					}
				} else if (pageObjectCls == UserSelection.class) {
					UserSelection userSelection = page.getSession().get(UserSelection.class);
					if (userSelection != null) {
						paramValue = String.valueOf(userSelection.getUser().getId());
					} else {
						GWT.log("No UserSelection in session");
					}
				} else {
					GWT.log("Don't know how to get param value for " + pageObjectCls.getName());
				}
				
				if (paramValue != null) {
					
					pageParams.put(paramName, paramValue);
				}
			}
		}
		
		return pageParams;
	}
}
