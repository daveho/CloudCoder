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

package org.cloudcoder.app.client.page;

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.client.model.PageParams;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.UserSelection;

/**
 * Singleton map that associates page object classes with the
 * parameter names that identify them in the {@link PageParams}. 
 * 
 * @author David Hovemeyer
 */
public class PageObjectParamNameMap {
	private static final PageObjectParamNameMap theInstance = new PageObjectParamNameMap();
	
	/**
	 * @return the singleton instance of {@link PageObjectParamNameMap}
	 */
	public static PageObjectParamNameMap getInstance() {
		return theInstance;
	}
	
	private Map<Class<?>, String> map;
	
	private PageObjectParamNameMap() {
		map = new HashMap<Class<?>, String>();
		map.put(CourseSelection.class, "c");
		map.put(Problem.class, "p");
		map.put(UserSelection.class, "u");
	}
	
	/**
	 * Get the parameter name for given page object class.
	 * 
	 * @param pageObjectCls the page object class
	 * @return the parameter name
	 */
	public String get(Class<?> pageObjectCls) {
		return map.get(pageObjectCls);
	}
	
}
