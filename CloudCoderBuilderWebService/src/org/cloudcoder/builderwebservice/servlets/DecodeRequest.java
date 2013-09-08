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

package org.cloudcoder.builderwebservice.servlets;

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.webservice.util.BadRequestException;

/**
 * Logic for decoding a {@link Request}.
 * 
 * @author David Hovemeyer
 */
public class DecodeRequest {
	
	private static final Map<String, Language> languageMap = new HashMap<String, Language>();
	static {
		languageMap.put("java", Language.JAVA);
		languageMap.put("c", Language.CPLUSPLUS);  // We consider C to be C++
		languageMap.put("c++", Language.CPLUSPLUS);
		languageMap.put("python", Language.PYTHON);
		languageMap.put("ruby", Language.RUBY);
	}
	
	public static String problemTypeKey(Language language, boolean functionOrMethod) {
		return language.toString() + ":" + functionOrMethod;
	}
	
	private static final Map<String, ProblemType> problemTypeMap = new HashMap<String, ProblemType>();
	static {
		problemTypeMap.put(problemTypeKey(Language.JAVA, false), ProblemType.JAVA_PROGRAM);
		problemTypeMap.put(problemTypeKey(Language.JAVA, true), ProblemType.JAVA_METHOD);
		problemTypeMap.put(problemTypeKey(Language.PYTHON, true), ProblemType.PYTHON_FUNCTION);
		problemTypeMap.put(problemTypeKey(Language.RUBY, true), ProblemType.RUBY_METHOD);
		problemTypeMap.put(problemTypeKey(Language.CPLUSPLUS, false), ProblemType.C_PROGRAM);
	}
	
	/**
	 * Map the {@link Request} onto the corresponding CloudCoder {@link ProblemType}.
	 * 
	 * @param request the {@link Request}
	 * @return the {@link ProblemType}
	 * @throws BadRequestException if there is no corresponding problem type
	 */
	public static ProblemType getProblemType(Request request) throws BadRequestException {
		Language language = languageMap.get(request.getLanguage().trim().toLowerCase());
		if (language == null) {
			throw new BadRequestException("Unknown language: " + request.getLanguage());
		}
		
		boolean functionOrMethod = !(request.getTestcaseType().intValue() == 0); // 0=input on stdin, 1=input is list of argument values
		ProblemType problemType = problemTypeMap.get(problemTypeKey(language, functionOrMethod));
		if (problemType == null) {
			throw new BadRequestException("Unsupported execution type: language=" +
					request.getLanguage() + ", testcaseType=" + request.getTestcaseType());
		}
		
		return problemType;
	}
}
